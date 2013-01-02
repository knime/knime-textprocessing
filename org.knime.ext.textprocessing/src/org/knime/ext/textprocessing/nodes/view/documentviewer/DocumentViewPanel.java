/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   05.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.TableCellRenderer;

import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.ImgLoaderUtil;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewPanel extends JSplitPane {

    private SearchEngines m_linkSources = SearchEngines.getInstance();

    private static final int MAX_COLS = 800;
    private static final int MAX_ROWS = 30;

    private static final boolean HILITE_TAGS = false;

    private final DocumentProvider m_docProvider;

    private Document m_doc;

    private int m_rowIndex;

    private JEditorPane m_fulltextPane;

    private JToggleButton m_hiliteTags;

    private JComboBox m_tagTypes;

    private JComboBox m_linkSourcesBox;

    private JButton m_colorChooserButton;

    private Color m_taggedEntityColor = Color.BLUE;

    private int m_infoRow;

    private JPopupMenu m_rightClickMenue;

    private JTextField m_searchField;

    private JToggleButton m_searchButton;

    private JButton m_nextButton;

    private JButton m_previousButton;

    /**
     * Creates new instance of <code>DocumentViewPanel</code> with given
     * document to display.
     *
     * @param doc The document to display.
     * @param docProvider The provider for next or previous documents.
     * @param rowIndex The index of the row containing the document to display.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     * @since 2.8
     */
    public DocumentViewPanel(final Document doc,
          final DocumentProvider docProvider, final int rowIndex)
                  throws IllegalArgumentException {
        super(JSplitPane.VERTICAL_SPLIT);

        if (doc == null) {
            throw new IllegalArgumentException("Document may not be null!");
        }
        m_doc = doc;
        m_docProvider = docProvider;
        m_rowIndex = rowIndex;

        displayDoc();
    }

    /**
     * Creates new instance of <code>DocumentViewPanel</code> with given
     * document to display.
     *
     * @param doc The document to display.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     */
    public DocumentViewPanel(final Document doc)
            throws IllegalArgumentException {
        this(doc, null, 0);
    }

    private void displayDoc() {
        JComponent controlPanel = createControlPanel();
        JPanel mainPanel = createMainPanel();
        JPanel authorPanel = createAuthorPanel();
        JPanel infoPanel = createInfoPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new GridLayout(1, 2));
        bottomPane.add(authorPanel);
        bottomPane.add(infoPanel);

        setTopComponent(topPanel);
        setBottomComponent(bottomPane);

        setOneTouchExpandable(true);
        setDividerLocation(600);
    }

    private JComponent createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToolBar hiliteToolbar = new JToolBar();
        // hilite button
        m_hiliteTags = new JToggleButton();
        m_hiliteTags.setSelected(HILITE_TAGS);
        m_hiliteTags.addActionListener(new HiliteActionListener());
        ImageIcon icon = ImgLoaderUtil.loadImageIcon("marker.png",
                                                     "Hilite tags");
        m_hiliteTags.setIcon(icon);
        m_hiliteTags.setToolTipText("click to hilite tagged terms");
        hiliteToolbar.add(m_hiliteTags);

        // color chooser
        m_colorChooserButton = new JButton();
        icon = ImgLoaderUtil.loadImageIcon("color.png", "Color");
        m_colorChooserButton.setIcon(icon);
        m_colorChooserButton.setToolTipText("Choose Color");
        m_colorChooserButton.setBorder(BorderFactory.createLineBorder(
                m_taggedEntityColor));
        m_colorChooserButton.setBorderPainted(true);
        m_colorChooserButton.addActionListener(new ColorButtonListener());
        m_colorChooserButton.setOpaque(true);
        hiliteToolbar.add(m_colorChooserButton);

        hiliteToolbar.addSeparator();

        // tag combo box
        m_tagTypes = new JComboBox();
        Set<String> tagTypes = TagFactory.getInstance().getTagTypes();
        for (String tagType : tagTypes) {
            m_tagTypes.addItem(tagType);
        }
        m_tagTypes.addActionListener(new HiliteActionListener());
        hiliteToolbar.add(m_tagTypes);

        hiliteToolbar.addSeparator();

        // links sources
        hiliteToolbar.add(new JLabel("Link to:"));
        m_linkSourcesBox = new JComboBox();
        for (String source : m_linkSources.getSearchEngineNames()) {
            m_linkSourcesBox.addItem(source);
        }
        m_linkSourcesBox.setSelectedItem(m_linkSources.getDefaultSource());
        m_linkSourcesBox.setEnabled(m_hiliteTags.isSelected());
        m_linkSourcesBox.addActionListener(new LinkSourceListener());
        hiliteToolbar.add(m_linkSourcesBox);

        JToolBar searchToolbar = new JToolBar();
        // search field and button
        m_searchButton = new JToggleButton();
        m_searchButton.addActionListener(new SearchListener());
        icon = ImgLoaderUtil.loadImageIcon("search.png", "Search");
        m_searchButton.setIcon(icon);
        m_searchButton.setToolTipText("click to search");
        searchToolbar.add(m_searchButton);
        m_searchField = new JTextField();
        m_searchField.setColumns(10);
        m_searchField.getDocument().addDocumentListener(new SearchListener());
        searchToolbar.add(m_searchField);

        // next and previous buttons
        JToolBar navigationToolbar = new JToolBar();
        m_previousButton = new BasicArrowButton(SwingConstants.WEST);
        m_previousButton.addActionListener(new NewDocumentListener());
        m_previousButton.setToolTipText("show previous document");
        navigationToolbar.add(m_previousButton);
        m_nextButton = new BasicArrowButton(SwingConstants.EAST);
        m_nextButton.addActionListener(new NewDocumentListener());
        m_nextButton.setToolTipText("show next document");
        navigationToolbar.add(m_nextButton);

        controlPanel.add(searchToolbar);
        controlPanel.add(hiliteToolbar);
        controlPanel.add(navigationToolbar);

        return controlPanel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        m_fulltextPane = new JEditorPane();
        m_fulltextPane.setContentType("text/html");
        m_fulltextPane.setText(getPreparedText(m_doc, HILITE_TAGS, "", null,
                m_searchButton.isSelected()));
        m_fulltextPane.setEditable(false);
        m_fulltextPane.setCaretPosition(0);
        m_fulltextPane.addHyperlinkListener(new LinkListener());
        m_fulltextPane.setToolTipText("Select text and right click.");

        if (checkBrowsingSupport()) {
            m_rightClickMenue = new JPopupMenu();
            JMenuItem item;
            for (String source : m_linkSources.getSearchEngineNames()) {
                item = new JMenuItem(source);
                item.addActionListener(new RightClickMenueListener());
                m_rightClickMenue.add(item);
            }
            m_fulltextPane.setComponentPopupMenu(m_rightClickMenue);
        }

        JScrollPane jsp = new JScrollPane(m_fulltextPane);
        jsp.setPreferredSize(new Dimension(MAX_COLS, MAX_ROWS * 20));
        panel.add(jsp, BorderLayout.CENTER);

        return panel;
    }

    private String getPreparedText(final Document doc, final boolean hiliteTags,
            final String tagType, final String search,
            final boolean hiliteSearch) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<br/><font face=\"Verdana\" size=\"4\"><b>");
        buffer.append(doc.getTitle());
        buffer.append("</b></font><br/>");

        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            buffer.append("<br/><hr><br/>");
            buffer.append("<font face=\"Verdana\" size=\"3\"><b>");
            buffer.append(s.getAnnotation().toString());
            buffer.append("</b></font>");

            List<Paragraph> paras = s.getParagraphs();
           for (Paragraph p : paras) {
               buffer.append("<font face=\"Verdana\" size=\"3\"><br/><br/>");
               buffer.append(getParagraphText(p, hiliteTags, tagType, search,
                       hiliteSearch));
               buffer.append("</font>");
           }
        }

        return buffer.toString();
    }

    private String getParagraphText(final Paragraph p,
            final boolean hiliteTags, final String tagType,
            final String search, final boolean hiliteSearch) {
        if (!hiliteTags && !hiliteSearch) {
            return p.getText();
        }

        // selected color to hex str
        String hexColorStr = Integer.toHexString(
                m_taggedEntityColor.getRGB() & 0x00ffffff);

        StringBuffer paramStr = new StringBuffer();
        for (Sentence sen : p.getSentences()) {
            for (Term t : sen.getTerms()) {
                boolean marked = false;

                // search hiliting
                if (hiliteSearch && search != null) {
                    if (search.toLowerCase().equals(t.getText().toLowerCase()))
                    {
                        paramStr.append("<font style=\"BACKGROUND-COLOR: green;"
                                + "COLOR: white\">"
                                + t.getText() + "</font>");
                        paramStr.append(" ");
                        marked = true;
                        continue;
                    }
                }

                if (hiliteTags) {
                    if (t.getTags().size() > 0) {
                        List<Tag> tags = t.getTags();
                        for (Tag tag : tags) {
                            // tag hiliting
                            if (tag.getTagType().equals(tagType)) {
                                String link =
                                        m_linkSources.getUrlString(
                                                (String)m_linkSourcesBox
                                                        .getSelectedItem(), t
                                                        .getText());

                                paramStr.append("<a href=\"" + link + "\">");
                                paramStr.append("<font color=\"#" + hexColorStr
                                        + "\">" + t.getText() + "</font>");
                                paramStr.append("</a>");

                                paramStr.append(" ");
                                marked = true;
                                break;
                            }
                        }
                    }
                }
                if (!marked) {
                    paramStr.append(t.getText() + " ");
                }
            }
        }
        return paramStr.toString();
    }

    private JPanel createAuthorPanel() {
        Object[][] names;

        Set<Author> authors = m_doc.getAuthors();
        if (authors != null && authors.size() > 0) {
            names = new Object[authors.size()][2];
            int count = 0;
            for (Author a : authors) {
                if (a.getFirstName() == null) {
                    names[count][0] = "";
                } else {
                    names[count][0] = a.getFirstName();
                }
                if (a.getLastName() == null) {
                    names[count][1] = "";
                } else {
                    names[count][1] = a.getLastName();
                }
                count++;
            }
        } else {
            names = new Object[1][2];
            names[0][0] = "";
            names[0][1] = "";
        }

        return createMetaInfoPanel("Authors", names,
                new Object[]{"First name", "Last name"});
    }

    private JPanel createInfoPanel() {
        int rowCount = 3 + m_doc.getSources().size()
        + m_doc.getCategories().size();
        Object[][] info = new Object[rowCount][2];

        info[0][0] = "Filename";
        info[0][1] = m_doc.getDocFile().getAbsolutePath().toString();
        info[1][0] = "Publication date";
        info[1][1] = m_doc.getPubDate().toString();
        info[2][0] = "Document type";
        info[2][1] = m_doc.getType().toString();

        int row = 3;
        // Sources
        Set<DocumentSource> sources = m_doc.getSources();
        for (DocumentSource s : sources) {
            info[row][0] = "Document source";
            info[row][1] = s.getSourceName();
            row++;
        }

        // Categories
        Set<DocumentCategory> cats = m_doc.getCategories();
        for (DocumentCategory c : cats) {
            info[row][0] = "Document category";
            info[row][1] = c.getCategoryName();
            row++;
        }

        return createMetaInfoPanel("Metainfo", info,
                new Object[]{"Name", "Value"});
    }

    private JPanel createMetaInfoPanel(final String title,
            final Object[][] metaInfo, final Object[] metaInfoTableHeader) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel();
        Font f = new Font("Verdana", Font.BOLD, 13);
        label.setFont(f);
        label.setText(title);
        panel.add(label, BorderLayout.NORTH);

        JTable table =
                new JTable(metaInfo, metaInfoTableHeader);
        table.addMouseMotionListener(new MetaInfoTableListener());
        table.setOpaque(false);
        table.setDefaultRenderer(Object.class, new AttributiveCellRenderer());

        JScrollPane jsp = new JScrollPane(table);
        jsp.setPreferredSize(new Dimension(MAX_COLS / 2, MAX_ROWS * 5));
        panel.add(jsp, BorderLayout.CENTER);
        panel.setBorder(new EtchedBorder());

        return panel;
    }

    private void refreshHiliting() {
        m_fulltextPane.setText(getPreparedText(m_doc,
                m_hiliteTags.isSelected(),
                m_tagTypes.getSelectedItem().toString(),
                m_searchField.getText(), m_searchButton.isSelected()));
    }

    private boolean checkBrowsingSupport() {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                return true;
            }
        }
        return false;
    }

    private void openUrlInBrowser(final URL u) {
        if (u != null) {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(u.toURI());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    private class SearchListener implements ActionListener, DocumentListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            refreshHiliting();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void insertUpdate(final DocumentEvent e) {
            refreshHiliting();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeUpdate(final DocumentEvent e) {
            refreshHiliting();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void changedUpdate(final DocumentEvent e) {
            refreshHiliting();
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    private class RightClickMenueListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            String selectedText = m_fulltextPane.getSelectedText();
            if (selectedText != null) {
                if (selectedText.length() > 0) {
                    if (e.getSource() instanceof JMenuItem) {
                        String source = ((JMenuItem)e.getSource()).getText();
                        String urlStr = SearchEngines.getInstance()
                            .getUrlString(source, selectedText);

                        try {
                            URL u = new URL(urlStr);
                            openUrlInBrowser(u);
                        } catch (MalformedURLException e1) {
                            // No msg here
                        }
                    }
                }
            }
        }

    }


    private class NewDocumentListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource().equals(m_nextButton)) {
                if (m_docProvider != null) {
                    int nextIndex = m_rowIndex + 1;
                    Document nextDoc = m_docProvider.getDocument(nextIndex);
                    if (nextDoc != null) {
                        m_doc = nextDoc;
                        m_rowIndex = nextIndex;
                    }
                }
            } else if (e.getSource().equals(m_previousButton)) {
                if (m_docProvider != null) {
                    int prevIndex = m_rowIndex - 1;
                    Document prevDoc = m_docProvider.getDocument(prevIndex);
                    if (prevDoc != null) {
                        m_doc = prevDoc;
                        m_rowIndex = prevIndex;
                    }
                }
            }

            displayDoc();
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    private class LinkSourceListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            refreshHiliting();
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    private class LinkListener implements HyperlinkListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                URL u = e.getURL();
                openUrlInBrowser(u);
            }
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    private class ColorButtonListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            m_taggedEntityColor = JColorChooser.showDialog(null,
                    "Choose hilite color", m_taggedEntityColor);
            if (m_taggedEntityColor == null) {
                m_taggedEntityColor = Color.RED;
            }
            m_colorChooserButton.setOpaque(true);
            m_colorChooserButton.setBorder(BorderFactory.createLineBorder(
                    m_taggedEntityColor));
            repaint();
            refreshHiliting();
        }
    }

    /**
     * Sets (un-)hilited text when action was performed.
     *
     * @author Kilian Thiel, University of Konstanz
     */
    class HiliteActionListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent arg0) {
            refreshHiliting();
            if (checkBrowsingSupport()) {
                m_linkSourcesBox.setEnabled(m_hiliteTags.isSelected());
            } else {
                m_linkSourcesBox.setEnabled(false);
            }
        }
    }

    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    private class MetaInfoTableListener extends MouseAdapter {

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(final MouseEvent e) {
            JTable aTable = (JTable)e.getSource();
            m_infoRow = aTable.rowAtPoint(e.getPoint());
            aTable.repaint();
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    @SuppressWarnings("serial")
    private class AttributiveCellRenderer extends JLabel
    implements TableCellRenderer {

        /**
         * Constructor.
         */
        public AttributiveCellRenderer() {
            setOpaque(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                final Object value, final boolean isSelected,
                final boolean hasFocus, final int row, final int column) {

            if (isSelected) {
                this.setBackground(Color.DARK_GRAY);
                this.setForeground(Color.WHITE);
            } else if (row == m_infoRow) {
                this.setBackground(Color.LIGHT_GRAY);
                this.setForeground(Color.BLACK);
            } else {
                this.setBackground(Color.WHITE);
                this.setForeground(Color.BLACK);
            }

            this.setText(value.toString());
            return this;
        }
    }
}

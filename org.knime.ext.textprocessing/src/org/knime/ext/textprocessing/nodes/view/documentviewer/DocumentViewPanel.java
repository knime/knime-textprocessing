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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewPanel extends JSplitPane {

    private static final int MAX_COLS = 500;
    private static final int MAX_ROWS = 30;

    private static final boolean HILITE_TAGS = false;

    private final Document m_doc;

    private JEditorPane m_fulltextPane;

    private JCheckBox m_hiliteTags;

    private JComboBox m_tagTypes;

    /**
     * Creates new instance of <code>DocumentViewPanel</code> with given
     * document to display.
     *
     * @param doc The document to display.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     */
    public DocumentViewPanel(final Document doc)
    throws IllegalArgumentException {
        super(JSplitPane.VERTICAL_SPLIT);

        if (doc == null) {
            throw new IllegalArgumentException("Document may not be null!");
        }
        m_doc = doc;
        displayDoc();
    }

    private void displayDoc() {
        JPanel controlPanel = createControlPanel();
        JPanel mainPanel = createMainPanel();
        JPanel authorPanel = createAuthorPanel();
        JPanel infoPanel = createInfoPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(mainPanel, BorderLayout.CENTER);

        JSplitPane bottomPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomPane.setTopComponent(authorPanel);
        bottomPane.setBottomComponent(infoPanel);
        bottomPane.setResizeWeight(0.5);

        setTopComponent(topPanel);
        setBottomComponent(bottomPane);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        m_hiliteTags = new JCheckBox("Hilite tags");
        m_hiliteTags.setSelected(HILITE_TAGS);
        m_hiliteTags.addActionListener(new HiliteActionListener());
        controlPanel.add(m_hiliteTags);

        m_tagTypes = new JComboBox();
        Set<String> tagTypes = TagFactory.getInstance().getTagTypes();
        for (String tagType : tagTypes) {
            m_tagTypes.addItem(tagType);
        }
        m_tagTypes.addActionListener(new HiliteActionListener());
        controlPanel.add(m_tagTypes);

        return controlPanel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel();
        Font f = new Font("Verdana", Font.PLAIN, 20);
        label.setFont(f);
        label.setText("Document");
        panel.add(label, BorderLayout.NORTH);

        m_fulltextPane = new JEditorPane();
        m_fulltextPane.setContentType("text/html");
        m_fulltextPane.setText(getPreparedText(m_doc, HILITE_TAGS, ""));
        m_fulltextPane.setEditable(false);
        m_fulltextPane.setCaretPosition(0);

        JScrollPane jsp = new JScrollPane(m_fulltextPane);
        jsp.setPreferredSize(new Dimension(MAX_COLS, MAX_ROWS * 20));
        panel.add(jsp, BorderLayout.CENTER);

        return panel;
    }

    private String getPreparedText(final Document doc, final boolean hiliteTags,
            final String tagType) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<h1>");
        buffer.append(doc.getTitle());
        buffer.append("</h1>");

        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            buffer.append("<br/>");
            buffer.append("<hr>");
            buffer.append("<h3>");
            buffer.append("Section: " + s.getAnnotation().toString());
            buffer.append("</h3>");

            List<Paragraph> paras = s.getParagraphs();
           for (Paragraph p : paras) {
               buffer.append("<p>");
               buffer.append(getParagraphText(p, hiliteTags, tagType));
               buffer.append("</p>");
           }
        }

        return buffer.toString();
    }

    private String getParagraphText(final Paragraph p,
            final boolean hiliteTags, final String tagType) {
        if (!hiliteTags) {
            return p.getText();
        }
        StringBuffer paramStr = new StringBuffer();
        for (Sentence sen : p.getSentences()) {
            for (Term t : sen.getTerms()) {
                boolean hilited = false;
                if (t.getTags().size() > 0) {
                    List<Tag> tags = t.getTags();
                    for (Tag tag : tags) {
                        if (tag.getTagType().equals(tagType)) {
                            paramStr.append("<font color=\"#FF0000\">"
                                    + t.getText() + "</font> ");
                            hilited = true;
                            break;
                        }
                    }
                }
                if (!hilited) {
                    paramStr.append(t.getText() + " ");
                }
            }
        }
        return paramStr.toString();
    }

    private JPanel createAuthorPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Set<Author> authors = m_doc.getAuthors();
        if (authors != null && authors.size() > 0) {

            JLabel label = new JLabel();
            Font f = new Font("Verdana", Font.PLAIN, 20);
            label.setFont(f);
            label.setText("Authors");
            panel.add(label, BorderLayout.NORTH);

            Object[][] names = new Object[authors.size()][2];
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
            JTable table =
                    new JTable(names, new Object[]{"First name", "Last name"});

            JScrollPane jsp = new JScrollPane(table);
            jsp.setPreferredSize(new Dimension(MAX_COLS, MAX_ROWS * 3));
            panel.add(jsp, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel();
        Font f = new Font("Verdana", Font.PLAIN, 20);
        label.setFont(f);
        label.setText("Info");
        panel.add(label, BorderLayout.NORTH);

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

        JTable table =
                new JTable(info, new Object[]{"Info name", "Info value"});

        JScrollPane jsp = new JScrollPane(table);
        jsp.setPreferredSize(new Dimension(MAX_COLS, MAX_ROWS * 3));
        panel.add(jsp, BorderLayout.CENTER);
        return panel;
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
        public void actionPerformed(final ActionEvent arg0) {
            m_fulltextPane.setText(getPreparedText(m_doc,
                    m_hiliteTags.isSelected(),
                    m_tagTypes.getSelectedItem().toString()));
        }
    }
}

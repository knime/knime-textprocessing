/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   27.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.util.ImgLoaderUtil;

/**
 * A panel providing the functionality of displaying a specified set of documents in a table. A double click at a
 * document in a row of that table will trigger the call of the abstract method
 * {@link AbstractDocumentTablePanel2#onClick(int, Document)}. Extending this class and implementing this method allows
 * to react to a double click on a certain document in a particular way.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */

@SuppressWarnings("javadoc")
abstract class AbstractDocumentTablePanel2 extends JPanel implements DocumentProvider {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;

    private JPanel m_searchBoxContainer;

    private JButton m_searchButton, m_resetButton;

    private JTextField m_searchField;

    private JTable m_table;

    private final List<Document> m_docs;

    private final List<Document> m_filterDocs;

    private int m_row = 0;

    private int m_selectedRowIndex = 0;

    private static final String DOCUMENT_ID = "#";

    private static final String DOCUMENT_TITLE = "Document Title";

    private static final String AUTHORS = "Authors";

    private static final String SOURCE = "Source";

    private static final String CATEGORY = "Category";

    private static final String[] m_tableColumns = {DOCUMENT_ID, DOCUMENT_TITLE, AUTHORS, SOURCE, CATEGORY};

    private String[] m_items = {DOCUMENT_TITLE, AUTHORS, SOURCE, CATEGORY};

    private JComboBox<String> m_selection;

    /**
     * Constructor with the given set of documents to display.
     *
     * @param documents The set of documents to display.
     */
    public AbstractDocumentTablePanel2(final List<Document> documents) {
        if (documents == null) {
            m_docs = new ArrayList<Document>(0);
        } else {
            m_docs = documents;
        }

        m_filterDocs = new ArrayList<Document>(m_docs);
        setLayout(new BorderLayout());
        add(initTable(), BorderLayout.CENTER);
    }

    /**
     * Clears the list of documents.
     */
    public void clean() {
        m_docs.clear();
    }

    /**
     * This method is called by a double click on a document of the table. Implementing it allows to react in a certain
     * way on a double click.
     *
     * @param rowIndex The index of the row at which was clicked.
     * @param document The document at which was clicked.
     */
    protected abstract void onClick(final int rowIndex, final Document document);

    private JPanel initTable() {
        SummaryTableListener listener = new SummaryTableListener();

        JPanel panel = new JPanel(new BorderLayout());

        // TABLE
        Object[][] docList = createTableData(m_docs);

        JLabel label = new JLabel("Quick Search: ");
        m_searchField = new JTextField();
        m_searchField.setToolTipText("Enter the search item here...");

        // Combo box list
        m_selection = new JComboBox<String>(m_items);
        m_selection.setToolTipText("Specifiy what to search");
        m_selection.setSelectedIndex(0);
        m_searchButton = new JButton();
        ImageIcon icon = ImgLoaderUtil.loadImageIcon("search.png", "Search");
        m_searchButton.setIcon(icon);
        m_searchButton.setToolTipText("Apply search");
        // The search button is by default disable until the text field is filled
        m_searchButton.setEnabled(false);

        // make sure user enter a search key into the text field, otherwise the search button remain disable
        m_searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(final DocumentEvent e) {
                changed();

            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                changed();

            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                changed();

            }

            public void changed(){
                if(!m_searchField.getText().isEmpty()){
                    m_searchButton.setEnabled(true);
                }else {
                    m_searchButton.setEnabled(false);
                }
            }
        });

        m_searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                performSearch();
            }
        });

        m_resetButton = new JButton();
        icon = ImgLoaderUtil.loadImageIcon("arrow_redo.png", "Search");
        m_resetButton.setIcon(icon);
        m_resetButton.setToolTipText("Reset search results");
        m_resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                resetTable();
                m_searchField.setText("");
            }
        });
        m_resetButton.setVisible(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        m_searchBoxContainer = new JPanel();
        JPanel panelForm = new JPanel(new GridBagLayout());
        m_searchBoxContainer.add(panelForm);

        GridBagConstraints ct = new GridBagConstraints();
        ct.insets = new Insets(0, 2, 0, 2);

        ct.gridx = 0;
        ct.gridy = 0;
        panelForm.add(label, ct);

        ct.gridx = 1;
        ct.gridy = 0;
        m_searchField.setPreferredSize(m_selection.getPreferredSize());
        panelForm.add(m_searchField, ct);
        ct.gridx = 2;
        ct.gridy = 0;

        panelForm.add(m_selection, ct);
        ct.gridx = 3;
        ct.gridy = 0;

        panelForm.add(m_searchButton, ct);
        ct.gridx = 4;
        ct.gridy = 0;

        panelForm.add(m_resetButton);
        ct.gridx = 5;
        ct.gridy = 0;

        mainPanel.add(m_searchBoxContainer, BorderLayout.WEST);

        // The table display result
        m_table = new JTable(docList, new Object[]{DOCUMENT_ID, DOCUMENT_TITLE, AUTHORS, SOURCE, CATEGORY}) {

            /**
             * Automatically generated serial version id.
             */
            private static final long serialVersionUID = -167060303181645711L;

            @Override
            public boolean isCellEditable(final int x, final int y) {
                return false;
            }
        };

        Font headerFont = new Font("sansserif", Font.BOLD, 15);
        m_table.getTableHeader().setFont(headerFont);
        m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_table.addMouseListener(listener);
        m_table.addMouseMotionListener(listener);
        m_table.setOpaque(false);
        m_table.setDefaultRenderer(Object.class, new AttributiveCellRenderer());
        m_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        m_table.setToolTipText("double click to open document");

        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(750);

        JScrollPane jsp = new JScrollPane(m_table);
        jsp.setPreferredSize(new Dimension(850, 600));
        panel.add(jsp, BorderLayout.CENTER);
        panel.add(mainPanel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public Document getDocument(final int index) {
        if (index < m_filterDocs.size() && index >= 0) {
            return m_filterDocs.get(index);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public Document next() {
        int nextIndex = m_selectedRowIndex + 1;
        Document nextDoc = getDocument(nextIndex);
        if (nextDoc != null) {
            m_selectedRowIndex = nextIndex;
            m_table.setRowSelectionInterval(m_selectedRowIndex, m_selectedRowIndex);
        }
        return nextDoc;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public Document previous() {
        Document prevDoc = null;
        int prevIndex = m_selectedRowIndex - 1;
        if (prevIndex >= 0) {
            prevDoc = getDocument(prevIndex);
            if (prevDoc != null) {
                m_selectedRowIndex = prevIndex;
                m_table.setRowSelectionInterval(m_selectedRowIndex, m_selectedRowIndex);
            }
        }
        return prevDoc;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public boolean hasNext() {
        int nextIndex = m_selectedRowIndex + 1;
        Document nextDoc = getDocument(nextIndex);
        if (nextDoc != null) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public boolean hasPrevious() {
        int prevIndex = m_selectedRowIndex - 1;
        if (prevIndex >= 0) {
            Document prevDoc = getDocument(prevIndex);
            if (prevDoc != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public void remove() {
        // Documents are not removed!
    }

    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    private class SummaryTableListener extends MouseAdapter {

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            // if double clicked
            if (e.getClickCount() == 2) {
                m_selectedRowIndex = m_table.getSelectedRow();
                Document doc = m_filterDocs.get(m_selectedRowIndex);
                onClick(m_selectedRowIndex, doc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(final MouseEvent e) {
            JTable aTable = (JTable)e.getSource();
            m_row = aTable.rowAtPoint(e.getPoint());
            aTable.repaint();
        }
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com AG, Zurich
     */
    @SuppressWarnings("serial")
    private class AttributiveCellRenderer extends JLabel implements TableCellRenderer {

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
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {

            if (isSelected) {
                this.setBackground(Color.DARK_GRAY);
                this.setForeground(Color.WHITE);
            } else if (row == m_row) {
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

    /**
     * @author hermann
     * @param searchPattern
     * @param selectedCategory
     * @since 3.1
     */
    private void performSearch() {
        m_filterDocs.clear();
        final String searchPattern = m_searchField.getText();
        final String selectedCategory = m_selection.getSelectedItem().toString();

        // Iterate the category list and chose the one selected by the user
        // In the chosen Category, apply the pattern to match the 'category' against the items
        for (Document d : m_docs) {
            switch (selectedCategory) {
                case DOCUMENT_TITLE:
                    if (searchTitle(d, searchPattern)) {
                        m_filterDocs.add(d);
                    }
                    break;
                case AUTHORS:
                    if (searchAuthor(d, searchPattern)) {
                        m_filterDocs.add(d);
                    }
                    break;
                case SOURCE:
                    if (searchSource(d, searchPattern)) {
                        m_filterDocs.add(d);
                    }
                    break;
                case CATEGORY:
                    if (searchCategory(d, searchPattern)) {
                        m_filterDocs.add(d);
                    }
                    break;

                default:
                    break;
            }
        }

        // Table Results
        Object[][] docListResults = createTableData(m_filterDocs);
        DefaultTableModel model = new DefaultTableModel(docListResults, m_tableColumns);
        updateTableModel(model, true);
    }

    private void updateTableModel(final TableModel model, final boolean showResetButton) {
        m_table.setModel(model);
        m_searchButton.setEnabled(true);
        m_resetButton.setVisible(showResetButton);
        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(750);
    }

    /**
     * @since 3.1
     */
    private void resetTable() {
        m_filterDocs.clear();
        m_filterDocs.addAll(m_docs);

        Object[][] list = createTableData(m_docs);

        DefaultTableModel model = new DefaultTableModel(list, m_tableColumns);
        updateTableModel(model, false);
    }

    private boolean searchTitle(final Document d, final String term) {
        if (d.getTitle().contains(term)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean searchAuthor(final Document d, final String term) {
        Collection<Author> authorList = d.getAuthors();
        for (Author author : authorList) {
            if (author.getFirstName().contains(term) || author.getLastName().contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchSource(final Document d, final String term) {
        Collection<DocumentSource> sourceList = d.getSources();
        for (DocumentSource source : sourceList) {
            if (source.getSourceName().equals(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchCategory(final Document d, final String term) {
        Collection<DocumentCategory> categoryList = d.getCategories();
        for (DocumentCategory cat : categoryList) {
            if (cat.getCategoryName().equals(term)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Diese Methode sollte aus einer gegebene Liste von Dokumente eine Tabelle erstellen
     *
     * @author hermann
     * @param docs
     * @return table
     *
     *
     */
    private Object[][] createTableData(final List<Document> docs) {
        Object[][] table = new Object[docs.size()][5];
        int count = 0;
        // Iterate through the docs
        for (Document doc : docs) {
            table[count][0] = Integer.toString(count + 1);
            table[count][1] = doc.getTitle();

            String authors = "";
            int i = 0;
            for (Author a : doc.getAuthors()) {
                authors += a.getFirstName() + " " + a.getLastName();

                if (i < doc.getAuthors().size() - 1) {
                    authors += ", ";
                }
                i++;
            }
            table[count][2] = authors;

            String sources = "";
            i = 0;
            for (DocumentSource src : doc.getSources()) {
                sources += src.getSourceName();

                if (i < doc.getSources().size() - 1) {
                    sources += ", ";
                }
                i++;
            }
            table[count][3] = sources;

            String categories = "";
            i = 0;
            for (DocumentCategory cat : doc.getCategories()) {
                categories += cat.getCategoryName();

                if (i < doc.getCategories().size() - 1) {
                    categories += ", ";
                }
                i++;
            }
            table[count][4] = categories;

            count++;
        }
        return table;

    }

}

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
 *   27.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import org.knime.ext.textprocessing.data.Document;

/**
 * A panel providing the functionality of displaying a specified set of
 * documents in a table. A double click at a document in a row of that table
 * will trigger the call of the abstract method
 * {@link AbstractDocumentTablePanel#onClick(int, Document)}. Extending
 * this class and implementing this method allows to react to a double click
 * on a certain document in a particular way.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class AbstractDocumentTablePanel extends JPanel
implements DocumentProvider {

    private JTable m_table;

    private List<Document> m_docs;

    private int m_row = 0;

    private int m_selectedRow = 0;

    /**
     * Constructor of <code>AbstractDocumentTablePanel</code> with the given
     * set of documents to display.
     *
     * @param documents The set of documents to display.
     */
    public AbstractDocumentTablePanel(final List<Document> documents) {
        if (documents == null) {
            m_docs = new ArrayList<Document>(0);
        } else {
            m_docs = documents;
        }

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
     * This method is called by a double click on a document of the table.
     * Implementing it allows to react in a certain way on a double click.
     *
     * @param rowIndex The index of the row at which was clicked.
     * @param document The document at which was clicked.
     */
    protected abstract void onClick(final int rowIndex,
            final Document document);

    private JPanel initTable() {
        SummaryTableListener listener = new SummaryTableListener();

        JPanel panel = new JPanel(new BorderLayout());

        // TABLE
        Object[][] docList = new Object[m_docs.size()][2];
        int count = 0;
        for (Document d : m_docs) {
            docList[count][0] = Integer.toString(count + 1);
            docList[count][1] = d.getTitle();
            count++;
        }

        m_table = new JTable(docList, new Object[]{"#", "Document title"}) {
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
        m_table.setDefaultRenderer(Object.class,
                new AttributiveCellRenderer());
        m_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        m_table.setToolTipText("double click to open document");

        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(750);

        JScrollPane jsp = new JScrollPane(m_table);
        jsp.setPreferredSize(new Dimension(850, 600));
        panel.add(jsp, BorderLayout.CENTER);
        return panel;
    }

    /**
     * {@inheritDoc}
     * @since 2.7
     */
    @Override
    public Document getDocument(final int index) {
        if (index < m_docs.size() && index >= 0) {
            return m_docs.get(index);
        }
        return null;
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
                m_selectedRow = m_table.getSelectedRow();
                Document doc = m_docs.get(m_selectedRow);

                onClick(m_selectedRow, doc);
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
    private class AttributiveCellRenderer extends JLabel implements
    TableCellRenderer {

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
}

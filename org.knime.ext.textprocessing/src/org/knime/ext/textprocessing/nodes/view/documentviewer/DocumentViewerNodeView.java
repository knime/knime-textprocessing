/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   27.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeView;
import org.knime.ext.textprocessing.data.Document;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerNodeView extends NodeView<DocumentViewerNodeModel> {

    private JTable m_table;
    
    private List<Document> m_sortedDocs;
    
    public DocumentViewerNodeView(final DocumentViewerNodeModel model) {
        super(model);        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(initTable(), BorderLayout.CENTER);
        setComponent(panel);
    }
    
    private JPanel initTable() {
        m_sortedDocs = new ArrayList<Document>(getNodeModel().getDocuments());
        Collections.sort(m_sortedDocs, new Comparator<Document>() {
            @Override
            public int compare(final Document o1, final Document o2) {
                String title1 = o1.getTitle();
                String title2 = o2.getTitle();
                return title1.compareTo(title2);
            }
        });
        SummaryTableListener listener = new SummaryTableListener();
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel();
        Font f = new Font("Verdana", Font.PLAIN, 20);
        label.setFont(f);
        label.setText("Documents");
        panel.add(label, BorderLayout.NORTH);
        
        Object[][] docList = new Object[m_sortedDocs.size()][1];
        int count = 0;
        for (Document d : m_sortedDocs) {
            docList[count][0] = d.getTitle();
            count++;
        }
        
        m_table = new JTable(docList, new Object[]{"Document title"}) {
            public boolean isCellEditable(int x, int y) {
                return false;
            }
        };
        m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_table.addMouseListener(listener);
        
        JScrollPane jsp = new JScrollPane(m_table);
        jsp.setPreferredSize(new Dimension(500, 600));
        panel.add(jsp, BorderLayout.CENTER);
        return panel;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO Auto-generated method stub

    }
    
    /**
     * 
     * @author Kilian Thiel, University of Konstanz
     */
    class SummaryTableListener extends MouseAdapter {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            // if double clicked
            if (e.getClickCount() == 2) {                
                int rowIndex = m_table.getSelectedRow();
                Document doc = m_sortedDocs.get(rowIndex);
                
                JFrame detailsFrame = new JFrame("Details: " + doc.getTitle());
                if (KNIMEConstants.KNIME16X16 != null) {
                    detailsFrame.setIconImage(
                            KNIMEConstants.KNIME16X16.getImage());
                }
                
                detailsFrame.setContentPane(
                        new DocumentViewPanel(doc));
                detailsFrame.pack();
                detailsFrame.setVisible(true);
            }
        }
    }     
}

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
 *   05.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewPanel extends JSplitPane {

    private static final int MAX_COLS = 500;
    private static final int MAX_ROWS = 30;
    
    private Document m_doc;
    
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
        JPanel mainPanel = createMainPanel();
        JPanel authorPanel = createAuthorPanel();
        JPanel infoPanel = createInfoPanel();
        
        JSplitPane bottomPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomPane.setTopComponent(authorPanel);
        bottomPane.setBottomComponent(infoPanel);
        bottomPane.setResizeWeight(0.5);
        
        setTopComponent(mainPanel);
        setBottomComponent(bottomPane);
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JLabel label = new JLabel();
        Font f = new Font("Verdana", Font.PLAIN, 20);
        label.setFont(f);
        label.setText("Document");
        panel.add(label, BorderLayout.NORTH);
        
        JEditorPane fulltextPane = new JEditorPane();
        fulltextPane.setContentType("text/html");
        fulltextPane.setText(getPreparedText(m_doc));
        fulltextPane.setEditable(false);
        fulltextPane.setCaretPosition(0);

        JScrollPane jsp = new JScrollPane(fulltextPane);
        jsp.setPreferredSize(new Dimension(MAX_COLS, MAX_ROWS * 20));
        panel.add(jsp, BorderLayout.CENTER);
                
        return panel;
    }
    
    private String getPreparedText(final Document doc) {
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
               buffer.append(p.getText());
               buffer.append("</p>");               
           }
        }
        
        return buffer.toString();
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
}

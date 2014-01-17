/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.knime.ext.textprocessing.data.Document;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewPanel extends JSplitPane {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;

    /**
     * The default setting for hiliting of tagged named entities.
     * @since 2.8
     */
    public static final boolean HILITE_TAGS = false;

    /**
     * The default setting for hiliting of search results.
     * @since 2.8
     */
    public static final boolean HILITE_SEARCH = false;

    /**
     * The default entity hilite color.
     * @since 2.8
     */
    public static final Color DEFAULT_ENTITY_COLOR = Color.BLUE;

    private static final int MAX_COLS = 800;

    private static final int MAX_ROWS = 30;


    private final DocumentViewModel m_docViewModel;

    /**
     * Creates new instance of <code>DocumentViewPanel</code> with given
     * document to display.
     *
     * @param doc The document to display.
     * @param docProvider The provider for next or previous documents.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     * @since 2.8
     */
    public DocumentViewPanel(final Document doc, final DocumentProvider docProvider)
                  throws IllegalArgumentException {
        super(JSplitPane.VERTICAL_SPLIT);

        if (doc == null) {
            throw new IllegalArgumentException("Document may not be null!");
        }

        m_docViewModel = new DocumentViewModel(doc, docProvider);

        displayDoc();
    }

    /**
     * Creates new instance of <code>DocumentViewPanel</code> with given
     * document to display.
     *
     * @param doc The document to display.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     */
    public DocumentViewPanel(final Document doc) throws IllegalArgumentException {
        this(doc, null);
    }

    private void displayDoc() {
        JComponent controlPanel = createControlPanel();
        JPanel mainPanel = createMainPanel();

        AuthorTableModel authorTableModel = new AuthorTableModel(m_docViewModel);
        m_docViewModel.addObserver(authorTableModel);
        JPanel authorPanel = new MetaInfoPanel("Authors", authorTableModel, MAX_COLS / 2, MAX_ROWS * 3);

        DocumentInfoTableModel docInfoTableModel = new DocumentInfoTableModel(m_docViewModel);
        m_docViewModel.addObserver(docInfoTableModel);
        JPanel documentInfoPanel = new MetaInfoPanel("Document info", docInfoTableModel, MAX_COLS / 2, MAX_ROWS * 3);

        DocumentMetaInfoTableModel docMetaInfoTableModel = new DocumentMetaInfoTableModel(m_docViewModel);
        m_docViewModel.addObserver(docMetaInfoTableModel);
        JPanel metaInfoPanel = new MetaInfoPanel("Meta info", docMetaInfoTableModel, MAX_COLS, MAX_ROWS * 2);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new GridLayout(2, 1));
        JPanel authorAndDocInfo = new JPanel(new GridLayout(1, 2));
        authorAndDocInfo.add(authorPanel);
        authorAndDocInfo.add(documentInfoPanel);
        bottomPane.add(authorAndDocInfo);
        bottomPane.add(metaInfoPanel);

        setTopComponent(topPanel);
        setBottomComponent(bottomPane);

        setOneTouchExpandable(true);
        setDividerLocation(600);
    }

    private JComponent createControlPanel() {
        return new DocumentControlPanel(m_docViewModel);
    }

    private JPanel createMainPanel() {
        DocumentPanel docPanel = new DocumentPanel(m_docViewModel, MAX_COLS, MAX_ROWS * 20);
        m_docViewModel.addObserver(docPanel);
        return docPanel;
    }
}

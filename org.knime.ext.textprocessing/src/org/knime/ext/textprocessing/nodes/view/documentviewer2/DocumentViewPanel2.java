/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *   05.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.knime.ext.textprocessing.data.Document;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
class DocumentViewPanel2 extends JPanel {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;

    /**
     * The default setting for hiliting of tagged named entities.
     */
    public static final boolean HILITE_TAGS = false;

    /**
     * The default setting for hiliting of search results.
     */
    public static final boolean HILITE_SEARCH = false;

    /**
     * the default setting for displaying tags
     */

    public static final boolean DISPLAY_TAGS = false;

    /**
     * the default setting for disabling html tags
     */
    public static final boolean DISABLE_HTML_TAGS = false;


    /**
     * The default entity hilite color.
     */
    public static final Color DEFAULT_ENTITY_COLOR = Color.BLUE;

    private static final int MAX_COLS = 800;

    private static final int MAX_ROWS = 30;

    private final DocumentViewModel m_docViewModel;

    /**
     * Creates new instance of {@DocumentViewPanel2} with given document to display.
     *
     * @param doc The document to display.
     * @param docProvider The provider for next or previous documents.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     */
    public DocumentViewPanel2(final Document doc, final DocumentProvider docProvider) throws IllegalArgumentException {

        if (doc == null) {
            throw new IllegalArgumentException("Document may not be null!");
        }

        m_docViewModel = new DocumentViewModel(doc, docProvider);

        displayDoc();
    }

    /**
     * Creates new instance of {@DocumentViewPanel2} with given document to display.
     *
     * @param doc The document to display.
     * @throws IllegalArgumentException If given document is <code>null</code>.
     */
    public DocumentViewPanel2(final Document doc) throws IllegalArgumentException {
        this(doc, null);
    }

    private void displayDoc() {
        JPanel mainPanel = createMainPanel();
        setLayout(new BorderLayout());
        JPanel topContainer = new JPanel();
        JComponent leftContent = new DocumentActionPanel(m_docViewModel);
        final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setLeftComponent(leftContent);
        sp.setRightComponent(mainPanel);

        topContainer.add(new DocumentTopPanel(m_docViewModel));

        add(topContainer, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

    }

    private JPanel createMainPanel() {
        DocumentPanel2 docPanel = new DocumentPanel2(m_docViewModel, MAX_COLS, MAX_ROWS * 20);
        m_docViewModel.addObserver(docPanel);
        return docPanel;
    }
}

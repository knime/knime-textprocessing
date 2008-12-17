/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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


import javax.swing.JPanel;

import org.knime.core.node.NodeView;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerNodeView extends NodeView<DocumentViewerNodeModel> {
    
    /**
     * Creates a new instance of <code>DocumentViewerNodeView</code>.
     * 
     * @param model The model holding the documents to dosplay.
     */
    public DocumentViewerNodeView(final DocumentViewerNodeModel model) {
        super(model);
        JPanel panel = new DocumentViewerTablePanel(model.getDocuments());
        setComponent(panel);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // Nothing to do ...
    }
}

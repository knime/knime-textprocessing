/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   20.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.sentenceextraction;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class SentenceExtractionNodeFactory extends 
NodeFactory<SentenceExtractionNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new SentenceExtractionNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SentenceExtractionNodeModel createNodeModel() {
        return new SentenceExtractionNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<SentenceExtractionNodeModel> createNodeView(
            final int viewIndex, final SentenceExtractionNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }
}

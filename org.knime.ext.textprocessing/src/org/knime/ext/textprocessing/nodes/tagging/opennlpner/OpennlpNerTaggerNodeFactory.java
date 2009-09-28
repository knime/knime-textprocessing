/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import org.knime.core.node.AbstractNodeView;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpennlpNerTaggerNodeFactory 
extends NodeFactory<OpennlpNerTaggerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OpenNlpNerNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpennlpNerTaggerNodeModel createNodeModel() {
        return new OpennlpNerTaggerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractNodeView<OpennlpNerTaggerNodeModel> createNodeView(
            final int viewIndex, final OpennlpNerTaggerNodeModel nodeModel) {
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

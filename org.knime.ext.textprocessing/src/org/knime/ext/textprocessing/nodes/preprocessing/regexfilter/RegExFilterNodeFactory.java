/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2011
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
 *   19.02.2010 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.regexfilter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class RegExFilterNodeFactory extends NodeFactory<RegExFilterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new RegExFilterNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegExFilterNodeModel createNodeModel() {
        return new RegExFilterNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<RegExFilterNodeModel> createNodeView(final int viewIndex,
            final RegExFilterNodeModel nodeModel) {
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

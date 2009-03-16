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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringToTermNodeFactory extends NodeFactory<StringToTermNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new StringToTermNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringToTermNodeModel createNodeModel() {
        return new StringToTermNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<StringToTermNodeModel> createNodeView(final int viewIndex, 
            final StringToTermNodeModel nodeModel) {
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

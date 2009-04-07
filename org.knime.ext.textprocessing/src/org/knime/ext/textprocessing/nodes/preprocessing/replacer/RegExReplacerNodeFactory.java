/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
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
 *   24.10.2008 (kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.replacer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 *
 * @author kilian, University of Konstanz
 */
public class RegExReplacerNodeFactory extends
NodeFactory<RegExReplacerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new RegExReplacerNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegExReplacerNodeModel createNodeModel() {
        return new RegExReplacerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<RegExReplacerNodeModel> createNodeView(final int viewIndex,
            final RegExReplacerNodeModel nodeModel) {
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

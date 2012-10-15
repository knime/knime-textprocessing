package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 *
 * @author adae, University of Konstanz
 */
public class StringMatcherNodeFactory
                extends NodeFactory<StringMatcherNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new StringMatcherNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringMatcherNodeModel createNodeModel() {
        return new StringMatcherNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<StringMatcherNodeModel> createNodeView(final int viewIndex,
            final StringMatcherNodeModel nodeModel) {
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

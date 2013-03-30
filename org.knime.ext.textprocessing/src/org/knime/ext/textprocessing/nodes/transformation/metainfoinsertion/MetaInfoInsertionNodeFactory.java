/**
 *
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 */
public class MetaInfoInsertionNodeFactory extends NodeFactory<MetaInfoInsertionNodeModel> {

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeFactory#createNodeModel()
     */
    @Override
    public MetaInfoInsertionNodeModel createNodeModel() {
        return new MetaInfoInsertionNodeModel();
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeFactory#getNrNodeViews()
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeFactory#createNodeView(int, org.knime.core.node.NodeModel)
     */
    @Override
    public NodeView<MetaInfoInsertionNodeModel> createNodeView(final int viewIndex,
                                                               final MetaInfoInsertionNodeModel nodeModel) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeFactory#hasDialog()
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new MetaInfoInsertionNodeDialog();
    }

}

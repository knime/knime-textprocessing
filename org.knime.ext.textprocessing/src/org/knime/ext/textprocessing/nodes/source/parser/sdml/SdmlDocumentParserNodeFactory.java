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
 *   19.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.sdml;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeDialog;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeModel;

/**
 * The {@link org.knime.core.node.NodeFactory} of the Sdml document parser node,
 * provides methods to create the model and the dialog instance.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class SdmlDocumentParserNodeFactory extends 
NodeFactory<DocumentParserNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new DocumentParserNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentParserNodeModel createNodeModel() {
        return new DocumentParserNodeModel(
                new SdmlDocumentParser(), "xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DocumentParserNodeModel> createNodeView(final int index, 
            final DocumentParserNodeModel model) {
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

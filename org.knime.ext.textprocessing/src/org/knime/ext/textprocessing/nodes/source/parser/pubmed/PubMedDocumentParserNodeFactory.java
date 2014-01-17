/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   20.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.pubmed;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeDialog;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeModel;

/**
 * The {@link org.knime.core.node.NodeFactory} of the PubMed document parser node provides methods to create the model
 * and the dialog instance.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class PubMedDocumentParserNodeFactory extends NodeFactory<DocumentParserNodeModel> {

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
        return new DocumentParserNodeModel(new PubMedDocumentParserFactory(), false, "xml", "gz", "zip");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DocumentParserNodeModel> createNodeView(final int index, final DocumentParserNodeModel model) {
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

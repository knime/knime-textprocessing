/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
package org.knime.ext.textprocessing.nodes.source.parser.flatfile;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.ext.textprocessing.nodes.source.parser.CharsetDocumentParserNodeDialog;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeModel;

/**
 * The {@link org.knime.core.node.NodeFactory} of the flat file document
 * parser node, provides methods to create the model and the dialog instance.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class FlatFileDocumentParserNodeFactory extends
NodeFactory<DocumentParserNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new CharsetDocumentParserNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentParserNodeModel createNodeModel() {
        return new DocumentParserNodeModel(
                new FlatFileDocumentParser());
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

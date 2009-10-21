/*
========================================================================
 *
 *  Copyright (C) 2003 - 2009
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
 * -------------------------------------------------------------------
 *
 * History
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorNodeFactory
    extends NodeFactory<DocumentDataExtractorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected DocumentDataExtractorNodeDialog createNodeDialogPane() {
        return new DocumentDataExtractorNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentDataExtractorNodeModel createNodeModel() {
        return new DocumentDataExtractorNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DocumentDataExtractorNodeModel> createNodeView(
            final int viewIndex, 
            final DocumentDataExtractorNodeModel nodeModel) {
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

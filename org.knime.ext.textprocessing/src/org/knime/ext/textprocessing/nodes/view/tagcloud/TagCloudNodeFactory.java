/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   15.11.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * The factory of the tag cloud node.
 *
 * @author Iris Adae, University of Konstanz
 */
public class TagCloudNodeFactory extends NodeFactory<TagCloudNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new TagCloudNodeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagCloudNodeModel createNodeModel() {
        return new TagCloudNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<TagCloudNodeModel> createNodeView(final int index,
            final TagCloudNodeModel model) {
        return new TagCloudNodeView(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

}

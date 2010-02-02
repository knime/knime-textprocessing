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
 *   15.11.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud;

import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;


/**
 * The nodeview of the tag cloud node.
 *
 * The plotter is registered.
 *
 * @author Iris Adae, University of Konstanz
 */
public class TagCloudNodeView extends NodeView<TagCloudNodeModel>  {

    private TagCloudViewPlotter m_plotter;

     /**
      * Constructor.
     * @param tagModel  the {@link TagCloudNodeModel}.
     */
    public TagCloudNodeView(final TagCloudNodeModel tagModel) {
        super(tagModel);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
            final TagCloudNodeModel model = getNodeModel();
            if (model == null || model.getTagCloud() == null) {
                return;
            }
            if (m_plotter == null) {
                m_plotter = new TagCloudViewPlotter();
                getJMenuBar().add(m_plotter.getHiLiteMenu());
            }
            HiLiteHandler hilit = model.getInHiLiteHandler(0);
            m_plotter.setHiLiteHandler(hilit);

            model.getTagCloud().hiLite(hilit.getHiLitKeys());
            m_plotter.setTagCloudModel(model.getTagCloud());
            m_plotter.updatePaintModel();

            setComponent(m_plotter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        //nothing to do as we want to keep the last made configurations.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        //nothing to do
    }
}

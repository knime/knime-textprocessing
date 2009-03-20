/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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

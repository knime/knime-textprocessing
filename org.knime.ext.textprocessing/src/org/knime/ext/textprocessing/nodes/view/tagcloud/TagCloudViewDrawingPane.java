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
 *   02.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import org.knime.base.node.viz.plotter.AbstractDrawingPane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;

/**
 * The drawing pane of the tag cloud.
 *
 * The data of the tag cloud is painted, using the information stored in
 * the {@link TagCloud} object.
 *
 * @author Iris Adae, University of Konstanz
 */
public class TagCloudViewDrawingPane extends AbstractDrawingPane {

   /**
     * the serial version UID.
     */
    private static final long serialVersionUID = 1L;

   /**
    * the TagCloud object contains all data necessary to paint the cloud.
    */
    private  TagCloud m_tagcloud;

   /**
    * {@inheritDoc}
    */
   @Override
   public void paintContent(final Graphics g) {
           Graphics2D g2 = (Graphics2D)g;

         Font myfont = new Font(m_tagcloud.getfontName(),
                 Font.PLAIN, m_tagcloud.getmaxFontsize());
         TagCloudData[] points =  m_tagcloud.getDataArray();

         // through all points
         for (TagCloudData tcd : points) {
             myfont = myfont.deriveFont(Font.PLAIN, tcd.getFontsize());

             g2.setColor(tcd.getTextcolor());
             g2.setFont(myfont);

             if (tcd.isSelected()) {        // is the term is currently selec
                                             //ted it will be in italic
                 myfont =  myfont.deriveFont(Font.ITALIC);
              }
             if (tcd.isBold()) {
                myfont = myfont.deriveFont(Font.BOLD);
             }
              TextLayout tl = new TextLayout(tcd.getTerm().getText(), myfont,
                      g2.getFontRenderContext());
              tl.draw(g2, (float)tcd.getX(),
                    (float)(tcd.getY() + tcd.getHeight() * 0.8));
         }
       }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        if (m_tagcloud != null) {
            return m_tagcloud.getPreferredSize();
        }
        return super.getPreferredSize();
    }

    /** Called when the tag cloud has changed.
     *
     * @param tagcloud new tagcloud
     */
    public void modelChanged(final TagCloud tagcloud) {
        if (tagcloud != null) {
            m_tagcloud = tagcloud;
        }
        repaint();
        revalidate();
    }
}

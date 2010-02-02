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
 *   02.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import org.knime.base.node.viz.plotter.AbstractPlotterProperties;
import org.knime.base.node.viz.plotter.props.ColorLegendTab;

import java.awt.Color;
import java.util.Map;

/**
 * The properties contains element to interact with the view.
 *
 * For the tag cloud are in addition to the standard tab, two more
 * tabs implemented.
 *
 * The <code>ColorLegendTab</code> is used to change the colors which are
 * connected with the tags.
 * This tab is only shown, if the colors weren't fixed before. E.g. with a
 * color appender.
 *
 * The <code>TagCloudFontStyleTab</code> contains routines to change the look
 * of the font. The name, the minimal/maximal size and the distribution
 * of the font can be changed.
 * Also it includes two sliders for adjusting the amount of bold terms (the top
 * terms) and for
 *
 * @author Iris Adae, University of Konstanz
 */
public class TagCloudViewPlotterProperties extends AbstractPlotterProperties {

    /**
     * the serial version UID.
     */
    private static final long serialVersionUID = -4725066806014923219L;
    /**
     * Reference to the associated <code>ColorLegendTab</code>.
     */
    private ColorLegendTab m_colorLegend;
    /**
     * Reference to the associated <code>TagCloudFontStyleTab</code>.
     */
    private TagCloudFontStyleTab m_fontStyle;

    /**
     * creates a new properties object.
     */
    public TagCloudViewPlotterProperties() {
        super();
        //insert the color selection Tab
        m_colorLegend = new ColorLegendTab();
        addTab(m_colorLegend.getDefaultName(), m_colorLegend);

        // insert the font selection Tab
        m_fontStyle = new TagCloudFontStyleTab();
        addTab(m_fontStyle.getDefaultName(), m_fontStyle);
    }



    /**
     * Updates the colorLegend.
     * Updates the values in the <code>ColorLegendTab</code>.
     *
     * @param colorMapping a mapping between the type (as String),
     * and the color
     */
    public void updateColorLegend(final Map<String, Color>colorMapping) {
        if (colorMapping.isEmpty()) {
            removeTabAt(1);
        } else {
            m_colorLegend.update(colorMapping);
        }
    }

    /**
     * Updates the <code>fontStyleTab</code>.
     * @param min Minimal font size
     * @param max Maximal font size
     * @param font Name of the font
     * @param calcID calctype or 0
     * @param bold new bold value
     * @param alpha new alpha value
     */
    public void updateFontStyle(final int min,
                                final int max,
                                final String font,
                                final int calcID,
                                final int bold,
                                final int alpha) {
         m_fontStyle.update(min, max, font, calcID, bold, alpha);
    }

    /**
     *
     * @return the color legend.
     */
    public ColorLegendTab getColorLegend() {
        return m_colorLegend;
    }

    /**
    *
    * @return the fontstyle object
    */
   public TagCloudFontStyleTab getFontStyleTab() {
       return m_fontStyle;
   }
}

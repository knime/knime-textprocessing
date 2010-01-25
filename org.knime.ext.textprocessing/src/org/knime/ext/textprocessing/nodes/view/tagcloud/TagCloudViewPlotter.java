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

import org.knime.base.node.viz.plotter.AbstractDrawingPane;
import org.knime.base.node.viz.plotter.AbstractPlotter;
import org.knime.base.node.viz.plotter.AbstractPlotterProperties;
import org.knime.base.node.viz.plotter.props.ColorLegendTab;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.KeyEvent;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The plotter for the tag cloud.
 *
 * It provides functionality for updating the width, selecting elements,
 * highlighting elements, zooming and fitting to screen.
 *
 * @author Iris Adae, University of Konstanz
 */
public class TagCloudViewPlotter extends AbstractPlotter {

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -1902514009160242469L;

    private TagCloud m_tagcloud;

    private TagCloudFontStyleTab m_fs;


    /** Constructor for class {@link TagCloudViewPlotter}.
     * @param drawingPane the drawingPane of the plotter.
     * @param properties the properties of the plotter.
     */
    public TagCloudViewPlotter(final AbstractDrawingPane drawingPane,
            final AbstractPlotterProperties properties) {
        super(drawingPane, properties);
        if (getProperties() instanceof TagCloudViewPlotterProperties) {

            m_fs = ((TagCloudViewPlotterProperties)getProperties())
                    .getFontStyleTab();

            ((TagCloudViewPlotterProperties)getProperties())
                    .getFitToScreenButton().addActionListener(
                            new ActionListener() {
                                public void actionPerformed(
                                        final ActionEvent arg0) {
                                    fitTomyScreen();
                                }
                            });

            final ColorLegendTab legend = 
              ((TagCloudViewPlotterProperties)getProperties()).getColorLegend();

            legend.addChangeListener(new ChangeListener() {
                /**
                 * {@inheritDoc}
                 */
                public void stateChanged(final ChangeEvent e) {
                    m_tagcloud.setColorMap(legend.getColorMapping());
                    updatePaintModel();
                }
            });

            m_fs.addReverseChangeListener(new ChangeListener() {
                // is called when the user wants the default values
                public void stateChanged(final ChangeEvent e) {

                    updateSize();
                    m_tagcloud.restore();
                    m_fs.setAll(m_tagcloud.getminFontsize(), m_tagcloud
                            .getmaxFontsize(), m_tagcloud.getfontName(),
                            m_tagcloud.getCalcType(), m_tagcloud.getBold(),
                            m_tagcloud.getAlpha());
                    updatePaintModel();
                }
            });

            m_fs.addChangeListener(new ChangeListener() {
                // is called when the font style was changed in the tab.
                public void stateChanged(final ChangeEvent e) {

                    updateSize();
                    m_tagcloud.changeFontsizes(m_fs.getMinFontsize(), m_fs
                            .getMaxFontsize(), m_fs.getFontName(), m_fs
                            .getCalcType(), m_fs.getbold());
                    m_tagcloud.changealpha(m_fs.getAlpha());
                    updatePaintModel();
                }
            });
        }
    }

    /**
     * Changes the tag cloud to the given one.
     *
     * @param tc the new {@link TagCloud}.
     */
    public void setTagCloudModel(final TagCloud tc) {
        m_tagcloud = tc;
        if (getProperties() instanceof TagCloudViewPlotterProperties) {

            ((TagCloudViewPlotterProperties)getProperties())
            .updateColorLegend(m_tagcloud.getColorMap());

            ((TagCloudViewPlotterProperties)getProperties())
            .updateFontStyle(m_tagcloud.getminFontsize(),
                    m_tagcloud.getmaxFontsize() ,
                    m_tagcloud.getfontName(),
                    m_tagcloud.getCalcType(),
                    m_tagcloud.getBold(),
                    m_tagcloud.getAlpha());
            
            updatePaintModel();
        }
    }

    /** standard constructor.
     * creates a new Object
     */
    public TagCloudViewPlotter() {
        this(new TagCloudViewDrawingPane(),
                new TagCloudViewPlotterProperties());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSelection() {
        m_tagcloud.clearSelection();
        updatePaintModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hiLite(final KeyEvent event) {
        m_tagcloud.hiLite(event);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hiLiteSelected() {
        Set<RowKey> s =  m_tagcloud.hiLiteSelected(true);
        delegateHiLite(s);

        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_tagcloud.restore();
        updatePaintModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectClickedElement(final Point clicked) {
        m_tagcloud.selectClickedElement(clicked);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectElementsIn(final Rectangle selectionRectangle) {
        m_tagcloud.selectElementsIn(selectionRectangle);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unHiLite(final KeyEvent event) {
        m_tagcloud.unHiLite(event);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unHiLiteSelected() {
        Set<RowKey> s = m_tagcloud.hiLiteSelected(false);
        delegateUnHiLite(s);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePaintModel() {
        ((TagCloudViewDrawingPane)getDrawingPane()).modelChanged(m_tagcloud);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSize() {
       int height = m_tagcloud
                           .changeWidth(
                                   (getDrawingPaneDimension().width));
       if (height > 0) {
            updateAxisLength();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unHiLiteAll(final KeyEvent event) {
        m_tagcloud.unHiLiteAll(event);
        repaint();
    }

    /**
     * fits the tag cloud into the current screen.
     */
    public void fitTomyScreen() {
        m_tagcloud.fittoscreen(getDrawingPaneDimension());

        int min = m_tagcloud.getminFontsize();
        int max = m_tagcloud.getmaxFontsize();
        if ((min != m_fs.getMinFontsize())
                || (max != m_fs.getMaxFontsize())) {
                m_fs.setMinMaxFonsize(min, max);
                m_tagcloud.changeFontsizes(m_fs.getMinFontsize(),
                            m_fs.getMaxFontsize(),
                            m_fs.getFontName(),
                            m_fs.getCalcType(),
                            m_fs.getbold());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void zoomByClick(final Point clicked) {
        super.zoomByClick(clicked);
        m_tagcloud.zoombyfactor(DEFAULT_ZOOM_FACTOR);
        m_fs.setMinMaxFonsize(m_tagcloud.getminFontsize(),
                m_tagcloud.getmaxFontsize());
        repaint();
        revalidate();
    }
}

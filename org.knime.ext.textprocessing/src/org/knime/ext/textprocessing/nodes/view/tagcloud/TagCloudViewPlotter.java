/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   02.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.viz.plotter.AbstractDrawingPane;
import org.knime.base.node.viz.plotter.AbstractPlotter;
import org.knime.base.node.viz.plotter.AbstractPlotterProperties;
import org.knime.base.node.viz.plotter.props.ColorLegendTab;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.KeyEvent;

/**
 * The plotter for the tag cloud.
 *
 * It provides functionality for updating the width, selecting elements,
 * highlighting elements, zooming and fitting to screen.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
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
                                @Override
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
                @Override
                public void stateChanged(final ChangeEvent e) {
                    m_tagcloud.setColorMap(legend.getColorMapping());
                    updatePaintModel();
                }
            });

            m_fs.addReverseChangeListener(new ChangeListener() {
                // is called when the user wants the default values
                @Override
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
                @Override
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
    @Override
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

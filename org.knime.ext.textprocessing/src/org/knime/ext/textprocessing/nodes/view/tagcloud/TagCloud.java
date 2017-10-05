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
 *   09.09.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;

import org.knime.base.node.util.DataArray;
import org.knime.core.data.RowKey;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Term;

/**
 * Contains all methods to create a basic tag cloud and stores the
 * calculated solution.
 *
 * A basic tag cloud is either a (sorted) table or a circular version
 * as proposed by Seifert et al. in their paper On the beauty and usability
 * of tag clouds.
 *
 * @see AbstractTagCloud
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
public class TagCloud extends AbstractTagCloud<TagCloudData> {
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 8141006637646515497L;

    /** horizontal position of the next term. */
    private int m_nextposx;

    /** vertical position of the next line. */
    private int m_nextposy;

    /** Height of the current line. */
    private int m_nextysize;


    /** stores the kind of tagcloud.
     * as, defined in CFG_TYPEOFTCCALCI[].
     */
    private int m_typeoftagcloud;

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(TagCloud.class);

    /**
     * initializes the TagCloud.
     */
    TagCloud() {
        super();
        m_typeoftagcloud = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
       setMinMaxFontsize(10, 50);
       super.restore();

    }


    /**
     * Creates a Table setting the most Important Words in the Middle and the
     * others around. If the <code>Dimension</code> is too small to fit all
     * labels inside, the method uses a heuristic, which enlarges the
     * <code>Dimension</code> until all labels could be painted.
     *
     * @param drawingPaneDimension the desired <code>Dimension</code> of the
     *            tag cloud. Due to the label sizes the calculated tag cloud
     *            will be at least the given size but can be larger.
     */
    private void createInsideOutTable(final Dimension drawingPaneDimension) {
        double w = Math.min(Double.MAX_VALUE,
                drawingPaneDimension.getWidth()) - 2 * BOUNDLABEL;
        double h = Math.min(Double.MAX_VALUE, drawingPaneDimension.getHeight());

        boolean stop = false;
         while (!stop) {
             stop = createInsideOutTable(w, h);
             h *= 1.1;
             w *= 1.1;
         }
    }

    /**
     * Paints a TagCloud.
     *
     * All needed information will be gained from the given
     * <code>TagCloudNodeModel</code>.
     *
     * @param totalexec the current <code>ExecutionContext</code> needed to
     *            check for cancel actions made by the user during the
     *            execution.
     * @param tagModel containing necessary data information
     * @throws CanceledExecutionException If execution was canceled by the user.
     */
    public void createTagCloud(final ExecutionContext totalexec,
            final TagCloudNodeModel tagModel)
                        throws CanceledExecutionException {
        final int tablewidth = 1000;

        totalexec.setProgress(0, "Starting TagCloud Calculation");

        /** the Data is read and the minimal an maximal is determined */
        initMinMaxandData((DataArray)tagModel.getData(), tagModel.ignoreTags(),
                tagModel.getTermCol(), tagModel.getValueCol());

        totalexec.checkCanceled();
        initAllLabels(System.currentTimeMillis());

        final ExecutionMonitor exec = totalexec.createSubProgress(0.9);
        final String calctype = tagModel.getTCcalcType();

        exec.setProgress(0.5, "Calculating Table");
        totalexec.checkCanceled();

        // SimpleTable
        if (calctype.equals(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[0])) {
            createArray(getDataMap().keySet().iterator());
            m_typeoftagcloud = 0;
            createTable(tablewidth);
            // DictionarysortedTable
        } else if (calctype.equals(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[1])) {
            createArray(
                    TagCloudGeneral.getsortedAlphabeticalIterator(
                            getDataMap().keySet()));
            m_typeoftagcloud = 1;
            createTable(tablewidth);
            // FontsizesortedTable
        } else if (calctype.equals(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[2])) {
            createArray(TagCloudGeneral.getsortedFontsizeIterator(getDataMap(),
                    false));
            m_typeoftagcloud = 2;
            createTable(tablewidth);
            // InsideOutTable
        } else if (calctype.equals(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[3])) {
            createArray(TagCloudGeneral
                    .getsortedFontsizeIterator(getDataMap(), true));
            m_typeoftagcloud = 3;
            createInsideOutTable(getPreferredSize());
        }

        totalexec.setProgress(0.9, "Creating positions");
        /** the tag cloud is going to be painted */
        setlabelsontheiplaces();

        changealpha(DEFAULT_ALPHA);
    }

    /**
     * Paints a TagCloud.
     *
     * All needed information will be gained from the given
     * <code>TagCloudNodeModel</code>.
     *
     * @param tagModel containing necessary data information
     */
    public void createTagCloud(final TagCloudNodeModel tagModel) {
        try {
            createTagCloud(null, tagModel);
        } catch (CanceledExecutionException io) {
            // will never happen as the executioncontext is null
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createArray(final Iterator<Term> it) {
        TagCloudData[] dataarray = new TagCloudData[getDataMap().size()];
        int i = 0;
        while (it.hasNext()) {
            dataarray[i] = getDataMap().get(it.next());
            i++;
        }
        setDataArray(dataarray);
        rebuiltHiliteTable();
    }




    /** creates a table tag cloud.
     *
     * @param maxwidth the maximal width of the table
     */
    private void createTable(final int maxwidth) {
        setWidth(getPreferredSize().width - 2 * BOUNDLABEL);
        m_nextposx = BOUNDLABEL;
        m_nextposy = BOUNDLABEL;
        m_nextysize = 0;
        TagCloudData[] dataarray = getDataArray();
        for (int j = 0; j < dataarray.length; j++) {
            setTableTagCloudPositions(dataarray[j], maxwidth);
        }
    }

    /**
     * The class is needed for the inside out routine.
     *
     * It stores to each new found white space in the view
     * the position, of the label.
     *
     * @author Iris Adae, University of Konstanz
     */
    class InsidePlaces {
        /**indicates where in the rectangle the label should be placed. */
       private Placetype m_type;

        /** contains some white space.*/
       private Rectangle2D.Double m_place;

        /** Standard constructor.
         * @param t the new type for the saved rectangle
         * @param rect contains some white space
         *
         */
        public InsidePlaces(final Placetype t,
                            final Rectangle2D.Double rect) {
            m_type = t;
            m_place = rect;
        }
        /**
         * @return the placetype
         */
        public Placetype gettype() {
            return m_type;
        }
        /**
         * @return the rectangle
         */
        public Rectangle2D.Double getrectangle() {
            return m_place;
        }
    }

    /**
     * The four type of places are used for the class
     * <code>InsidePlaces</code>.
     *
     * @author Iris Adae, University of Konstanz
     */
    private enum Placetype {
        /** will be placed on top of the current position. */
        top,
        /** will be placed on bottom of the current position. */
         bottom,
         /** will be placed left of the current position. */
          left,
          /** will be placed right of the current position. */
        right
    }

    /**
     * Creates a new tagcloud view. The most important words, will be shown in
     * the middle of the cloud and the others around.
     *
     * @param width the preferred maximum width of the tagcloud
     * @param height the preferred maximum height of the tagcloud
     * @return true if the data could be fit inside the given rectangle,
     *         otherwise false
     */
    private boolean createInsideOutTable(final double width,
            final double height) {
        LinkedList<InsidePlaces> placequ = new LinkedList<InsidePlaces>();
        boolean lastlabelfoundaplace = true;
        double w = width * 0.5;
        double h = height * 0.5;
        double dataheight = 0, datawidth = 0;
        TagCloudData[] dataarray = getDataArray();

        if (dataarray.length > 0) {
            TagCloudData tcd = dataarray[0];
            dataheight = tcd.getHeight();
            datawidth = tcd.getWidth() + BOUNDLABEL;
            if (w * 2 > datawidth && h * 2 > dataheight) {
                tcd.setXY(0, 0);
                // the area above will be inserted
                placequ.addLast(new InsidePlaces(Placetype.top,
                        new Rectangle2D.Double(tcd.getWidth() / 2 - w,
                                (-1) * h, 2 * w, h)));

                // the area in the right will be inserted
                placequ.add(new InsidePlaces(Placetype.right,
                        new Rectangle2D.Double(tcd.getWidth(), 0, w - datawidth
                                / 2, tcd.getHeight())));

                // the area below will be inserted
                placequ.addLast(new InsidePlaces(Placetype.bottom,
                        new Rectangle2D.Double(datawidth / 2 - w, tcd
                                .getHeight(), 2 * w, h - tcd.getHeight())));

                // the area left of the label will be inserted
                placequ.addLast(new InsidePlaces(Placetype.left,
                        new Rectangle2D.Double(datawidth / 2 - w, 0, w
                                - datawidth / 2, tcd.getHeight())));
            } else {
                // if the first term already was to big, the method ends.
                lastlabelfoundaplace = false;
            }
        }
        // as the four initializations are done, we are now going to place
        // all other labels
        for (int count = 1; count < dataarray.length && lastlabelfoundaplace;
        count++) {
            TagCloudData tcd = dataarray[count];
            dataheight = tcd.getHeight();
            // to get a nicer view, we put some free space
            // between two term.
            datawidth = tcd.getWidth() + BOUNDLABEL;
            InsidePlaces now;
            Rectangle2D.Double rect = new Rectangle2D.Double();
            LinkedList<InsidePlaces> zwischenspeicher =
                new LinkedList<InsidePlaces>();
            lastlabelfoundaplace = false;

            while ((!lastlabelfoundaplace) && !(placequ.isEmpty())) {
                // searching the first space into which our label fits.
                now = placequ.removeFirst();
                zwischenspeicher.addLast(now);
                rect = now.getrectangle();
                lastlabelfoundaplace = tcd.fitsin(rect);
            }

            if (lastlabelfoundaplace) { // if there was a space
                // we know use the placetype to goon further

                now = zwischenspeicher.removeLast();
                placequ.addAll(zwischenspeicher);

                switch (now.gettype()) {

                case top:
                    // we found a top type. So the label will be placed on
                    // the center of the ground of the space.
                    tcd.setXY(rect.x + (rect.width / 2) - (datawidth / 2),
                            rect.y + rect.height - tcd.getHeight());

                    // we know slice the area into three parts. left, top and
                    // right of the newly insert label.
                    placequ.addLast((new InsidePlaces(Placetype.left,
                            new Rectangle2D.Double(rect.x, tcd.getY(), tcd
                                    .getX()
                                    - rect.x, tcd.getHeight()))));
                    placequ.addLast((new InsidePlaces(Placetype.top,
                            new Rectangle2D.Double(rect.x, rect.y, rect.width,
                                    rect.height - tcd.getHeight()))));

                    placequ.addLast((new InsidePlaces(Placetype.right,
                            new Rectangle2D.Double(tcd.getX() + datawidth, tcd
                                    .getY(),
                                    (rect.width / 2) - (datawidth / 2), tcd
                                            .getHeight()))));
                    break;
                case right:
                    tcd.setXY(rect.x, rect.y);
                    // we now slice the area into two parts. below and
                    // right of the newly insert label.

                    placequ.addLast((new InsidePlaces(Placetype.right,
                            new Rectangle2D.Double(rect.x + datawidth, rect.y,
                                    rect.width - datawidth, tcd.getHeight()))));
                    // below the label is a placetype right. (A drawing will
                    // show the cause)
                    placequ.addLast((new InsidePlaces(Placetype.right,
                            new Rectangle2D.Double(rect.x, rect.y
                                    + tcd.getHeight(), rect.width, rect.height
                                    - tcd.getHeight()))));
                    break;
                case bottom:
                    tcd.setXY(rect.x + (rect.width / 2) - (datawidth / 2),
                            rect.y);
                    // we know slice the area into three parts. below, right and
                    // left of the newly insert label.

                    placequ.addLast((new InsidePlaces(Placetype.right,
                            new Rectangle2D.Double(tcd.getX() + datawidth,
                                    rect.y, rect.width / 2 - datawidth / 2, tcd
                                            .getHeight()))));
                    placequ.addLast((new InsidePlaces(Placetype.bottom,
                            new Rectangle2D.Double(rect.x, rect.y
                                    + tcd.getHeight(), rect.width, rect.height
                                    - tcd.getHeight()))));
                    placequ.addLast((new InsidePlaces(Placetype.left,
                            new Rectangle2D.Double(rect.x, rect.y,
                                    (rect.width / 2) - (datawidth / 2), tcd
                                            .getHeight()))));
                    break;
                case left:
                    tcd.setXY(rect.x + rect.width - datawidth, rect.y);
                    // again slicing into two (left and above)
                    placequ.addLast((new InsidePlaces(Placetype.left,
                            new Rectangle2D.Double(rect.x, rect.y
                                    + tcd.getHeight(), rect.width, rect.height
                                    - tcd.getHeight()))));
                    placequ.addLast((new InsidePlaces(Placetype.left,
                            new Rectangle2D.Double(rect.x, rect.y, rect.width
                                    - datawidth, tcd.getHeight()))));
                    break;
                }
            }
        }
        return lastlabelfoundaplace;
    }


    /**
     * Places the given <code>TagCloudData</code> to the next free table
     * position.
     *
     * @param tcd next Object to be placed
     * @param maxwidth width of the table
     */
    private void setTableTagCloudPositions(final TagCloudData tcd,
            final int maxwidth) {
        if (m_nextposx + tcd.getWidth() > maxwidth) {
            m_nextposx = BOUNDLABEL;
            m_nextposy += m_nextysize;
            m_nextysize = 0;
        }
        tcd.setXY(m_nextposx, m_nextposy);

        m_nextposx += tcd.getWidth() + BOUNDLABEL;
        if (m_nextysize < tcd.getHeight()) {
            m_nextysize = (int)tcd.getHeight();
        }

    }

    /**
     * {@inheritDoc}
     * If this method is called the whole positioning of the tag cloud will be
     * done.
     */
    @Override
    public void recreateTagCloud() {
        // Table
        if (m_typeoftagcloud < 3) {
            createTable(getPreferredSize().width);

            // InsideOutTable
        } else if (m_typeoftagcloud >= 3) {
            createInsideOutTable(getPreferredSize());
        }

        /** the tag cloud is going to be painted */
        setlabelsontheiplaces();
    }

    /**Changes the width of the tagcloud.
     *
     * @param width the new Width of the tagcloud
     * @return the height of the panel if the width has to be changed,
     * otherwise -1
     */
    @Override
    public int changeWidth(final int width) {
        if (width == getPreferredSize().width) {
            return -1;
        }
        setWidth(width);
        recreateTagCloud();
        return getPreferredSize().height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagCloudData createTagCloudData(final Term term, final double i,
            final RowKey rowk) {
        return new TagCloudData(term, i, rowk);
    }

    /** fits the view inside the screen window.
     *
     * @param drawingPaneDimension the new dimension for the view
     */
    public void fittoscreen(final Dimension drawingPaneDimension) {
        double fontSizeScalingFactor =
                (drawingPaneDimension.getWidth() * drawingPaneDimension
                        .getHeight())
                        / (getPreferredSize().getWidth() * getPreferredSize()
                                .getHeight());

        double maxiterations = 20;
        /** the width is set to the width of the given dimension*/

        double[] futureminmax = new double[2];
        futureminmax[0] = getminFontsize();
        futureminmax[1] = getmaxFontsize();

        double[] futureminmaxhigh = new double[2];
        futureminmaxhigh[0] =
                ((fontSizeScalingFactor - 1) * getminFontsize()) / 3;
        futureminmaxhigh[1] =
                ((fontSizeScalingFactor - 1) * getmaxFontsize()) / 3;

        int i = 0;
        /**
         * indicates a good Configuration has been found, the iteration will
         * stop
         */
        boolean fitsin = false;
        /**
         * indicates if the calculated view is too big (true) or too small
         * (false)
         */
        boolean bigger = fontSizeScalingFactor > 1 ? true : false;
        while (!fitsin && i <= maxiterations) {

            /** reduce factor to just a partial adjusting */
            if ((drawingPaneDimension.getWidth()
                    * drawingPaneDimension.getHeight())
                    < (getPreferredSize().getWidth()
                            * getPreferredSize().getHeight())) {
                bigger = true;
            } else if (0.95 * (drawingPaneDimension.getWidth()
                    * drawingPaneDimension.getHeight())
                    > (getPreferredSize().getWidth()
                      * getPreferredSize().getHeight())) {
                bigger = false;

            } else {
                fitsin = true;
            }
            if ((bigger && futureminmaxhigh[i % 2] > 0)
                    || (!bigger && futureminmaxhigh[i % 2] < 0)) {
                // in these
                // cases we adjusted the font sizes too much,  and have
                // to go  back a little bit
                futureminmaxhigh[i % 2] *= -0.5;
            }
            if (Math.abs(futureminmaxhigh[0]) < 0.1
                    && Math.abs(futureminmaxhigh[1]) < 0.1) {
                // if both adjustments are such small, we found a good solution
                // and going to end.
                fitsin = true;
            }
            if (!fitsin && Math.abs(futureminmaxhigh[i % 2]) >= 0.1) {
                futureminmax[i % 2] += futureminmaxhigh[i % 2];
                setWidth((int)drawingPaneDimension.getWidth());
                changeFontsizes((int)(Math.round(futureminmax[0])), (int)(Math
                        .round(futureminmax[1])));
            }
            i++;
            if (i % 2 == 0) {
                LOGGER.debug("Fontsizes were adjusted to min: "
                        + futureminmax[0] + " and max: " + futureminmax[1]);
            }
        }
        if (i % 2 == 1) {
            LOGGER.debug("Fontsizes were adjusted to min: " + futureminmax[0]
                    + " and max: " + futureminmax[1]);
        }
    }

    /**
     * Increases the font sizes with the given factor.
     *
     * @param scalingFactor the factor to increase
     */
    public void zoombyfactor(final double scalingFactor) {
        LOGGER.debug("The fontsize will be increased with "
                + scalingFactor
                + ".");
        changeFontsizes((int)(scalingFactor * getminFontsize()),
                (int)(scalingFactor * getmaxFontsize()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFrom(final ModelContentRO modelContent)
    throws InvalidSettingsException {
        m_typeoftagcloud = 0;
        int size = 0;
        try {
                m_typeoftagcloud = modelContent.getInt(CFG_LS_KIND);
                size = modelContent.getInt(CFG_LS_DATASIZE);
        } catch (InvalidSettingsException e1) {
            InvalidSettingsException ioe =
                    new InvalidSettingsException("Could not load settings,"
                            + "due to invalid settings in model content !");
        ioe.initCause(e1);
        throw ioe;
    }

       TagCloudData[] dataarray = new TagCloudData[size];
        for (int i = 0; i < size; i++) {
            dataarray[i] = new TagCloudData(null);
            dataarray[i].loadFrom(modelContent, CFG_LS_DATAPOINT + i);
        }
        setDataArray(dataarray);
        super.loadFrom(modelContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveTo(final ModelContent modelContent) {
       modelContent.addInt(CFG_LS_KIND, m_typeoftagcloud);
       super.saveTo(modelContent);
    }
}

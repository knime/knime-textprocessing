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
 *   15.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JLabel;

import org.knime.base.node.util.DataArray;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.property.ColorAttr;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsize;

/**
 * A tag cloud is a representation of words indicating the importance of the
 * words by distributions of the font size, the intensity of the color and
 * the thickness of the font.
 *
 * This class provides all basic methods for this calculation.
 *
 * to create a new tag cloud you have to implement the createTagcloud method.
 * which should do the positioning for the tag cloud.
 *
 *
 * @author Iris Adae, University of Konstanz
 * @param <TC> the implementation of the {@link TagCloudData} this tag cloud is
 *            based on. The {@link TagCloudData} contains all information n
 *            necessary for displaying one single term.
 * @deprecated
 */
@Deprecated
public abstract class AbstractTagCloud<TC extends TagCloudData> implements
        HiLiteListener, Serializable {

    /**
     * The constant defines the horizontal distance between two
     * labels.
     */
    public static final int BOUNDLABEL = 5;

    /**
     * Minimal value for the intensity of a term font size color.
     */
   public static final int MIN_TRANSPARENCY = 50;

   /**
     * Maximal value for the intensity of a term font size color.
     */
   public static final int MAX_TRANSPARENCY = 255;


   /**  if terms have no tags, the color will be managed with this String.*/
   public static final String CFG_UNKNOWN_TAG_COLOR = "no Tag";

    /**
     * some definition for storing the tag cloud information.
     */
    /** the configuration key to save the kind of tagcloud. */
    protected static final String CFG_LS_KIND = "kind";

    /** the configuration key to save the size of the data array. */
    protected static final String CFG_LS_DATASIZE = "datasize";

    /**
     * the configuration key to save one data point.
     */
    protected static final String CFG_LS_DATAPOINT = "datapoint";
    /**
     * the configuration key to save the minimal value.
     */
    protected static final String CFG_LS_MINVALUE = "minvalue";
    /**
     * the configuration key to save the maximal value.
     */
    protected static final String CFG_LS_MAXVALUE = "maxvalue";
    /**
     * the configuration key to save the height of the panel.
     */
    protected static final String CFG_LS_HEIGHT = "height";
    /**
     * the configuration key to save the width of the panel.
     */
    protected static final String CFG_LS_WIDTH = "width";
    /**
     * the configuration key to save the minimal font size.
     */
    protected static final String CFG_LS_MINFONT = "minfont";
    /**
     * the configuration key to save the maximal font size.
     */
    protected static final String CFG_LS_MAXFONT = "maxfont";
    /**
     * the configuration key to save the type of fontsize distribution.
     */
    protected static final String CFG_LS_CALCTYPE = "calctype";
    /**
     * the configuration key to save the bold value.
     */
    protected static final String CFG_LS_BOLD = "bold";
    /**
     * the configuration key to save the alpha value.
     */
    protected static final String CFG_LS_ALPHA = "alpha";

    /**
     * the configuration key to save the name of the font.
     */
    protected static final String CFG_LS_FONTNAME = "fontsizename";

    /** default value for the minimal font.*/
    protected static final int DEFAULT_MINFONT = 10;

    /** default value for the maximal font.*/
    protected static final int DEFAULT_MAXFONT = 50;

    /** default value for the alpha value.*/
    protected static final int DEFAULT_ALPHA = 90;

    /** default value for the bold value.*/
    protected static final int DEFAULT_BOLD = 0;

    /**
     * The minimum of all values, is needed for the font size calculation.
     */
    private double m_minvalue;

    /**
     * The maximum of all values, is needed for the font size calculation.
     */
    private double m_maxvalue;

    /**
     * Map between terms and the term representing TC object. The map is only
     * used during the first calculation. After that the array m_dataarray is
     * used, which contains the same objects. *
     */
    private HashMap<Term, TC> m_hashi;

    /**
     * Array of all data point TC. The array will be sorted if necessary.
     */
    private TC[] m_dataarray;

    /**
     * Map between the rowkey and the representing Term. The map is used to
     * speed up the highliting.
     */
    private HashMap<RowKey, Integer> m_hiliteTool;

    /** Map between the first letter of the tag and the associated colour. */
    private Map<String, Color> m_color;

    /** Maximal font size. */
    private int m_maxfont;

    /** Minimal font size. */
    private int m_minfont;

    /** Name of the currently selected font. */
    private String m_fontname;

    /**
     * Type of font size calculation distribution. (0 for linear, 1 for
     * logarithm and 2 for exponential distribution)
     */
    private int m_calcType;

    /** current width of the tag cloud. */
    private int m_width = 1000;

    /** current height of the tag cloud. */
    private int m_height = 1000;

    /** stores a calculation value for the color intensity distribution. */
    private int m_alpha;

    /** stores the bound of the boldness of term. */
    private int m_bold = DEFAULT_BOLD;



    /**
     * Restores the default values, used in the execution,
     *  of the tagcloud.
     */
    public void restore() {
        changealpha(DEFAULT_ALPHA);

        m_bold = DEFAULT_BOLD;
        m_fontname = new JLabel().getFont().getFontName();
        recreateTagCloud();
    }

    /**
     * Constructor.
     */
    public AbstractTagCloud() {
        /** initializing some standardvalues */
        m_color = TagCloudGeneral.getStandardColorMap();
        m_fontname = new JLabel().getFont().getFontName();
        m_calcType = 0;
        m_hiliteTool = new HashMap<RowKey, Integer>();
    }

    /**
     * This method provides that all x and y values of terms are above 0
     * and the size of the tag cloud is calculated.
     */
    public void setlabelsontheiplaces() {
        double minx = Double.MAX_VALUE,
                miny = Double.MAX_VALUE;
        double maxx = Double.MAX_VALUE * (-1),
                maxy = Double.MAX_VALUE * (-1);

        for (TC aktual : m_dataarray) {

            if ((aktual.getX() + aktual.getWidth()) > maxx) {
                maxx = (int)(aktual.getX() + aktual.getWidth());
            }
            if ((aktual.getY() + aktual.getHeight()) > maxy) {
                maxy = (int)(aktual.getY() + aktual.getHeight());
            }
            if (aktual.getY() < miny) {
                miny = aktual.getY();
            }
            if (aktual.getX() < minx) {
                minx = aktual.getX();
            }

        }
        minx -= BOUNDLABEL;
        miny -= BOUNDLABEL;

        /**
         * calculation of the size of the panel.
         */
        m_width =  ((int)(maxx - minx)) + BOUNDLABEL;
        m_height = (int)(maxy - miny) + BOUNDLABEL;

        /**
         * if the minimal values were below 0 the positions are moved.
         */

        for (int j = 0; j < m_dataarray.length; j++) {

            TC aktual = m_dataarray[j];
            aktual.setXY(aktual.getX() - minx, aktual.getY() - miny);
        }
    }

    /**
     * Determines the minimal and maximal value. Initializes the hashtable
     * containing the data and the preparation for the highlighting
     *
     * @param data the datatable containing terms and values
     * @param ignoretags if true the tag of the terms will be ignored
     *
     * @param termColumn id of the column containing the terms
     * @param valueColumn id of the column containing the values
     */
    protected void initMinMaxandData(final DataArray data,
            final boolean ignoretags,
            final int termColumn, final int valueColumn) {

        m_minfont = DEFAULT_MINFONT;
        m_maxfont = DEFAULT_MAXFONT;

        m_maxvalue = Double.MIN_VALUE;
        m_minvalue = Double.MAX_VALUE;
        // Bestimmung maximaler und minimaler Termwert in Document
        m_hashi = new HashMap<Term, TC>();
        m_hiliteTool = new HashMap<RowKey, Integer>();

        if (ignoretags) {
            initMinMaxandDataignoreTags(data, termColumn,
                    valueColumn);
        } else {
            initMinMaxandData(data, termColumn,
                    valueColumn);

        }
    }

    /**
     * Determines the minimal and maximal value. Initalizes the hashtable
     * containing the data and the preparation for the highlighting the tags of
     * the terms will be ignored, so each term, which ocurres will be saved with
     * the first tag. This tag will be user for the textcolor later on.
     *
     * @param data the datatable containing terms and values
     * @param termColumn id of the column containing the terms
     * @param valueColumn id of the column containing the values
     */
    private void initMinMaxandDataignoreTags(final DataArray data,
            final int termColumn, final int valueColumn) {

        HashMap<Term, Term> termtags = new HashMap<Term, Term>();

        for (final DataRow row : data) {
            if (!row.getCell(termColumn).isMissing()
                    && !row.getCell(valueColumn).isMissing()) {
                Term term =
                        ((TermValue)row.getCell(termColumn))
                                .getTermValue();
                Term termwithouttags = new Term(term.getWords(), null, true);
                double i =
                        ((DoubleValue)row.getCell(valueColumn))
                                .getDoubleValue();
                RowKey rowk = row.getKey();
                // m_hiliteTool.put(rowk, term);

                if (m_hashi.containsKey(termwithouttags)) {
                    i = m_hashi.get(termwithouttags).addFreq(i, rowk);

                } else {
                    m_hashi.put(termwithouttags, createTagCloudData(
                            termwithouttags, i, rowk));
                    termtags.put(termwithouttags, term);
                    Color c = data.getDataTableSpec()
                                .getRowColor(row).getColor();
                    if (!c.equals(ColorAttr.DEFAULT.getColor())) {
                        m_hashi.get(termwithouttags).setColorPrefixed(c);
                    }
                }
                if (i < m_minvalue) {
                    m_minvalue = i;
                }
                if (i > m_maxvalue) {
                    m_maxvalue = i;
                }
            }
        }

        // we now reassign the first tag to the term and init the highlited map
        Iterator<Term> it2 = m_hashi.keySet().iterator();
        HashMap<Term, TC> copy = new HashMap<Term, TC>();
        while (it2.hasNext()) {
            Term twithout = it2.next();
            Term twith = termtags.get(twithout);
            TC tcd = m_hashi.get(twithout);

            tcd.setTerm(twith);
            copy.put(twith, tcd);
        }
        m_hashi = copy;

    }

    /**
     * Determines the minimal and maximal value. Initalizes the hashtable
     * containing the data and the preparation for the highlighting
     *
     * @param data the datatable containing terms and values
     * @param termColumn id of the column containing the terms
     * @param valueColumn id of the column containing the values
     */
    private void initMinMaxandData(final DataArray data,
            final int termColumn, final int valueColumn) {

        for (final DataRow row : data) {
            if (!row.getCell(termColumn).isMissing()
                    && !row.getCell(valueColumn).isMissing()) {
                 Term term =
                        ((TermValue)row.getCell(termColumn))
                                .getTermValue();
                double i =
                        ((DoubleValue)row.getCell(valueColumn))
                                .getDoubleValue();
                RowKey rowk = row.getKey();

                if (m_hashi.containsKey(term)) {
                    i = m_hashi.get(term).addFreq(i, rowk);

                } else {
                    m_hashi.put(term, createTagCloudData(term, i, rowk));
                    Color c = data.getDataTableSpec()
                            .getRowColor(row).getColor();
                    if (!c.equals(ColorAttr.DEFAULT.getColor())) {
                        m_hashi.get(term).setColorPrefixed(c);
                    }
                }
                if (i < m_minvalue) {
                    m_minvalue = i;
                }
                if (i > m_maxvalue) {
                    m_maxvalue = i;
                }
            }
        }
    }

    /**
     * Creates a new Object of TC.
     *
     * @param term representing the whole object and containing the words to be
     *            shown.
     * @param i value for the calculating the font size
     * @param rowk rowkey for hiliting
     * @return extends TagCloudData
     */
    public abstract TC createTagCloudData(Term term, double i, RowKey rowk);

   /**
     * Initializes all Labels / the TC fields. therefore the font size
     * distribution in m_fs is used and the min and max values of the font and
     * the data The Color is attached by using the Hash-Table m_color
     *
     * @param seed The random seed to use
     */
    protected void initAllLabels(final long seed) {
        Iterator<Term> it = m_hashi.keySet().iterator();
        m_color = TagCloudGeneral.getStandardColorMap();
        Random rt = new Random(seed);
        FontRenderContext fontrender =
                new FontRenderContext(new Font(m_fontname, Font.PLAIN,
                        DEFAULT_MINFONT).getTransform(), true, true);
        // copy is used to erase the unnecessary entries of the color map
        Map<String, Color> copy = TagCloudGeneral.getStandardColorMap();
        Color standard = m_color.get(CFG_UNKNOWN_TAG_COLOR);

        TCFontsize fs = TagCloudGeneral.getfontsizeobject(m_calcType);

        while (it.hasNext()) {

            TC data = m_hashi.get(it.next());
            Color c = standard;

            if (!data.isColorPrefixed()) {
                Iterator<Tag> itt = data.getTerm().getTags().iterator();
                if (!itt.hasNext()) {
                    c = standard;
                    copy.remove(CFG_UNKNOWN_TAG_COLOR);
                } else {
                    String now = itt.next().getTagValue().substring(0, 1);
                    if (m_color.containsKey(now)) {
                        c = m_color.get(now);
                        copy.remove(now);
                    } else {
                        c = standard;
                        copy.remove(CFG_UNKNOWN_TAG_COLOR);
                    }
                }
            }
            data.initLabel((int)fs.getSize(m_minfont, m_maxfont, m_minvalue,
                    m_maxvalue, data.getsumFreq()), c, m_fontname, fontrender,
                    rt.nextLong());
        }

        Iterator<String> stringit = copy.keySet().iterator();
        while (stringit.hasNext()) {
            m_color.remove(stringit.next());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hiLite(final KeyEvent event) {
        hiLite(event.keys());
    }

    /**Hilites to all given rowkeys the corresponding terms.
     * @param keys the rowkeys to be hilighted
     */
    public void hiLite(final Set<RowKey> keys) {
        Iterator<RowKey> it = keys.iterator();
        while (it.hasNext()) {
            RowKey cur = it.next();
            if (m_hiliteTool.containsKey(cur)) {
                int curterm = m_hiliteTool.get(cur).intValue();
                if (curterm >= 0 && curterm < m_dataarray.length) {
                    TC tcd = m_dataarray[curterm];
                    tcd.setHighlited(true);
                }
            }
        }
    }

    /**
     * Changes the minimal and maximal font size. These two bounds are used for
     * the font size distribution algorithm.
     *
     * @param min new minimal font size
     * @param max new maximal font size
     * @param calctype new type of the calculation
     */
    protected void setMinMaxFontsize(final int min,
            final int max, final int calctype) {
        m_minfont = min;
        m_maxfont = max;
        m_calcType = Math.min(3, Math.max(0, calctype));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unHiLite(final KeyEvent event) {
        Iterator<RowKey> it = event.keys().iterator();
        while (it.hasNext()) {
            RowKey cur = it.next();
            if (m_hiliteTool.containsKey(cur)) {
                int curterm = m_hiliteTool.get(cur).intValue();
                if (curterm >= 0 && curterm < m_dataarray.length) {
                    TC tcd = m_dataarray[curterm];
                    tcd.setHighlited(false);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unHiLiteAll(final KeyEvent event) {
        for (int j = 0; j < m_dataarray.length; j++) {
            TC tcd = m_dataarray[j];
            tcd.setHighlited(false);
        }
    }

    /**
     * Clears current selection.
     *
     */
    public void clearSelection() {
        for (int j = 0; j < m_dataarray.length; j++) {
            m_dataarray[j].setSelected(false);
        }
    }

    /**
     * Changes the font size of all labels if necessary.
     *
     * @param minFont new minimal font size
     * @param maxFont new maximal font size
     * @param fontname new font name
     * @param calcType new font size calculation type
     * @param bold new bold value
     */
    public void changeFontsizes(final int minFont, final int maxFont,
            final String fontname, final int calcType, final int bold) {
        if (m_minfont != minFont || m_maxfont != maxFont
                || !m_fontname.equals(fontname) || calcType != m_calcType
                || m_bold != bold) {
            m_minfont = minFont;
            m_maxfont = maxFont;
            m_fontname = fontname;
            m_calcType = calcType;
            TCFontsize fs = TagCloudGeneral.getfontsizeobject(m_calcType);

            double bound = getBoldBound(bold);
            for (int j = 0; j < m_dataarray.length; j++) {
                TC tcd = m_dataarray[j];
                boolean b = false;
                b = tcd.getsumFreq() > bound ? true : false;
                tcd.setFontStyle((int)fs.getSize(m_minfont, m_maxfont,
                        m_minvalue, m_maxvalue, tcd.getsumFreq()), m_fontname,
                        b);
            }
            recreateTagCloud();
        }
    }

    /**
     * Changes the bold value and return the lower bound of the terms painted
     * bold.
     *
     * @param bold between 0 and 100
     * @return the lower bound of the frequency for terms being painted bold
     */
    private double getBoldBound(final int bold) {
        m_bold = bold;
        if (m_bold >= 100) {
            return m_minvalue - 1;
        } else if (m_bold <= 0) {
            return m_maxvalue + 1;
        }
        double maxvalue = m_minvalue + ((m_maxvalue - m_minvalue)
                * (99 - m_bold) / (99));
        return maxvalue;
    }

    /**
     * Changes the font size of all Labels if necessary.
     *
     * @param minFont new minimal font size
     * @param maxFont new maximal font size
     */
    public void changeFontsizes(final int minFont, final int maxFont) {
        changeFontsizes(minFont, maxFont, m_fontname, m_calcType, m_bold);
    }

    /**
     * Called after changing the font sizes.
     */
    public abstract void recreateTagCloud();

    /**
     * Initializes the Array representation of the data.
     * The array will be used for the view.
     *
     * @param it Iterator of the hashmap, if necessary it should be already
     * sorted.
     */
    public abstract void createArray(final Iterator<Term> it);

    /**
     * @return the current color mapping.
     */
    public Map<String, Color> getColorMap() {
        return m_color;
    }

    /**
     * Changes the color of the text color.
     *
     * @param colormap new color mapping containing the first letters of the
     *            tags and the associated colors.
     */
    public void setColorMap(final Map<String, Color> colormap) {
        m_color = colormap;

        Color standard = Color.black;
        if (m_color.containsKey(CFG_UNKNOWN_TAG_COLOR)) {
            standard = m_color.get(CFG_UNKNOWN_TAG_COLOR);
        }
        for (int j = 0; j < m_dataarray.length; j++) {
            TC tcd = m_dataarray[j];
            Iterator<Tag> itt = tcd.getTerm().getTags().iterator();
            if (itt.hasNext()) {
                try {
                    String now = itt.next().getTagValue().substring(0, 1);
                    if (m_color.containsKey(now)) {
                        tcd.changeTextcolor(m_color.get(now));
                    } else {
                        tcd.changeTextcolor(standard);
                    }
                } catch (IndexOutOfBoundsException io) {
                    tcd.changeTextcolor(standard);
                }
            } else {
                tcd.changeTextcolor(standard);
            }
        }
    }

    /**
     * @return minimal font size.
     */
    public int getminFontsize() {
        return m_minfont;
    }

    /**
     * @return maximal font size.
     */
    public int getmaxFontsize() {
        return m_maxfont;
    }

    /**
     * @return the currently selected font name.
     */
    public String getfontName() {
        return m_fontname;
    }

    /**
     * @return the type of calculation.
     */
    public int getCalcType() {
        return m_calcType;
    }

    /**Changes the width of the tagcloud.
     *
     * @param width of the table
     * @return the height of the panel
     */
    public int changeWidth(final int width) {
        if (width == m_width) {
            return -1;
        }
        m_width = width;
        recreateTagCloud();
        return getPreferredSize().height;
    }

    /**
     * @return the hash map containing the terms and positions to display
     */
    public HashMap<Term, TC> getDataMap() {
        return m_hashi;
    }

    /**Changes the data map.
     *
     * The mapping is only used during the initialization. Afterwards
     * any change here is useless.
     *
     * @param datamap the new <code>HashMap </code> containing
     * all necessary data.
     *
     */
    protected void setDataMap(final HashMap<Term, TC> datamap) {
            m_hashi = datamap;
    }

    /**
     * Is called when the user clicks on a point to select the element. Sets all
     * term as selected, which representation contains the clicked point.
     *
     * @param clicked a Point in the panel
     */
    public void selectClickedElement(final Point clicked) {
        for (int j = 0; j < m_dataarray.length; j++) {
            TC tcd = m_dataarray[j];
                tcd.setSelected(tcd.contains(clicked));
        }
    }

    /**
     * Is called when the user draws an rectangle to select elements. selects
     * all terms which representing bounding rectangle intersects with the given
     * rectangle
     *
     * @param selectionRectangle a rectangle inside the panels
     */
    public void selectElementsIn(final Rectangle selectionRectangle) {
        for (int j = 0; j < m_dataarray.length; j++) {
            TC tcd = m_dataarray[j];
            tcd.setSelected(tcd.intersects(selectionRectangle));
        }
    }

    /**
     * hilite or unhilite the currently selected elements.
     *
     * @param b when true the selected will be hilited otherwise the selected
     *            will be unhilited
     * @return all rowkeys of fitting rows, to fire a hilite event
     *
     */
    public Set<RowKey> hiLiteSelected(final boolean b) {
        Set<RowKey> rk = new HashSet<RowKey>();

        for (int j = 0; j < m_dataarray.length; j++) {
            TC tcd = m_dataarray[j];
            if (tcd.isSelected()) {
                rk.addAll(tcd.getRowKeys());
                tcd.setHighlited(b);
            }
        }
        return rk;

    }

    /**
     * @return the standard size of the tagcloud
     */
    public Dimension getPreferredSize() {
        return new Dimension(m_width, m_height);
    }

    /**
     * Changing the alpha value. Use with care, the data points will not be
     * adjusted. To also adjust the Data Points use changealpha.
     *
     * @param alpha new alpha value
     */
    protected void setAlpha(final int alpha) {
        m_alpha = alpha;
    }

    /**
     * Changes the current distribution of the color intensity of the labels.
     *
     * @param alpha an int - value between 0 and 100. The higher the value is,
     *            the more labels will loose color intensity.
     */
    public void changealpha(final int alpha) {
        if (getAlpha() != alpha) {
            setAlpha(alpha);

            TCFontsize fs = TagCloudGeneral.getfontsizeobject(m_calcType);

            int a = 100 - getAlpha();
            double maxvalue =
                    m_minvalue + (m_maxvalue - m_minvalue) * a / (100);

            for (int j = 0; j < m_dataarray.length; j++) {
                TC tcd = m_dataarray[j];
                double freq = tcd.getsumFreq();
                double d = MAX_TRANSPARENCY;
                if (freq < maxvalue) {
                    d = fs.getSize(MIN_TRANSPARENCY, MAX_TRANSPARENCY,
                                    m_minvalue, maxvalue, freq);
                }
                tcd.setColorTransparency((int)d);
            }
        }
    }

    /**
     * Is called when the amount of bold labels should be changed. All labels
     * have to be recalculated, to adjust the label size.
     *
     * @param bold an int - value between 0 and 100. The higher the value,
     *            the less labels will loose color intensity.
     */
    public void changebold(final int bold) {
        int boldvalue = bold;
        if (m_bold != bold) {
            if (bold > 100) {
                boldvalue = 100;
            } else if (bold < 0) {
                boldvalue = 0;
            }
            changeFontsizes(getminFontsize(), getmaxFontsize(), getfontName(),
                    getCalcType(), boldvalue);
        }
    }

    /**
     * @return an int - value between 0 and 100. The higher the value, the more
     *         labels will be painted in bold.
     */
    public int getBold() {
        return m_bold;
    }

    /**
     * @return an int - value between 0 and 100. The higher the value, the less
     *         labels will loose color intensity.
     */
    public int getAlpha() {
        return m_alpha;
    }

    /**
     * @return the datapoints in an array.
     */
    public TC[] getDataArray() {
        return m_dataarray;
    }

    /**
     * Changing the dataarray to the given. if dataarray is empty or null,
     * m_dataarray will not be updated.
     *
     * @param dataarray the new dataarray containing the TC objects.
     */
    protected void setDataArray(final TC[] dataarray) {
        if (dataarray != null && dataarray.length > 0) {
            m_dataarray = dataarray;
        }
    }

    /**
     * Changes the minimal and maximal font size. These two bounds are used for
     * the font size distribution algorithm.
     *
     * @param min new minimal font size
     * @param max new maximal font size
     */
    protected void setMinMaxFontsize(final int min,
            final int max) {
        m_minfont = min;
        m_maxfont = max;
    }

    /**
     * Changes the minimal and maximal found data values to the given.
     *
     * @param minvalue new minimal found data value
     * @param maxvalue new maximal found data value
     */
    protected void setMinMaxValue(final double minvalue,
            final double maxvalue) {
       m_minvalue = minvalue;
       m_maxvalue = maxvalue;
    }

    /**
     * Sets the Dimension of the Tagcloud.
     *
     * @param drawingPaneDimension new Dimension
     */
    public void setSize(final Dimension drawingPaneDimension) {
       m_width = drawingPaneDimension.width;
       m_height = drawingPaneDimension.height;
    }

    /**
     * Sets the width of the tagcloud.
     *
     * @param width new width
     */
    public void setWidth(final int width) {
        if (width > 0) {
            m_width = width;
        }
    }

    /**
     * Saves the data, which is needed for the view into the given
     * <code>ModelContent</code>.
     *
     *  @param modelContent The <code>ModelContentRO</code> to save the data
     * to.
     */
    public void saveTo(final ModelContent modelContent) {
        modelContent.addInt(CFG_LS_ALPHA, getAlpha());
        modelContent.addInt(CFG_LS_BOLD, getBold());
        modelContent.addInt(CFG_LS_CALCTYPE, getCalcType());
        modelContent.addString(CFG_LS_FONTNAME, getfontName());
        modelContent.addInt(CFG_LS_HEIGHT, m_height);
        modelContent.addInt(CFG_LS_WIDTH, m_width);
        modelContent.addInt(CFG_LS_MAXFONT, getmaxFontsize());
        modelContent.addInt(CFG_LS_MINFONT, getminFontsize());
        modelContent.addDouble(CFG_LS_MINVALUE, m_minvalue);
        modelContent.addDouble(CFG_LS_MAXVALUE, m_maxvalue);

        modelContent.addInt(CFG_LS_DATASIZE, m_dataarray.length);

        for (int i = 0; i < m_dataarray.length; i++) {
            m_dataarray[i].saveTo(modelContent, CFG_LS_DATAPOINT + i);
        }
    }

    /**
     * Loads the data from the given <code>ModelContentRO</code>.
     *
     * @param modelContent The <code>ModelContentRO</code> to load the cells
     * from.
     *  @throws InvalidSettingsException If setting to load is not valid.
     */
    public void loadFrom(final ModelContentRO modelContent)
    throws InvalidSettingsException {
        m_minvalue = modelContent.getDouble(CFG_LS_MINVALUE);
        m_maxvalue = modelContent.getDouble(CFG_LS_MAXVALUE);

        m_height = modelContent.getInt(CFG_LS_HEIGHT);
        m_width = modelContent.getInt(CFG_LS_WIDTH);

        changeFontsizes(modelContent.getInt(CFG_LS_MINFONT),
                modelContent.getInt(CFG_LS_MAXFONT),
                modelContent.getString(CFG_LS_FONTNAME),
                modelContent.getInt(CFG_LS_CALCTYPE),
                modelContent.getInt(CFG_LS_BOLD));
        changealpha(modelContent.getInt(CFG_LS_ALPHA));

        rebuiltHiliteTable();
        rebuiltColorTable();
    }


    /**
     * Rebuilts the map between the rowkeys and the corresponding TC.
     */
    protected void rebuiltHiliteTable() {
        if (m_dataarray != null && m_dataarray.length > 0) {
            m_hiliteTool.clear();
            for (int i = 0; i < m_dataarray.length; i++) {
                TC tcd = m_dataarray[i];
                Iterator<RowKey> it = tcd.getRowKeys().iterator();
                while (it.hasNext()) {
                    m_hiliteTool.put(it.next(), i);
                }
            }
        }
    }

    /**
     * Rebuilt the color mapping from the current color selection
     * given in the data array.
     */
    protected void rebuiltColorTable() {
        if (m_dataarray != null && m_dataarray.length > 0) {
            m_color = new HashMap<String, Color>();
            HashMap<String, Color> original
                    = TagCloudGeneral.getStandardColorMap();

            for (int i = 0; i < m_dataarray.length; i++) {
                TC tcd = m_dataarray[i];
                if (!tcd.isColorPrefixed()) {
                    Iterator<Tag> itt = tcd.getTerm().getTags().iterator();
                    String now = CFG_UNKNOWN_TAG_COLOR;
                    if (itt.hasNext()) {

                        String newString = itt.next().getTagValue();
                        if (newString.length() > 0) {
                            newString = newString.substring(0, 1);
                            if (original.containsKey(newString)) {
                             now = newString;
                            } else {
                                now = CFG_UNKNOWN_TAG_COLOR;
                        }
                        } else {
                            now = CFG_UNKNOWN_TAG_COLOR;
                        }
                    }
                    if (!m_color.containsKey(now)) {
                        Color c =
                           new Color(tcd.getColor().getRGB(), false);
                        m_color.put(now, c);
                    }
                }
            }
        }
    }
}

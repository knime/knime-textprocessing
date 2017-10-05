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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.knime.core.data.RowKey;
import org.knime.core.data.property.ColorAttr;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

/**
 * This class wraps all information and methods needed for displaying one term.
 *
 * To each term (m_term) is the frequency, which is needed for the
 * fontsize/color intensity/bold calculation stored. The fontsize, the width and
 * height of the representation is also stored to speed up the viewing routines.
 * <p>
 * The position of the term is stored.
 * <p>
 * The color of the term is stored. When the colors aren't distributed using the
 * tag type, the color will be fixed to a constant and the boolean value
 * m_colorfixed will be set to true.
 * <p>
 * A term can also be selected or hilited.
 * <p>
 * For sending hilite events all {@link RowKey} which are linked to this object,
 * are stored in a list.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
public class TagCloudData {

    /** the following configuration keys are used for loading and saving. */

    private static final String CFG_KEY_TERM_COLOR = "color";
    private static final String CFG_KEY_TERM_COLORFIXED = "colorfixed";
    private static final String CFG_KEY_TERM_HILITE = "hilite";
    private static final String CFG_KEY_TERM_SELECTED = "selected";
    private static final String CFG_KEY_TERM_VALUE = "value";
    private static final String CFG_KEY_TERM_X = "x";
    private static final String CFG_KEY_TERM_Y = "y";
    private static final String CFG_KEY_TERM_WORDS = "words";
    private static final String CFG_KEY_TERM_ROWKEYS = "rowkeys";
    private static final String CFG_KEY_TERM_TAG_TYPE = "tagtype";
    private static final String CFG_KEY_TERM_TAG_VALUE = "tagvalue";

    /** Term, representing the Object. */
    private Term m_term;

    /** summed up value for the term . */
    private double m_sumFreq;

    /**
     * rowIDs of all appearances of the term (in all documents used).
     */
    private List<RowKey> m_rowID;

    /**
     * Position of the term in the layout.
     */
    private double m_x, m_y;

    /**
     * Width and Height of term representation.
     */
    private double m_width, m_height;

    /** font size of this object. */
    private int m_fs;

    /** true, if the term is currently selected. */
    private boolean m_isselected;

    /** The font color. */
    private Color m_color;

    /** true, if the term is currently hilited. */
    private boolean m_isHighlighted;

    /** true if the color is prefixed.
     * For example by a color appender
     */
    private boolean m_colorfixed = false;

    /** true, if the term should be printed in bold.*/
    private boolean m_isbold = false;

    /**
     * initializes the label object with the font size and the color. width and
     * height are generated using the font size. x and y are initialized
     * randomly using the seed as starting point
     *
     * @param fs the font size of the Term
     * @param c Color of the term
     * @param fontname the name of the selected font for the term
     * @param frc the current FontRenderContext
     * @param seed the Random seed
     */
    public void initLabel(final int fs, final Color c, final String fontname,
            final FontRenderContext frc, final long seed) {
        Random rt = new Random(seed);
        m_fs = fs;
        changeTextcolor(c);
        Font myfont = new Font(fontname, Font.PLAIN, m_fs);
        TextLayout tl = new TextLayout(m_term.getText(), myfont, frc);
        Rectangle2D size = tl.getBounds();
        m_width = size.getWidth();
        m_height = tl.getAscent();
        setFontStyle(m_fs, fontname, false);
        m_x = rt.nextDouble();
        m_y = rt.nextDouble();
    }

    /**
     * initializes the label object with the fontsize and the color. width and
     * height are generated using the fontsize. x and y are set 0
     *
     * @param fs int, showing the fontsize of the Term
     *
     * @param c Color, the term should k.be painted
     * @param fontname , name of the new font
     */
    public void initLabel(final int fs, final Color c,
            final String fontname) {
        initLabel(fs, c, fontname, new FontRenderContext(new Font(fontname,
                Font.PLAIN, fs).getTransform(), true, true),
                System.currentTimeMillis());
    }

    /**Checks if two labels intersect.
     *
     * @param comp another tagclouddata
     * @return true if the section of this and comp intersects
     */
    public boolean intersects(final TagCloudData comp) {
        Rectangle2D.Double akt =
                new Rectangle2D.Double(this.m_x, this.m_y, this.m_width,
                        this.m_height);
        Rectangle2D.Double cur =
                new Rectangle2D.Double(comp.m_x, comp.m_y, comp.m_width,
                        comp.m_height);
        return akt.intersects(cur);
    }

    /**Checks if two labels intersect.
    *
    * @param comp another tagclouddata
     * @param perc maximal allowed overlap percentage
    * @return true if the section of this and comp intersects
    */
   public boolean intersects(final TagCloudData comp, final int perc) {
       Rectangle cur =
               new Rectangle((int)comp.m_x, (int)comp.m_y, (int)comp.m_width,
                       (int)comp.m_height);
       return intersects(cur, perc);
   }

    /**Calculate the distance of the center of two terms.
     *
     * @param l another TagCloudData
     * @return the distance between the center of gravities of the two labels.
     */
    public double getlabeldist(final TagCloudData l) {
        return Math.hypot(getCenterX() - l.getCenterX(), getCenterY()
                - l.getCenterY());
    }

    /**
     * Initializes the Object.
     *
     * @param t the Term
     * @param freq the first found frequency of the Term
     * @param rowid the first found RowKey of the Term
     */
    protected TagCloudData(final Term t, final double freq,
            final RowKey rowid) {
        m_term = t;
        m_sumFreq = freq;
        m_rowID = new ArrayList<RowKey>();
        m_rowID.add(rowid);
        m_isHighlighted = false;
        m_isselected = false;
        m_isbold = false;
    }

    /**
     * Initializes the object with the given term t.
     *
     * @param t a Term
     */
    public TagCloudData(final Term t) {
        m_term = t;
        m_sumFreq = 0;
        m_rowID = new ArrayList<RowKey>();
        m_color = Color.PINK;
        m_fs = 0;
        m_isselected = false;
    }

    /**
     * Must be called when the term is found in another row. E.g. if the tag
     * types are ignored. The rowid will be insert and
     * the frequencies added.
     *
     * @param freq additional frequency
     * @param rowid additional rowid
     * @return the new frequency
     */
    public double addFreq(final double freq, final RowKey rowid) {
        m_sumFreq += freq;
        m_rowID.add(rowid);
        return m_sumFreq;
    }

    /**
     * {@inheritDoc} returns the HashCode of the termtext.
     */
    @Override
    public int hashCode() {
        return m_term.getText().hashCode();
    }

    /**
     * @return true if the two objects represent the same term.
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof TagCloudData)) {
            return false;
        }
        TagCloudData tcd = (TagCloudData)o;
        return (m_term.equals(tcd.m_term));
    }

    /**
     * @return x-center of the label
     */
    public double getCenterX() {
        return (m_x + m_width / 2);
    }

    /**
     * @return y-center of the label
     */
    public double getCenterY() {
        return (m_y + m_height / 2);
    }

    /**
     * sets the center of the label to xcoor.
     *
     * @param xcoor new x-center
     */
    public void setCenterX(final double xcoor) {
        m_x = xcoor - m_width / 2;
    }

    /**
     * sets the center of the label to ycoor.
     *
     * @param ycoor new y-center
     */
    public void setCenterY(final double ycoor) {
        m_y = ycoor - m_height / 2;
    }

    /**
     * sets the center of the label to the specified Data.
     *
     * @param x the value for the x-center of the label
     * @param y the value for the y-center of the label
     */
    public void setCenter(final double x, final double y) {
        setCenterX(x);
        setCenterY(y);
    }

    /**
     * Sets the top-left corner of the label to x and y.
     *
     * @param x the left of the label
     * @param y the top of the label
     */
    public void setXY(final double x, final double y) {
        m_x = x;
        m_y = y;
    }

    /**
     * @return the summed up frequency
     */
    public double getsumFreq() {
        return m_sumFreq;
    }

    /**
     * Checks if the label could be painted into the rect.
     *
     * @param rect a area to paint the label in
     * @return true if the label could be placed in this rectangle
     */
    public boolean fitsin(final Rectangle2D.Double rect) {
        if (rect.width < m_width || rect.height < m_height) {
            return false;
        }
        return true;
    }

    /**
     * Hilite (true) or unhilite the term.
     *
     * @param h if true, the label will be marked as hilited,
     * if false, the label will be marked as not hilited
     */
    public void setHighlited(final boolean h) {
        m_isHighlighted = h;
    }


    /**
     * @param t new Term for the Label
     */
    public void setTerm(final Term t) {
        m_term = t;
    }

    /**
     * @return the Term representing the label
     */
    public Term getTerm() {
        return m_term;
    }

    /**
     * @return the currently stored font size
     */
    public int getFontsize() {
        return m_fs;
    }

    /**
     * @return x the horizontal position.
     */
    public double getX() {
        return m_x;
    }

    /**
     * @return y the vertical position.
     */
    public double getY() {
        return m_y;
    }

    /**
     * Reduces the x value.
     *
     * @param red if positiv x will be reduced if negative x will be summed
     */
    public void reduceX(final double red) {
        m_x -= red;
    }

    /**
     * Reduces the y value.
     *
     * @param red if positiv y will be reduced if negative y will be summed
     */
    public void reduceY(final double red) {
        m_y -= red;
    }

    /**
     * @return width of the label
     */
    public double getWidth() {
        return m_width;
    }

    /**
     * @return height of the label
     */
    public double getHeight() {
        return m_height;
    }

    /**
     * Changes the font of the label.
     *
     * @param fontsize new fontsize
     * @param fontname new fontname
     * @param bold true if the fontsize should be bold
     */
    public void setFontStyle(final int fontsize,
            final String fontname,
            final boolean bold) {
        m_fs = fontsize;
        setBold(bold);

        int fontstyle = Font.PLAIN;
        if (bold) {
            fontstyle = Font.BOLD;
        }
        Font myfont = new Font(fontname, fontstyle, fontsize);

        Rectangle2D size =
                myfont.getStringBounds(m_term.getText(), new FontRenderContext(
                        myfont.getTransform(), true, true));
        TextLayout tl =
                new TextLayout(m_term.getText(), myfont, new FontRenderContext(
                        myfont.getTransform(), true, true));
        m_width = size.getWidth();
        m_height = tl.getAscent();
    }

    /** Changes the text color if color is not null and the color wasn't fixed.
     *
     * @param color the new text color for the Label
     * @return true if the text color was changed
     */
    public boolean changeTextcolor(final Color color) {
        if (color.equals(m_color) || m_colorfixed) {
            return false;
        }
        m_color = color;
        return true;
    }

    /** Returns the text color of the term. If the term is hilited or selected,
     * this will be considered.
     *
     * @return the text color to display the term
     */
    public Color getTextcolor() {
        if (m_isHighlighted) {
            if (m_isselected) {
                return ColorAttr.SELECTED_HILITE;
            }
            return ColorAttr.HILITE;
        }
        if (m_isselected) {
            return ColorAttr.SELECTED;
        }
        return m_color;
    }

    /**
     * @return the saved color data, ignoring
     * any hilite or selection information.
     */
    public Color getColor() {
          return m_color;
    }

    /**
     * @return true if the term is currently selected.
     */
    public boolean isSelected() {
        return m_isselected;
    }

    /**
     * @param select if true, the term will be painted as a selected one
     */
    public void setSelected(final boolean select) {
        m_isselected = select;
    }

    /**
     * @param clicked a Point in the Layout, selected by the user
     * @return true if the term rectangle contains the point
     */
    public boolean contains(final Point clicked) {
        int x = clicked.x;
        int y = clicked.y;
        if (m_x <= x && x <= m_x + m_width) {
            if (m_y <= y && x <= m_y + m_height) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param selectionRectangle a Rectangle in the view.
     * @return true if the term and the rectangle intersect
     */
    public boolean intersects(final Rectangle selectionRectangle) {
        Rectangle akt =
                new Rectangle((int)this.m_x, (int)this.m_y, (int)this.m_width,
                        (int)this.m_height);

        return akt.intersects(selectionRectangle);
    }

    /**
     * @param selectionRectangle a Rectangle in the view.
     * @param perc amount of allowed overlapping.
     * @return true if the term and the rectangle intersect
     */
    public boolean intersects(final Rectangle selectionRectangle,
            final int perc) {
        Rectangle akt =
                new Rectangle((int)this.m_x, (int)this.m_y, (int)this.m_width,
                        (int)this.m_height);
        Rectangle sect = akt.intersection(selectionRectangle);
        double area = sect.getWidth() * sect.getHeight();
        if (sect.getWidth() * sect.getHeight() <= 0) {
            return false;
        }

        double overlap =
                100
                        * area
                        / Math.min(akt.height * akt.width,
                                selectionRectangle.height
                                        * selectionRectangle.width);

        return overlap < perc;
    }

    /**
     * @return a List of all Rowkeys, which have been collected for this term.
     */
    public List<RowKey> getRowKeys() {
        return m_rowID;
    }

    /**
     * @return true, if one of the rows containing this term in the input
     * data table is highlighted
     */
    public boolean isHighlited() {
        return m_isHighlighted;
    }

    /**
     * Should be called if the color was  appended to a
     * row. For example taking the color appender.
     *
     * After calling this method, the color is fixed and can't be changed.
     *
     * @param c the prefixed color.
     */
    public void setColorPrefixed(final Color c) {
        m_colorfixed = true;
        m_color = c;
    }

    /** If the color is prefixed, e.g. with the colorappender.
     * The color can't be changed, only the alpha-value indicating
     * the transparency can be changed.
     *
     * @return true, if the color was prefixed and can't be changed.
     */
    public boolean isColorPrefixed() {
        return m_colorfixed;
    }

    /** Change the transparency of the color used for
     * painting the term.
     * @param alpha a int value between 0 and 255.
     * 255 indicates the term will be painted opaque.
     */
    public void setColorTransparency(final int alpha) {
        Color c = m_color;
        m_color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**Sets the labels to be painted bold or not.
     * @param b if true, the label will be painted bold, otherwise not.
     */
    public void setBold(final boolean b) {
        m_isbold = b;
    }
    /**
     * @return true if the term should be painted in bold style.
     */
    public boolean isBold() {
        return m_isbold;
    }
    /**Adds an certain amount to the current x-value.
     * @param overlapX the current x-value will be increased with
     */
    public void addX(final double overlapX) {
        m_x += overlapX;
    }
    /**Adds an certain amount to the current y-value.
     * @param overlapY the y-value will be increased with.
     */
    public void addY(final double overlapY) {
        m_y += overlapY;
    }

    /**
     * Saves the data to the given <code>ModelContentWO</code>.
     *
     * @param modelContent The <code>ModelContentWO</code> to save the data to.
     * @param config unique String for the keys.
     *
     */
    public void saveTo(final ModelContentWO modelContent,
                       final String config) {
        modelContent.addInt(config + CFG_KEY_TERM_COLOR, m_color.getRGB());
        modelContent.addBoolean(config + CFG_KEY_TERM_COLORFIXED, m_colorfixed);
        modelContent.addBoolean(config + CFG_KEY_TERM_HILITE, m_isHighlighted);
        modelContent.addBoolean(config + CFG_KEY_TERM_SELECTED, m_isselected);
        modelContent.addDouble(config + CFG_KEY_TERM_VALUE, m_sumFreq);
        modelContent.addDouble(config + CFG_KEY_TERM_X, m_x);
        modelContent.addDouble(config + CFG_KEY_TERM_Y, m_y);

        // saving term with text and tags
        modelContent.addString(config + CFG_KEY_TERM_WORDS,  m_term.getText());
        Iterator<Tag> itt = getTerm().getTags().iterator();
        String tagtype =  "";
        String tagvalue =  "";
        if (itt.hasNext()) {
            Tag t = itt.next();
            tagtype = t.getTagType();
            tagvalue = t.getTagValue();
        }
            modelContent.addString(config + CFG_KEY_TERM_TAG_TYPE, tagtype);
            modelContent.addString(config + CFG_KEY_TERM_TAG_VALUE, tagvalue);


        // saving row keys
        RowKey[] rk = new RowKey[m_rowID.size()];
        Iterator<RowKey> it = m_rowID.iterator();
        int i = 0;
        while (it.hasNext()) {
            rk[i] = it.next();
            i++;
        }
        modelContent.addRowKeyArray(config + CFG_KEY_TERM_ROWKEYS, rk);
    }

    /**
     * Loads the data from the given <code>ModelContentRO</code>.
     *
     * @param modelContent The <code>ModelContentRO</code> to load the data
     *            from.
     * @param config unique String for the keys.
     *
     * @throws InvalidSettingsException If setting to load is not valid.
     *
     */
    public void loadFrom(final ModelContentRO modelContent,
            final String config) throws InvalidSettingsException {
        m_color = new Color(modelContent.getInt(config + CFG_KEY_TERM_COLOR),
                true);
        m_colorfixed = modelContent.getBoolean(
                config + CFG_KEY_TERM_COLORFIXED);
        m_isHighlighted = modelContent.getBoolean(config + CFG_KEY_TERM_HILITE);
        m_isselected = modelContent.getBoolean(config + CFG_KEY_TERM_SELECTED);
        m_sumFreq = modelContent.getDouble(config + CFG_KEY_TERM_VALUE);
        m_x = modelContent.getDouble(config + CFG_KEY_TERM_X);
        m_y = modelContent.getDouble(config + CFG_KEY_TERM_Y);

        // loading term data
        String words = modelContent.getString(config + CFG_KEY_TERM_WORDS);
        List<Word> wordlist = new LinkedList<Word>();
        wordlist.add(new Word(words));

        // loading first tag (others aren't needed here)
        List<Tag> taglist = new LinkedList<Tag>();
        String tagtype =
            modelContent.getString(config + CFG_KEY_TERM_TAG_TYPE);
        String tagvalue =
            modelContent.getString(config + CFG_KEY_TERM_TAG_VALUE);

        Tag t = TagFactory.getInstance().createTag(tagtype, tagvalue);

        if (t != null) {
            taglist.add(t);
        }
        m_term = new Term(wordlist, taglist, true);

        // loading row keys
        RowKey[] rk = modelContent.getRowKeyArray(
                config + CFG_KEY_TERM_ROWKEYS);
        m_rowID = new ArrayList<RowKey>();
       for (int i = 0; i < rk.length; i++) {
            m_rowID.add(rk[i]);
        }
    }
}

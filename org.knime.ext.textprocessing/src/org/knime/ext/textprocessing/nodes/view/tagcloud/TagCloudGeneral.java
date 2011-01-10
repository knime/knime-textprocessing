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
 *   14.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsize;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeExponential;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeLinear;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeLogarithmic;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This is a utility class for some tag cloud routines.
 *
 * It provides methods for sorting list of terms and for the color
 * mapping.
 *
 * @author Iris Adae, University of Konstanz
 */
public final class TagCloudGeneral {

    /**
     * Constructor.
     */
    private TagCloudGeneral() {
        // nothing to do
    }

    /**
     * @param keyset a set of keys to be sorted
     * @return an Iterator with all terms of keyset sorted by their Bib
     */
    @SuppressWarnings("unchecked")
    public static final Iterator<Term> getsortedAlphabeticalIterator(
            final Set<Term> keyset) {
        List<Term> keys = new LinkedList<Term>(keyset);
        Collections.sort(keys, new Comparator() {
            public int compare(final Object o1, final Object o2) {
                String tcd1 = ((Term)(o1)).getText();
                String tcd2 = ((Term)(o2)).getText();
                return tcd1.compareTo(tcd2);
            }
        });
        return keys.iterator();
    }


    /**
     * @param hashi hashmap containing to be sorted data
     * @param order true if sorted from small to big
     * @return an Iterator with all terms of m_hashi sorted by their Value
     */
    @SuppressWarnings("unchecked")
    public static final Iterator<Term> getsortedFontsizeIterator(
            final HashMap<Term, TagCloudData> hashi, final boolean order) {
        List<Term> keys = new LinkedList<Term>(hashi.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(final Object o1, final Object o2) {
                int reverse = order ? -1 : 1;
                TagCloudData tcd1 = hashi.get(o1);
                TagCloudData tcd2 = hashi.get(o2);
                if (tcd1.getsumFreq() > tcd2.getsumFreq()) {
                    return reverse;
                } else if (tcd1.getsumFreq() < tcd2.getsumFreq()) {
                    return reverse * (-1);
                } else {
                    return (reverse)
                            * (tcd1.getTerm().getText().compareTo(tcd2
                                    .getTerm().getText()));
                }
            }
        });
        return keys.iterator();
    }

    /**
     * @return the standard colormap
     */
    public static final HashMap<String, Color> getStandardColorMap() {
        HashMap<String, Color> color = new HashMap<String, Color>();

        String[] strlist =
                {"J", "V", "W", "F", "N", "S", "D", "E", "C", "I", "L", "U",
                AbstractTagCloud.CFG_UNKNOWN_TAG_COLOR, "M", "P", "R", "T"};

        float fac = 360f / strlist.length;
        for (int i = 0; i < strlist.length; i++) {
            float col = 360f - (i * fac);
            color.put(strlist[i], Color.getHSBColor(col, 1, 1));
        }
        return color;
    }

    /**
     * @param calcType 0 for linear, 1 for logarithmic, 2 for exponential
     * @return the font size object, to calculate font sizes
     */
    public static final TCFontsize getfontsizeobject(final int calcType) {
        switch (calcType) {
        case 0:
            return (new TCFontsizeLinear());
        case 1:
            return (new TCFontsizeLogarithmic());
        case 2:
            return (new TCFontsizeExponential());
        default:
            return (new TCFontsizeLinear());
        }
    }
}

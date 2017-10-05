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
 *   14.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsize;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeExponential;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeLinear;
import org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize.TCFontsizeLogarithmic;

/**
 * This is a utility class for some tag cloud routines.
 *
 * It provides methods for sorting list of terms and for the color
 * mapping.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
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
    public static final Iterator<Term> getsortedAlphabeticalIterator(
            final Set<Term> keyset) {
        List<Term> keys = new LinkedList<Term>(keyset);
        Collections.sort(keys, new Comparator<Term>() {
            @Override
            public int compare(final Term o1, final Term o2) {
                String tcd1 = o1.getText();
                String tcd2 = o2.getText();
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
    public static final Iterator<Term> getsortedFontsizeIterator(
            final HashMap<Term, TagCloudData> hashi, final boolean order) {
        List<Term> keys = new LinkedList<Term>(hashi.keySet());
        Collections.sort(keys, new Comparator<Term>() {
            @Override
            public int compare(final Term o1, final Term o2) {
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

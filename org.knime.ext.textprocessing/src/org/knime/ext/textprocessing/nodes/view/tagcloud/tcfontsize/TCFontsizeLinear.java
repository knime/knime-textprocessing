/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   11.09.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize;

/**
 *
 * @author Iris Adae, University of Konstanz
 */
public class TCFontsizeLinear implements TCFontsize {

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSize(final double minf, final double maxf,
            final double minv, final double maxv, final double value) {
        // fix in version 2.6
        if (maxv == minv) {
             return minf;
        }
        return ((value - minv) * (maxf - minf) / (maxv - minv)) + minf;
    }
}

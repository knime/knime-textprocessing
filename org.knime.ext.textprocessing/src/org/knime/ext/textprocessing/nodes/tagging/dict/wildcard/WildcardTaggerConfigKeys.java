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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.wildcard;

/**
 * Provides the configuration keys of the wildcard tagger node.
 *
 * @author Kilian Thiel, University of Konstanz
 * @since 2.8
 */
public final class WildcardTaggerConfigKeys {

    private WildcardTaggerConfigKeys() { }

    /**
     * The configuration key of the single/multi term option.
     */
    public static final String CFGKEY_MATCHING_LEVEL = "Matching level";

    /**
     * The configuration key of the regex/wildcard option.
     */
    public static final String CFGKEY_MATCHING_METHOD = "Matching method";
}

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
 * -------------------------------------------------------------------
 * 
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

/**
 *
 * @author Kilian Thiel
 */
public final class TagToStringConfigKeys {

    private TagToStringConfigKeys() { }
    
    /**
     * The configuration key of the tag types to convert.
     */
    public static final String CFG_KEY_TAG_TYPES = "TagTypes";
    
    /**
     * The configuration key of the term column.
     */
    public static final String CFG_KEY_TERM_COL = "TermColumn";
    
    /**
     * The configuration key of the missing tag string.
     */
    public static final String CFG_KEY_MISSING_TAG_STRING = "MissingString";
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *   03.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class FilterConfigKeys {

    private FilterConfigKeys() { }
    
    /**
     * The configuration key for the relative/absolute setting of TF 
     * computation.
     */
    public static final String CFG_KEY_FILTERCOL = "FilterCol";
    
    /**
     * The configuration key for the way of filtering (selection).
     */
    public static final String CFG_KEY_SELECTION = "Selection";
    
    /**
     * The configuration key for the min max threshold.
     */
    public static final String CFG_KEY_MINMAX = "MinMax";
    
    /**
     * The configuration key for the number of terms to filter.
     */
    public static final String CFG_KEY_NUMBER = "Number";
    
    /**
     * The configuration key for the deep filtering setting.
     */
    public static final String CFG_KEY_DEEPFILTERING = "DeepFiltering";
    
    /**
     * The configuration key for the setting, specifying if unmodifiable terms 
     * have to be filtered too.
     */
    public static final String CFG_KEY_MODIFY_UNMODIFIABLE = 
        "MofiyUnmodifiable";
    
    /**
     * The configuration key for the column containing he documents to process.
     */
    public static final String CFG_KEY_DOCUMENT_COL = "DocCol";    
}

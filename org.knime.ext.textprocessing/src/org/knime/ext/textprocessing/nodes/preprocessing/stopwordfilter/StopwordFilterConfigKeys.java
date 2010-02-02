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
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.08.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class StopwordFilterConfigKeys {
    
    private StopwordFilterConfigKeys() { }

    /**
     * Config Key for file containing the stop words.
     */
    public static final String CFGKEY_FILE = "File";
    
    /**
     * Config Key for the activation of case sensitivity.
     */
    public static final String CFGKEY_CASE_SENSITIVE = "CS";

    /**
     * Config Key for the "use build in list" flag.
     */
    public static final String CFGKEY_USE_BUILDIN_LIST = "UseBuildInList";

    /**
     * Config Key for the selected build in list.
     */
    public static final String CFGKEY_BUILDIN_LIST = "BuildInList";   
}

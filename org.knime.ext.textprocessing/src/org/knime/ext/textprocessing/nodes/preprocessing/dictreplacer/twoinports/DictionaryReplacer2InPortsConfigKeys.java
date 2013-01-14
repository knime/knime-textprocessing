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
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.twoinports;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class DictionaryReplacer2InPortsConfigKeys {

    private DictionaryReplacer2InPortsConfigKeys() { /* empty */ }
    
    /**
     * Config key for the key column.
     */
    public static final String CFGKEY_KEYCOLUMN = "Replace Column";

    /**
     * Config key for the key column.
     */    
    public static final String CFGKEY_VALUECOLUMN = "Replacement Column";
}

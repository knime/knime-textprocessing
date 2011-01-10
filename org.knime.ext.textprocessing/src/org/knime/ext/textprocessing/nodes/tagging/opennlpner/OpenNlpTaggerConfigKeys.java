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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class OpenNlpTaggerConfigKeys {
    
    private OpenNlpTaggerConfigKeys() { }
    
    /**
     * The configuration key of unmodifiable flag of terms.
     */
    public static final String CFGKEY_UNMODIFIABLE = "SetUnmodifiable";
    
    /**
     * The configuration key for the ABNER tagging model.
     */
    public static final String CFGKEY_MODEL = "OPENNLP Model";
    
    /**
     * The configuration key for the dictionary flag.
     */
    public static final String CFGKEY_USE_DICT = "Use Dict";
    
    /**
     * The configuration key for the dictionary file.
     */
    public static final String CFGKEY_DICTFILE = "Dict File";
}

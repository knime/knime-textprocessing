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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

/**
 * Provides the configuration keys of the dictionary tagger node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DictionaryTaggerConfigKeys {

    private DictionaryTaggerConfigKeys() { }
    
    /**
     * The configuration key for the file containing the named entities.
     */
    public static final String CFGKEY_FILE = "File";
    
    /**
     * The configuration key of the tag to use.
     */
    public static final String CFGKEY_TAG = "Tag";

    /**
     * The configuration key of the case sensitive setting.
     */
    public static final String CFGKEY_CASE_SENSITIVE = "CS";
    
    /**
     * The configuration key of unmodifiable flag of terms.
     */
    public static final String CFGKEY_UNMODIFIABLE = "SetUnmodifiable";
}

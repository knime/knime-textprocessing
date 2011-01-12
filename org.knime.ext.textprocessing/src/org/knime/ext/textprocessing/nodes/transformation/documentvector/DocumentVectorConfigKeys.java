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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

/**
 * Provides the configuration keys for the document vector node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentVectorConfigKeys {

    private DocumentVectorConfigKeys() { }
    
    /**
     * The configuration key of the boolean value setting.
     */
    public static final String CFGKEY_BOOLEAN = "Boolean";
    
    /**
     * The configuration key of the column value setting.
     */
    public static final String CFGKEY_VALUE_COL = "Value_Col";
    
    /**
     * The configuration key of the column containing the documents.
     */
    public static final String CFGKEY_DOC_COL = "Document_Col";
    
    /**
     * The configuration key of the ignore tags flag.
     */
    public static final String CFGKEY_IGNORE_TAGS = "Ignore_Tags";    
}

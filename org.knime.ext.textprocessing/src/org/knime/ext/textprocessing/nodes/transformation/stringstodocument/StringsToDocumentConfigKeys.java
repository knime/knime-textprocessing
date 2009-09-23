/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class StringsToDocumentConfigKeys {

    private StringsToDocumentConfigKeys() { }
    
    /**
     * The configuration key of the title column.
     */
    public static final String CFGKEY_TITLECOL = "TitleCol";

    /**
     * The configuration key of the text column.
     */
    public static final String CFGKEY_TEXTCOL = "TextCol";    
    
    /**
     * The configuration key of the authors column.
     */
    public static final String CFGKEY_AUTHORSCOL = "AuthorsCol";
    
    /**
     * The configuration key of the author name split string.
     */
    public static final String CFGKEY_AUTHORSPLIT_STR = "AuthorSplitChar";

    /**
     * The configuration key of the document source.
     */
    public static final String CFGKEY_DOCSOURCE = "DocumentSource";
    
    /**
     * The configuration key of the document category.
     */
    public static final String CFGKEY_DOCCAT = "DocumentCategory";
    
    /**
     * The configuration key of the document type.
     */
    public static final String CFGKEY_DOCTYPE = "DocumentType";
    
    /**
     * The configuration key of the publication date.
     */
    public static final String CFGKEY_PUBDATE = "PublicationDate";
}

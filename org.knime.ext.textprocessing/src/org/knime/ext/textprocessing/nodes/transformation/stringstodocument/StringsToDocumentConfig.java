/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
public class StringsToDocumentConfig {

    public static final String DEF_AUTHORS_SPLITCHAR = ", ";
    
    public static final String DEF_AUTHOR_NAMES = "-";
    
    
    private int m_titleStringIndex = -1;
    
    private int m_authorsStringIndex = -1;

    private String m_authorsSplitChar = DEF_AUTHORS_SPLITCHAR;
    
    /**
     * @return the titleStringIndex
     */
    public int getTitleStringIndex() {
        return m_titleStringIndex;
    }

    /**
     * @param titleStringIndex the titleStringIndex to set
     */
    public void setTitleStringIndex(final int titleStringIndex) {
        m_titleStringIndex = titleStringIndex;
    }

    /**
     * @return the authorsStringIndex
     */
    public int getAuthorsStringIndex() {
        return m_authorsStringIndex;
    }

    /**
     * @param authorsStringIndex the authorsStringIndex to set
     */
    public void setAuthorsStringIndex(final int authorsStringIndex) {
        m_authorsStringIndex = authorsStringIndex;
    }

    /**
     * @return the authorsSplitChar
     */
    public String getAuthorsSplitChar() {
        return m_authorsSplitChar;
    }

    /**
     * @param authorsSplitChar the authorsSplitChar to set
     */
    public void setAuthorsSplitChar(final String authorsSplitChar) {
        m_authorsSplitChar = authorsSplitChar;
    }
}

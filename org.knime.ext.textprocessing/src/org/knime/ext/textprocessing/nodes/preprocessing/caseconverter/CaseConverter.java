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
 *   13.08.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.caseconverter;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class CaseConverter implements Preprocessing {

    /**
     * Constant for lower case conversion.
     */
    public static final String LOWER_CASE = "Lower case";
    
    /**
     * Constant for upper case conversion.
     */
    public static final String UPPER_CASE = "Upper case";
    
    
    private String m_case = CaseConverter.LOWER_CASE;
    
    /**
     * Creates new instance of <code>CaseConverter</code> with given case
     * to convert to.
     * 
     * @param caseConversion The case to convert to.
     */
    public CaseConverter(final String caseConversion) {
        m_case = caseConversion;
    }
    
    /**
     * Creates new instance of <code>CaseConverter</code> which converts to 
     * lower case by default.
     * 
     */
    public CaseConverter() {
        this(CaseConverter.LOWER_CASE);
    }    
    
    /**
     * @param caseConversion The case to convert to.
     */
    public void setCase(final String caseConversion) {
        m_case = caseConversion;
    }
    
    /**
     * @return The case to convert to.
     */
    public String getCase() {
        return m_case;
    }
    
    /**
     * {@inheritDoc}
     */
    public Term preprocess(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(CaseConverter.convert(w.getWord(), m_case)));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }    
    

    /**
     * Converts the case of the given string to lower or upper case depending
     * on the second string parameter, which specifies whether the conversion
     * is to lower case or to upper case.
     * 
     * @param str The string to convert
     * @param convCase The string specifying the was of conversion, to upper
     * or to lower case.
     * @return The converted string.
     */
    public static String convert(final String str, final String convCase) {
        String newTerm;
        if (convCase.equals(CaseConverter.LOWER_CASE)) {
            newTerm = str.toLowerCase();
        } else {
            newTerm = str.toUpperCase();
        }
        return newTerm;
    }
}

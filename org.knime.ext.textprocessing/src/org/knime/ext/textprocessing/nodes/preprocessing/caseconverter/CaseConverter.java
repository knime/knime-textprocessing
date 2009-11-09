/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 *   13.08.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.caseconverter;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class CaseConverter implements TermPreprocessing, StringPreprocessing {

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
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(CaseConverter.convert(w.getWord(), m_case)));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return CaseConverter.convert(str, m_case);
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

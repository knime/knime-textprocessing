/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
========================================================================
 *
 *  Copyright by 
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
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.kuhlenstemmer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class KuhlenStemmer implements TermPreprocessing, StringPreprocessing {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(KuhlenStemmer.class);

    /**
     * Creates new instance of PorterStemmer.
     */
    public KuhlenStemmer() { }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(KuhlenStemmer.stem(w.getWord()), w.getWhitespaceSuffix()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return KuhlenStemmer.stem(str);
    }

    /**
     * Returns the stemmed version of the given string.
     * @param str String to stem
     * @return If the word can not be stemmed or an error occurs the given
     * string is returned unmodified.
     */
    public static final String stem(final String str) {
        try {
            String results = internalStem(str);
            if (results != null) {
                return results;
            }
            return str;
        } catch (Exception e) {
            LOGGER.info("Stemming of string " + str + " is not possible");
            return str;
        }
    }

    private static String replaceIES(final String s) {
        return s.substring(0, s.length() - 3) + "y";
    }

    private static String replaceES(final String s) {
        String t = s.substring(0, s.length() - 2);
        if (t.endsWith("ch") || t.endsWith("sh")
               || t.endsWith("ss") || t.endsWith("zz") || t.endsWith("x")) {
            return t;
        } else if (t.endsWith("o")) {
            if (!isVowel(t.charAt(t.length() - 2))) {
                return t;
            }
        }
        return s;
    }

    private static String replaceS(final String s) {
        String t = s.substring(0, s.length() - 1);
        if (t.endsWith("e") || t.endsWith("oa") || t.endsWith("ea")) {
            return t;
        } else if (t.endsWith("y") || t.endsWith("o")) {
            if (isVowel(t.charAt(t.length() - 2))) {
                return t;
            }
        } else if (!isVowel(t.charAt(t.length() - 1))) {
            return t;
        }
        return s;
    }

    private static String replaceING(final String s) {
        String t = s.substring(0, s.length() - 3);
        if (t.endsWith("x") || isVowel(t.charAt(t.length() - 1))) {
            return t;
        } else if (t.length() > 1 && !isVowel(t.charAt(t.length() - 1))) {
            if (!isVowel(t.charAt(t.length() - 2))) {
                return t;
            }
            return t + "e";
        }
        return s;
    }

    private static String replaceED(final String s) {
        String t = s.substring(0, s.length() - 2);
        if (t.endsWith("x") || isVowel(t.charAt(t.length() - 1))) {
            return t;
        } else if (t.length() > 1 && !isVowel(t.charAt(t.length() - 1))) {
            if (!isVowel(t.charAt(t.length() - 2))) {
                return t;
            }
            return t + "e";
        }
        return s;
    }

    private static final String internalStem(final String str) {
        String s = str;
        boolean go = false;

        // Step 1, 2, 5, 3, 4, 5a, 5b:
        if (s.endsWith("ies")) {
            s = replaceIES(s);
        } else if (s.endsWith("es") && s.equals(str)) {
            s = replaceES(s);
        } else if (s.endsWith("'s") && s.equals(str)) {
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("s") && s.equals(str)) {
            s = replaceS(s);
        } else if (s.endsWith("ies'") && s.equals(str)) {
            s = s.substring(0, s.length() - 4) + "y";
        } else if (s.endsWith("es'") && s.equals(str)) {
            s = s.substring(0, s.length() - 3);
        } else if (s.endsWith("s'") && s.equals(str)) {
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("'") && s.equals(str)) {
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("ily") && s.equals(str)) {
            s = s.substring(0, s.length() - 3) + "y";
            go = true;
        } else if (s.endsWith("bly") && s.equals(str)) {
            s = s.substring(0, s.length() - 3) + "le";
            go = true;
        } else if (s.endsWith("ly") && s.equals(str)) {
            s = s.substring(0, s.length() - 2);
            go = true;
        }

        // Step 6:
        if (s.endsWith("ing") && (s.equals(str) || go)) {
            s = replaceING(s);
        }

        // Step 7:
        if (s.endsWith("ied") && (s.equals(str) || go)) {
            s = s.substring(0, s.length() - 3) + "y";
        }

        // Step 8:
        if (s.endsWith("ed") && (s.equals(str) || go)) {
            s = replaceED(s);
        }

        return s;
    }

    /**
     * Returns true is given Char is a vowel.
     * @param c Char to check if it is a vowel.
     * @return True if given Char is a vowel
     */
    private static final boolean isVowel(final char c) {
        if ((c == 'a') || (c == 'e') || (c == 'i') || (c == 'o')
                || (c == 'u') || (c == 'y')) {
            return true;
        }
        return false;
    }
}

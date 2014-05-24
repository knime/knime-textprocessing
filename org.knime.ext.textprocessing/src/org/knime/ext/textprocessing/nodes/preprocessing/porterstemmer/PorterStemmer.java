/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
========================================================================
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 * History
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.porterstemmer;

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
public class PorterStemmer implements TermPreprocessing, StringPreprocessing {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(PorterStemmer.class);

    /**
     * Creates new instance of PorterStemmer.
     */
    public PorterStemmer() { }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        final List<Word> words = term.getWords();
        final List<Word> newWords = new ArrayList<Word>();
        for (final Word w : words) {
            newWords.add(new Word(PorterStemmer.stem(w.getWord()), w.getWhitespaceSuffix()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return PorterStemmer.stem(str);
    }

    /**
     * Returns the stemed version of str.
     * @param str String to stem
     * @return If the word can not be stemmed or there
     * is some sort of error str is returned.
     */
    public static final String stem(final String str) {
        try {
            final String results = internalStem(str);
            if (results != null) {
                return results;
            }
            return str;
        } catch (Exception e) {
            LOGGER.info("Stemming of string " + str + " is not possible");
            return str;
        }
    }

    private static final String internalStem(final String str) {
        String s = str;
        // check for zero length
        if (s.length() > 0) {
            // all characters must be letters
            char[] c = s.toCharArray();
            for (int i = 0; i < c.length; i++) {
                if (!Character.isLetter(c[i])) {
                    return str;
                }
            }
        } else {
            return "";
        }
        s = step1a(s);
        s = step1b(s);
        s = step1c(s);
        s = step2(s);
        s = step3(s);
        s = step4(s);
        s = step5a(s);
        s = step5b(s);
        return s;
    }


    /**
     * Computes step 1a of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 1a of PorterStemmer
     * algorithm.
     */
    protected static final String step1a(final String str) {
        // SSES -> SS
        if (str.endsWith("sses")) {
            return str.substring(0, str.length() - 2);
        // IES -> I
        } else if (str.endsWith("ies")) {
            return str.substring(0, str.length() - 2);
        // SS -> S
        } else if (str.endsWith("ss")) {
            return str;
        // S ->
        } else if (str.endsWith("s")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Computes step 1b of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 1b of PorterStemmer
     * algorithm.
     */
    protected static final String step1b(final String str) {
        // (m > 0) EED -> EE
        if (str.endsWith("eed")) {
            if (stringMeasure(str.substring(0, str.length() - 3)) > 0) {
                return str.substring(0, str.length() - 1);
            }
            return str;
        // (*v*) ED ->
        } else if ((str.endsWith("ed"))
                && (containsVowel(str.substring(0, str.length() - 2)))) {
            return step1b2(str.substring(0, str.length() - 2));
        // (*v*) ING ->
        } else if ((str.endsWith("ing"))
                && (containsVowel(str.substring(0, str.length() - 3)))) {
            return step1b2(str.substring(0, str.length() - 3));
        } // end if
        return str;
    }

    /**
     * Computes step 1b2 of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 1b2 of PorterStemmer
     * algorithm.
     */
    protected static final String step1b2(final String str) {
        // AT -> ATE
        if (str.endsWith("at") || str.endsWith("bl") || str.endsWith("iz")) {
            return str + "e";
        } else if ((endsWithDoubleConsonent(str))
                && (!(str.endsWith("l") || str.endsWith("s")
                        || str.endsWith("z")))) {
            return str.substring(0, str.length() - 1);
        } else if ((stringMeasure(str) == 1) && (endsWithCVC(str))) {
            return str + "e";
        } else {
            return str;
        }
    }

    /**
     * Computes step 1c of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 1c of PorterStemmer
     * algorithm.
     */
    protected static final String step1c(final String str) {
        // (*v*) Y -> I
        if (str.endsWith("y")) {
            if (containsVowel(str.substring(0, str.length() - 1))) {
                return str.substring(0, str.length() - 1) + "i";
            }
        }
        return str;
    }

    /**
     * Computes step 2 of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 2 of PorterStemmer
     * algorithm.
     */
    protected static final String step2(final String str) {
        // (m > 0) ATIONAL -> ATE
        if ((str.endsWith("ational"))
                && (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "e";
            // (m > 0) TIONAL -> TION
        } else if ((str.endsWith("tional"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) ENCI -> ENCE
        } else if ((str.endsWith("enci"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) ANCI -> ANCE
        } else if ((str.endsWith("anci"))
                && (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1) + "e";
            // (m > 0) IZER -> IZE
        } else if ((str.endsWith("izer"))
                && (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1);
            // (m > 0) ABLI -> ABLE
        } else if ((str.endsWith("abli"))
                && (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
            return str.substring(0, str.length() - 1) + "e";
            // (m > 0) ENTLI -> ENT
        } else if ((str.endsWith("alli"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) ELI -> E
        } else if ((str.endsWith("entli"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) OUSLI -> OUS
        } else if ((str.endsWith("eli"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) IZATION -> IZE
        } else if ((str.endsWith("ousli"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) IZATION -> IZE
        } else if ((str.endsWith("ization"))
                && (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "e";
            // (m > 0) ATION -> ATE
        } else if ((str.endsWith("ation"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3) + "e";
            // (m > 0) ATOR -> ATE
        } else if ((str.endsWith("ator"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2) + "e";
            // (m > 0) ALISM -> AL
        } else if ((str.endsWith("alism"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) IVENESS -> IVE
        } else if ((str.endsWith("iveness"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
            // (m > 0) FULNESS -> FUL
        } else if ((str.endsWith("fulness"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
            // (m > 0) OUSNESS -> OUS
        } else if ((str.endsWith("ousness"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
            // (m > 0) ALITII -> AL
        } else if ((str.endsWith("aliti"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) IVITI -> IVE
        } else if ((str.endsWith("iviti"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3) + "e";
            // (m > 0) BILITI -> BLE
        } else if ((str.endsWith("biliti"))
                && (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5) + "le";
        }
        return str;
    }

    /**
     * Computes step 3 of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 3 of PorterStemmer
     * algorithm.
     */
    protected static final String step3(final String str) {
        // (m > 0) ICATE -> IC
        if ((str.endsWith("icate"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) ATIVE ->
        } else if ((str.endsWith("ative"))
                && (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
            return str.substring(0, str.length() - 5);
            // (m > 0) ALIZE -> AL
        } else if ((str.endsWith("alize"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) ICITI -> IC
        } else if ((str.endsWith("iciti"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) ICAL -> IC
        } else if ((str.endsWith("ical"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
            return str.substring(0, str.length() - 2);
            // (m > 0) FUL ->
        } else if ((str.endsWith("ful"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
            return str.substring(0, str.length() - 3);
            // (m > 0) NESS ->
        } else if ((str.endsWith("ness"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
            return str.substring(0, str.length() - 4);
        }
        return str;
    }

    /**
     * Computes step 4 of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 4 of PorterStemmer
     * algorithm.
     */
    protected static final String step4(final String str) {
        if ((str.endsWith("al"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
            // (m > 1) ANCE ->
        } else if ((str.endsWith("ance"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
            // (m > 1) ENCE ->
        } else if ((str.endsWith("ence"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
            // (m > 1) ER ->
        } else if ((str.endsWith("er"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
            // (m > 1) IC ->
        } else if ((str.endsWith("ic"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
            // (m > 1) ABLE ->
        } else if ((str.endsWith("able"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
            // (m > 1) IBLE ->
        } else if ((str.endsWith("ible"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
            // (m > 1) ANT ->
        } else if ((str.endsWith("ant"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) EMENT ->
        } else if ((str.endsWith("ement"))
                && (stringMeasure(str.substring(0, str.length() - 5)) > 1)) {
            return str.substring(0, str.length() - 5);
            // (m > 1) MENT ->
        } else if ((str.endsWith("ment"))
                && (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
            return str.substring(0, str.length() - 4);
            // (m > 1) ENT ->
        } else if ((str.endsWith("ent"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) and (*S or *T) ION ->
        } else if ((str.endsWith("sion") || str.endsWith("tion"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) OU ->
        } else if ((str.endsWith("ou"))
                && (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
            return str.substring(0, str.length() - 2);
            // (m > 1) ISM ->
        } else if ((str.endsWith("ism"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) ATE ->
        } else if ((str.endsWith("ate"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) ITI ->
        } else if ((str.endsWith("iti"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) OUS ->
        } else if ((str.endsWith("ous"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) IVE ->
        } else if ((str.endsWith("ive"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
            // (m > 1) IZE ->
        } else if ((str.endsWith("ize"))
                && (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
            return str.substring(0, str.length() - 3);
        }
        return str;
    }

    /**
     * Computes step 5a of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 5a of PorterStemmer
     * algorithm.
     */
    protected static final String step5a(final String str) {
        // (m > 1) E ->
        if ((stringMeasure(str.substring(0, str.length() - 1)) > 1)
                && str.endsWith("e")) {
            return str.substring(0, str.length() - 1);
        // (m = 1 and not *0) E ->
        } else if ((stringMeasure(str.substring(0, str.length() - 1)) == 1)
                && (!endsWithCVC(str.substring(0, str.length() - 1)))
                && (str.endsWith("e"))) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Computes step 5b of PorterStemmer algorithm.
     * @param str String to stem.
     * @return String modified under terms of step 5b of PorterStemmer
     * algorithm.
     */
    protected static final String step5b(final String str) {
        // (m > 1 and *d and *L) ->
        if (str.endsWith("l") && endsWithDoubleConsonent(str)
                && (stringMeasure(str.substring(0, str.length() - 1)) > 1)) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }


    /**
     * Returne true if given String ends with 's'.
     * @param str String to check if it ends with 's'.
     * @return True if given String end with 's'.
     */
    protected static final boolean endsWithS(final String str) {
        return str.endsWith("s");
    }

    /**
     * Returns true if given String contains a vowel.
     * @param str String to check if it contains a vowel.
     * @return True if given String contains a vowel.
     */
    protected static final boolean containsVowel(final String str) {
        char[] strchars = str.toCharArray();
        for (int i = 0; i < strchars.length; i++) {
            if (isVowel(strchars[i])) {
                return true;
            }
        }
        // no aeiou but there is y
        if (str.indexOf('y') > -1) {
            return true;
        }
        return false;
    }

    /**
     * Returns true is givn Char is a vowel.
     * @param c Char to check if it is a vowel.
     * @return True if given Char is a vowel
     */
    public static final boolean isVowel(final char c) {
        if ((c == 'a') || (c == 'e') || (c == 'i') || (c == 'o')
                || (c == 'u')) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if given String end with double consonent.
     * @param str String to check if it ends with double consonent.
     * @return True if given Strign ends with double consonent.
     */
    protected static final boolean endsWithDoubleConsonent(final String str) {
        char c = str.charAt(str.length() - 1);
        if (str.length() >= 2) {
            if (c == str.charAt(str.length() - 2)) {
                if (!containsVowel(str.substring(str.length() - 2))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a CVC measure for the string.
     * @param str String to measure
     * @return The CVC measure of given String.
     */
    protected static final int stringMeasure(final String str) {
        int count = 0;
        boolean vowelSeen = false;
        char[] strchars = str.toCharArray();

        for (int i = 0; i < strchars.length; i++) {
            if (isVowel(strchars[i])) {
                vowelSeen = true;
            } else if (vowelSeen) {
                count++;
                vowelSeen = false;
            }
        }
        return count;
    }

    /**
     * Returns true if given String ends with a CVC phrase.
     * @param str String to check if it ends with a CVC phrase.
     * @return True if given String end with a CVC phrase.
     */
    protected static final boolean endsWithCVC(final String str) {
        char c, v, c2 = ' ';
        if (str.length() >= 3) {
            c = str.charAt(str.length() - 1);
            v = str.charAt(str.length() - 2);
            c2 = str.charAt(str.length() - 3);
        } else {
            return false;
        }

        if ((c == 'w') || (c == 'x') || (c == 'y')) {
            return false;
        } else if (isVowel(c)) {
            return false;
        } else if (!isVowel(v)) {
            return false;
        } else if (isVowel(c2)) {
            return false;
        }
        return true;
    }
}

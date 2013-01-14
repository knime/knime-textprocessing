/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   11.11.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.hyphenator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author thiel, University of Konstanz
 */
public class Hyphenator implements TermPreprocessing, StringPreprocessing {

    private String m_separator;

    private Map<String, int[]> m_patterns;

    /**
     * Creates an instance of <code>Hyphenator</code> with given m_patterns
     * and m_separator to use.
     * @param patterns The m_patterns to use.
     * @param separator The m_separator to set.
     */
    public Hyphenator(final HyphenationPatterns patterns,
            final String separator) {
        m_separator = separator;
        m_patterns = patterns.getPatterns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : term.getWords()) {
            newWords.add(new Word(preprocessString(w.getText())));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        String returnWord = "";
        List<String> hyphens = hyphenate(str);
        int hcount = 0;
        for (String hyphen : hyphens) {
            hcount++;
            returnWord += hyphen;
            if (hcount < hyphens.size()) {
                returnWord += m_separator;
            }
        }
        return returnWord;
    }

    private List<String> hyphenate(final String word) {
        List<String> hyphenatedWord = new LinkedList<String>();
        if (word.length() <= 2) {
            hyphenatedWord.add(word);
            return hyphenatedWord;
        }

        int[] points;
        String lowerCaseWord = "_" + word.toLowerCase() + "_";
        points = new int[lowerCaseWord.length() + 1];
        int[] tpoints;
        BACK:
        for (int i = 0; i < lowerCaseWord.length(); i++) {
            for (int j = lowerCaseWord.length(); j > i; j--) {
                if (m_patterns.containsKey(lowerCaseWord.substring(i, j))) {
                    tpoints = m_patterns.get(lowerCaseWord.substring(i, j));
                    for (int k = 0; k < tpoints.length; k++) {
                        if (points[i + k] <= tpoints[k]) {
                            points[i + k] = tpoints[k];
                        }
                    }
                    continue BACK;
                }
            }
        }
        String tstr = "";
        for (int i = 0; i < (points.length - 3); i++) {
            tstr += word.charAt(i);
            if ((points[i + 2] % 2) == 1) {
                hyphenatedWord.add(tstr);
                tstr = "";
            }
        }
        hyphenatedWord.add(tstr);
        return hyphenatedWord;
    }
}

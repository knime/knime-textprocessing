/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
========================================================================
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
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
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import java.util.HashSet;
import java.util.Set;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 * A stop word filter, filtering strings contained in the given set of stop
 * words. See {@link StopWordFilter#preprocessTerm(Term)} for details to filter
 * terms.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StopWordFilter implements TermPreprocessing, StringPreprocessing {

    private Set < String > m_wordList;

    private boolean m_caseSensitive;

    /**
     * Creates new instance of <code>StopWordFilter</code> with given
     * list containing the stop words to filter.
     *
     * @param stopWordList List with stop word to filter.
     * @param caseSensitive If set <code>true</code> the case matters when
     * filtering given string, otherwise not.
     * @since 3.1
     */
    public StopWordFilter(final Set < String > stopWordList, final boolean caseSensitive) {
        m_caseSensitive = caseSensitive;
        if (caseSensitive) {
            m_wordList = stopWordList;
        } else {
            m_wordList = convert(stopWordList);
        }
    }

    /**
     * Adds the given stop words to the existing stop word list. If caseSensitive is set {@code true} the stop words
     * will be added lower case.
     * @param stopWords The stop word to add.
     * @param caseSensitive If caseSensitive is set {@code true} the stop words will be added lower case.
     * @since 3.1
     */
    public void addStopWords(final Set<String> stopWords, final boolean caseSensitive) {
        if (caseSensitive) {
            m_wordList.addAll(stopWords);
        } else {
            m_wordList.addAll(convert(stopWords));
        }
    }

    private Set<String> convert(final Set<String> set) {
        Set<String> convertedSet = new HashSet<String>();
        for (String s : set) {
            convertedSet.add(s.toLowerCase());
        }
        return convertedSet;
    }

    /**
     * Returns true if given String is a stop word, false if not.
     * @param word String to check if it is a stop word.
     * @return True if given String is a stop word.
     */
    public boolean isStopWord(final String word) {
        return m_wordList.contains(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        final String t;
        if (m_caseSensitive) {
            t = term.getText();
        } else {
            t = term.getText().toLowerCase();
        }

        if (isStopWord(t)) {
            return null;
        }
        return term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        final String t;
        if (m_caseSensitive) {
            t = str;
        } else {
            t = str.toLowerCase();
        }

        if (isStopWord(t)) {
            return null;
        }
        return str;
    }
}

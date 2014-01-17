/*
 * ------------------------------------------------------------------------
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
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.tartarus.snowball.SnowballStemmer;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class WrappedSnowballStemmer implements TermPreprocessing,
        StringPreprocessing {

    private SnowballStemmer m_stemmer;

    /**
     * Creates new instance of <code>WrappedSnowballStemmer</code> with given
     * stemmer to use.
     *
     * @param stemmer The stemmer to use.
     */
    public WrappedSnowballStemmer(final SnowballStemmer stemmer) {
        m_stemmer = stemmer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            m_stemmer.setCurrent(w.getText());
            m_stemmer.stem();
            newWords.add(new Word(m_stemmer.getCurrent(), w.getWhitespaceSuffix()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        m_stemmer.setCurrent(str);
        m_stemmer.stem();
        return m_stemmer.getCurrent();
    }
}

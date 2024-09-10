/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * Class used to replace strings or terms in documents.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryReplacer implements TermPreprocessing, StringPreprocessing {

    /**
     * Map containing the words to replaces and their replacement strings.
     */
    private final Map<String, String> m_replaceDict;

    /**
     * The tokenizer used for word tokenization.
     */
    private final Tokenizer m_wordTokenizer;

    /**
     * String used to replace words that are not available in the replacement dictionary.
     */
    private final String m_replacement;

    /**
     * Default whitespace suffix.
     */
    private static final String DEFAULT_WHITESPACE_SUFFIX = " ";

    /**
     * Creates new instance of {@code DictionaryReplacer} with give dictionary, containing key value pairs for
     * replacement.
     *
     * @param replaceDict The dictionary consisting of key value pairs for replacement (keys will be replaced by their
     *            corresponding values).
     * @deprecated use {@link DictionaryReplacer#DictionaryReplacer(Map, String) DictionaryReplacer(Map, String)}
     *             instead to define the word tokenizer.
     * @since 3.1
     */
    @Deprecated
    public DictionaryReplacer(final Hashtable<String, String> replaceDict) {
        // uses the tokenizer from preference page
        this(replaceDict, TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates new instance of {@link DictionaryReplacer} with give dictionary, containing key value pairs for
     * replacement.
     *
     * @param replaceDict The dictionary consisting of key value pairs for replacement (keys will be replaced by their
     *            corresponding values).
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public DictionaryReplacer(final Map<String, String> replaceDict, final String tokenizerName) {
        this(replaceDict, null, tokenizerName);
    }

    /**
     * Creates new instance of {@link DictionaryReplacer} with give dictionary, containing key value pairs for
     * replacement.
     *
     * @param replaceDict The dictionary consisting of key value pairs for replacement (keys will be replaced by their
     *            corresponding values).
     * @param replacement The string used to replace words that are not contained in the dictionary.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 4.0
     */
    public DictionaryReplacer(final Map<String, String> replaceDict, final String replacement,
        final String tokenizerName) {
        m_replaceDict = new HashMap<>(replaceDict);
        m_wordTokenizer = DefaultTokenization.getWordTokenizer(tokenizerName);
        m_replacement = replacement != null ? replacement.trim() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        final String word = term.getText();
        final String newWord = m_replaceDict.get(word);
        if (newWord != null) {
            return createTerm(newWord, term.getTags(), term.isUnmodifiable());
        } else if (m_replacement != null) {
            return createTerm(m_replacement, new ArrayList<>(), false);
        }
        return term;
    }

    /**
     * Creates a new {@link Term}.
     *
     * @param word The word.
     * @param tags List of {@link Tag Tags} from replaced term.
     * @param unmodifiable Modifiability of the new term.
     * @return A new term.
     */
    private Term createTerm(final String word, final List<Tag> tags, final boolean unmodifiable) {
        if (word.isEmpty()) {
            return null;
        }
        final List<String> tokenizedWords = m_wordTokenizer.tokenize(word);

        final List<Word> newWords = new ArrayList<>();
        for (final String s : tokenizedWords) {
            // TODO: here the original white space suffix of the term should be added as suffix of last word.
            newWords.add(new Word(s, DEFAULT_WHITESPACE_SUFFIX));
        }
        return new Term(newWords, tags, unmodifiable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        final String newStr = m_replaceDict.get(str);
        if (newStr != null) {
            return newStr;
        } else if (m_replacement != null) {
            return m_replacement;
        }
        return str;
    }

}

/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   Aug 9, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.language.turkish.nodes.preprocessing.zemberekstemmer;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.SentencePreprocessing;

import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.TurkishMorphology.Builder;
import zemberek.morphology.analysis.SentenceWordAnalysis;

/**
 * A class for stemming Turkish words. The stemming functionality comes from the ZemberekNLP library.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class WrappedZemberekStemmer implements SentencePreprocessing {

    /**
     * The {@link TurkishMorphology} object used for ambiguity resolution and stemming.
     */
    private final TurkishMorphology m_stemmer;

    /**
     * Defines if stems should maintain the letter cases of the original word.
     */
    private final boolean m_maintainCase;

    /**
     * Creates a new instance of {@code WrappedZemberekStemmer}.
     *
     * @param maintainCase Set true to maintain the letter cases of the word to stem.
     */
    WrappedZemberekStemmer(final boolean maintainCase) {
        m_maintainCase = maintainCase;
        m_stemmer = new Builder().addDefaultBinaryDictionary().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sentence preprocessSentence(final Sentence sentence, final boolean processUnmodifiableTerms) {
        return stemTermsAndGetSentence(sentence, processUnmodifiableTerms);
    }

    /**
     * This method stems {@link Term Terms} contained in the input {@link Sentence} and returns a new {@code Sentence}
     * based on the stemmed {@code Terms}.
     *
     * @param sentence The {@code Sentence} containing the {@code Terms} to stem.
     * @return A new {@code Sentence} containing the stemmed {@code Terms}.
     */
    private Sentence stemTermsAndGetSentence(final Sentence sentence, final boolean processUnmodifiableTerms) {
        final List<Term> terms = sentence.getTerms();
        final List<Term> newTerms = new ArrayList<>();
        final int numberOfWords = terms.stream()//
            .map(Term::getWords)//
            .mapToInt(List::size)//
            .sum();

        // disambiguation and morphological analysis
        final List<SentenceWordAnalysis> analysisResults =
            m_stemmer.analyzeAndDisambiguate(sentence.getText()).getWordAnalyses();

        // counter to keep track of the current analysis result
        int counter = 0;
        if (analysisResults.size() == numberOfWords) {
            for (final Term term : terms) {
                if (!term.getText().isEmpty() && (!term.isUnmodifiable() || processUnmodifiableTerms)) {
                    // getting the stem of each word
                    for (final Word word : term.getWords()) {
                        String stem = analysisResults.get(counter).getBestAnalysis().getStem();
                        stem = m_maintainCase ? maintainCase(word.getText(), stem) : stem;
                        final List<Word> newWords = new ArrayList<>();
                        newWords.add(new Word(stem, word.getWhitespaceSuffix()));
                        newTerms.add(new Term(newWords, term.getTags(), term.isUnmodifiable()));
                        counter++;
                    }
                } else {
                    newTerms.add(term);
                    counter += term.getWords().size();
                }
            }
        } else {
            // this is only the case if there is no one-to-one mapping between analyzed words and incoming words
            for (final SentenceWordAnalysis swa : analysisResults) {
                final String stem = swa.getBestAnalysis().getStem();
                final List<Word> newWords = new ArrayList<>();
                newWords.add(new Word(stem, " "));
                newTerms.add(new Term(newWords, new ArrayList<>(), false));
            }
        }

        return new Sentence(newTerms);
    }

    /**
     * This method is used to maintain the case of word, since the case gets lost due to the stemming behavior of the
     * Zemberek library.
     *
     * @param word The original word
     * @param stem The word stem
     * @return A stem that resembles the original word in terms of letter case.
     */
    private static String maintainCase(final String word, final String stem) {
        // return a substr of the original word, if the substr equals the stem
        if (stem.length() <= word.length()) {
            final String substr = word.substring(0, stem.length());
            if (substr.equalsIgnoreCase(stem)) {
                return substr;
            }
        }

        if (!word.chars().anyMatch(Character::isLowerCase)) {
            // return all upper case stem if original word is all upper case
            return stem.toUpperCase();
        } else if (!word.chars().anyMatch(Character::isUpperCase)) {
            // return all lower case stem if original word is all lower case
            return stem;
        } else {
            // if original word has mixed case, try to apply the it on the stem
            final StringBuilder finalStem = new StringBuilder();
            for (int i = 0; i < stem.toCharArray().length; i++) {
                char currentCharOfStem = stem.charAt(i);
                if (i < word.length()) {
                    final char currentCharOfWord = word.charAt(i);
                    if (!Character.isLowerCase(currentCharOfWord)
                        && (i == 0 || Character.toLowerCase(currentCharOfWord) == currentCharOfStem)) {
                        currentCharOfStem = Character.toUpperCase(currentCharOfStem);
                    }
                    finalStem.append(currentCharOfStem);
                } else {
                    finalStem.append(stem.substring(i));
                    break;
                }
            }
            return finalStem.toString();
        }
    }
}

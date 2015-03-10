/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   10.03.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.data.hittisau.legancy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.data.hittisau.Document;
import org.knime.ext.textprocessing.data.hittisau.Sentence;
import org.knime.ext.textprocessing.data.hittisau.Serializer;

/**
 *
 * @author Kilian
 */
public class DocumentLegacy implements Document {

    private UUID m_uuid;

    private String m_title;

    private int m_numberOfTerms = 0;

    private DocumentMetaInfo m_metaInfo;

    private String[] m_terms;

    private String[] m_whiteSpaces;

    private TagBuilder[] m_tagBuilder;

    private InternalTerm[][] m_sentences;

    String[] getWhitespaces()  {
        return m_whiteSpaces;
    }

    String[] getTerms()  {
        return m_terms;
    }

    TagBuilder[] getTagBuilders() {
        return m_tagBuilder;
    }

    InternalTerm[][] getSentences() {
        return m_sentences;
    }

    DocumentLegacy(final UUID uuid, final String title, final int numberOfTerms,
        final DocumentMetaInfo metaInfo, final String[] terms, final String[] whiteSpaces,
        final TagBuilder[] tagBuilder, final InternalTerm[][] sentences) {
        m_uuid = uuid;
        m_title = title;
        m_numberOfTerms = numberOfTerms;
        m_metaInfo = metaInfo;
        m_terms = terms;
        m_whiteSpaces = whiteSpaces;
        m_tagBuilder = tagBuilder;
        m_sentences = sentences;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID() {
        return m_uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return m_title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Sentence> iterator() {
        return new SentenceIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Sentence> stream() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTerms() {
        return m_numberOfTerms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSentences() {
        return m_sentences.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentMetaInfo getMetaInformation() {
        return m_metaInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializer createSerializer() {
        // TODO Auto-generated method stub
        return null;
    }

    class SentenceIterator implements Iterator<Sentence> {

        private int m_currentIndex = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            if (m_currentIndex < m_sentences.length) {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Sentence next() {
            InternalTerm[] internalSentence = m_sentences[m_currentIndex];

            List<Term> legacyTerms = new ArrayList<>(internalSentence.length);

            for (int i = 0; i < internalSentence.length; i++) {

                InternalTerm it = internalSentence[i];
                String word = m_terms[it.getTermIndex()];
                String ws = m_whiteSpaces[it.getWhiteSpaceIndex()];

                // Words (incl. Whitespaces)
                List<Word> legacyWords = new ArrayList<>(1);
                legacyWords.add(new Word(word, ws));

                // Tags
                List<Tag> legacyTags = new ArrayList<>();
                for (int tbi = 0; tbi < m_tagBuilder.length; tbi++) {
                    TagBuilder tb = m_tagBuilder[tbi];

                    int noTags = it.getTags()[tbi].length;
                    for (int nt = 0; nt < noTags; nt++) {
                        Tag legacyTag = new Tag(tb.buildTag(it.getTags()[tbi][nt]).getTagValue(), tb.getType());
                        legacyTags.add(legacyTag);
                    }
                }

                Term t = new Term(legacyWords,  legacyTags, it.isImmutable());
                legacyTerms.add(t);
            }

            return new org.knime.ext.textprocessing.data.Sentence(legacyTerms);
        }
    }

}

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
 *   10.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.betterdoc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.hittisau.Document;
import org.knime.ext.textprocessing.data.hittisau.Sentence;
import org.knime.ext.textprocessing.data.hittisau.Serializer;
import org.knime.ext.textprocessing.data.hittisau.legancy.DocumentLegacySerializer;

/**
 *
 * @author Alexander
 */
public class FastDocument implements Document, TermLookupTable, TagBuilderLookupTable {

    private UUID m_uuid;

    private String m_title;

    private int m_numberOfTerms = 0;

    private DocumentMetaInfo m_metaInfo;

    private String[] m_terms;

    private String[] m_whiteSpaces;

    private TagBuilder[] m_tagBuilders;

    private FastSentence[] m_sentences;

    String[] getWhitespaces()  {
        return m_whiteSpaces;
    }

    String[] getTerms()  {
        return m_terms;
    }

    TagBuilder[] getTagBuilders() {
        return m_tagBuilders;
    }

    FastSentence[] getSentences() {
        return m_sentences;
    }

    public FastDocument(final UUID uuid, final String title, final int numberOfTerms,
        final DocumentMetaInfo metaInfo, final String[] terms, final String[] whiteSpaces,
        final TagBuilder[] tagBuilder, final FastSentence[] sentences) {
        m_uuid = uuid;
        m_title = title;
        m_numberOfTerms = numberOfTerms;
        m_metaInfo = metaInfo;
        m_terms = terms;
        m_whiteSpaces = whiteSpaces;
        m_tagBuilders = tagBuilder;
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
        return  Arrays.stream(m_sentences);
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
        return new DocumentLegacySerializer();
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
            return m_sentences[m_currentIndex++];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDocument)) {
            return false;
        }
        return ((FastDocument)obj).getUUID().equals(getUUID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTermAt(final int i) {
        if (i < 0) {
            return m_whiteSpaces[i + 1];
        } else {
            return m_terms[i];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagBuilder getTagBuilderAt(final int i) {
        return m_tagBuilders[i];
    }
}

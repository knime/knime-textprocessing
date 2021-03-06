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
 *   May 9, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.MultipleTaggedEntity;
import org.knime.ext.textprocessing.nodes.tagging.NamedEntityMatcher;
import org.knime.ext.textprocessing.nodes.tagging.SentenceTagger;

/**
 * The {@code MultipleDictionarySentenceTagger} is used for tagging documents with different tags and dictionaries.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MultipleDictionarySentenceTagger implements SentenceTagger {

    /**
     * Array of {@link SingleDictionaryTagger SingleDictionaryTaggers}.
     */
    private final List<SingleDictionaryTagger> m_singleTagger;

    /**
     * Creates a new instance of {@code MultipleDictionarySentenceTagger}.
     */
    MultipleDictionarySentenceTagger() {
        m_singleTagger = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MultipleTaggedEntity> tagEntities(final Sentence sentence) {
        Map<String, MultipleTaggedEntity> foundEntities = new LinkedHashMap<>();

        final String origSentenceStr = sentence.getText();

        for (SingleDictionaryTagger singleTagger : m_singleTagger) {
            for (String entity : singleTagger.getEntities()) {
                if (singleTagger.getMatcher().matchWithSentence(entity, origSentenceStr)) {
                    addToListAndCheckOccurrence(entity, singleTagger.getTag(), singleTagger.getMatcher(),
                        foundEntities);
                }
            }
        }
        return new ArrayList<>(foundEntities.values());
    }

    /**
     * This method checks if the map of {@code MultipleTaggedEntity}s already contains the entity. If it is the case,
     * this method adds a new combination of {@code Tag} and {@code NamedEntityMatcher} to the specific
     * MultipleTaggedEntity, otherwise creates a new.
     *
     * @param entity The found entity as String.
     * @param tag The tag to be used for tagging the entity.
     * @param matcher The matcher to be used for matching the entity with a word.
     * @param mtes The map of entities to their {@code MultipleTaggedEntity}.
     */
    private static void addToListAndCheckOccurrence(final String entity, final Tag tag,
        final NamedEntityMatcher matcher, final Map<String, MultipleTaggedEntity> mtes) {
        MultipleTaggedEntity mte = mtes.remove(entity);
        if (mte == null) {
            mte = new MultipleTaggedEntity(entity);
        }
        mte.addTagMatcherCombination(tag, matcher);
        mtes.put(entity, mte);
    }

    /**
     * Adds an instance of {@link SingleDictionaryTagger}.
     *
     * @param singleTagger The {@code SingleDictionaryTagger} to add.
     */
    void add(final SingleDictionaryTagger singleTagger) {
        m_singleTagger.add(singleTagger);
    }

}

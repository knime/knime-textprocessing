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
import java.util.List;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.MultipleTaggedEntity;
import org.knime.ext.textprocessing.nodes.tagging.SentenceTagger;

/**
 * The {@code MultipleDictionarySentenceTagger} is used for tagging documents with different tags and dictionaries.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MultipleDictionarySentenceTagger implements SentenceTagger {

    /**
     * List of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries used for
     * tagging.
     */
    private List<DictionaryTaggerConfiguration> m_configs;

    /**
     * Creates a new instance of {@code MultipleDictionarySentenceTagger} which is used to tag {@code Sentence}s of
     * {@code Document}s based on multiple dictionaries.
     *
     * @param namedEntityFinder List of {@code DictionaryTaggerConfiguration} containing configurations for all
     *            dictionaries.
     */
    MultipleDictionarySentenceTagger(final List<DictionaryTaggerConfiguration> configs) {
        m_configs = configs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MultipleTaggedEntity> tagEntities(final Sentence sentence) {
        List<MultipleTaggedEntity> foundEntities = new ArrayList<>();

        final String origSentenceStr = sentence.getText();

        for (DictionaryTaggerConfiguration config : m_configs) {
            for (String entity : config.getEntities()) {
                NamedEntityMatcher matcher =
                    new NamedEntityMatcher(config.getCaseSensitivityOption(), config.getExactMatchOption());
                if (matcher.matchWithSentence(entity, origSentenceStr)) {
                    addToListAndCheckOccurrence(entity, config.getTag(), matcher, foundEntities);
                }
            }
        }

        return foundEntities;
    }

    /**
     * This method checks if the list of {@code MultipleTaggedEntity}s already contains the entity. If it is the case,
     * this method adds a new combination of {@code Tag} and {@code NamedEntityMatcher} to the specific
     * MultipleTaggedEntity.
     *
     * @param entity The found entity as String.
     * @param tag The tag to be used for tagging the entity.
     * @param matcher The matcher to be used for matching the entity with a word.
     * @param mtes The list of entities.
     * @return The updated list of entities.
     */
    private List<MultipleTaggedEntity> addToListAndCheckOccurrence(final String entity, final Tag tag,
        final NamedEntityMatcher matcher, final List<MultipleTaggedEntity> mtes) {
        MultipleTaggedEntity mte = null;
        for (int i = 0; i < mtes.size(); i++) {
            if (entity.equals(mtes.get(i).getEntity())) {
                mte = mtes.get(i);
                mtes.remove(i);
                break;
            }
        }
        if (mte == null) {
            mte = new MultipleTaggedEntity(entity);
        }
        mte.addTagMatcherCombination(tag, matcher);
        mtes.add(mte);
        return mtes;
    }

}

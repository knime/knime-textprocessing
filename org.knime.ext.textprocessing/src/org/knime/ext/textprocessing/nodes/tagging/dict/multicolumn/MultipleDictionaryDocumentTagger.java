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
 * ---------------------------------------------------------------------
 *
 * History
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

/**
 * A dictionary based tagger providing methods to detect and tag named entities in a given sentence. The named entities
 * to detect have to be specified when calling the constructor.
 *
 * @author Kilian Thiel, University of Konstanz
 * @since 3.6
 */
public class MultipleDictionaryDocumentTagger extends MultipleAbstractDocumentTagger {

    private List<Set<String>> m_dictionaries;

    private List<Tag> m_tags;

    private List<Boolean> m_caseSensitivity;

    private List<Boolean> m_exactMatch;

    /**
     * Creates a new instance of <code>DictionaryDocumentTagger</code> with given flag to set found named entities
     * unmodifiable, to ignore the case of the named entities to detect, to compare entities by exact match or contains
     * match, the tag to assign to the found named entities and the set of named entities to watch out for.
     *
     * @param setUnmodifiable If <code>true</code> found named entities are set unmodifiable, otherwise not.
     * @param dictionaries
     * @param tags
     * @param caseSensitive If <code>false</code> the case of named entities and words of the sentences are ignored,
     *            otherwise not.
     * @param exactMatch If <code>true</code> terms must match exactly with the entities to find, to be recognized.
     *            Otherwise terms only need to contain the entity string to find.
     * @param tokenizerName The tokenizer used for word tokenization.
     */
    public MultipleDictionaryDocumentTagger(final List<Boolean> setUnmodifiable, final List<Set<String>> dictionaries,
        final List<Tag> tags, final List<Boolean> caseSensitive, final List<Boolean> exactMatch,
        final String tokenizerName) {
        super(tags, setUnmodifiable, caseSensitive, exactMatch, tokenizerName);

        if (dictionaries == null) {
            throw new NullPointerException("Set of named entities may not be null!");
        } else if (tags == null) {
            throw new NullPointerException("Specified tag may not be null!");
        } else if (caseSensitive == null || exactMatch == null || setUnmodifiable == null) {
            throw new NullPointerException("Lists containing the settings cannot be null.");
        } else if (!(caseSensitive.size() == setUnmodifiable.size() && setUnmodifiable.size() == exactMatch.size())) {
            throw new InvalidSettingsException("Lists containing the settings cannot be of different size.");
        }

        m_dictionaries = dictionaries;
        m_tags = tags;
        m_caseSensitivity = caseSensitive;
        m_exactMatch = exactMatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<TaggedEntity> foundEntities = new ArrayList<TaggedEntity>();


        String sentenceStr = sentence.getText();
        if (!m_caseSensitivity.get(0)) {
            sentenceStr = sentenceStr.toLowerCase();
        }

        for (String ne : m_dictionaries.get(0)) {
            String entity = ne;
            if (!m_caseSensitivity.get(0)) {
                entity = entity.toLowerCase();
            }

            if (sentenceStr.contains(entity)) {
                TaggedEntity taggedEntity = new TaggedEntity(ne, m_tags.get(0).getTagValue());
                foundEntities.add(taggedEntity);
            }
        }

        return foundEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }
}

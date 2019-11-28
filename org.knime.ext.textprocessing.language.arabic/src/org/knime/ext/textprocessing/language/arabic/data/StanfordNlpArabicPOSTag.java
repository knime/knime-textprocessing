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
 *   Nov 26, 2019 (Julian Bunzel, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ext.textprocessing.language.arabic.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

/**
 * This class provides methods given by the {@link TagBuilder} interface to use the StanfordNLP Arabic tag set.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public final class StanfordNlpArabicPOSTag implements TagBuilder {

    private enum StanfordNlpArabicTagSet {
        /** Unkown type. */
        UNKNOWN,
        /** Determiner. */
        DT,
        /** Coordinating conjunction. */
        CC,
        /** Preposition or subordinating conjunction. */
        IN,
        /** Adjective. */
        JJ,
        /** Noun, singular or mass. */
        NN,
        /** Noun, plural. */
        NNS,
        /** Proper noun, singular. */
        NNP,
        /** Proper noun, plural. */
        NNPS,
        /** Personal pronoun. */
        PRP,
        /** Possessive pronoun. */
        PRP$,
        /** Adverb. */
        RB,
        /** Particle. */
        RP,
        /** Symbol. */
        SYM,
        /** Interjection. */
        UH,
        /** Imperative. */
        VB,
        /** Verb, past tense. */
        VBD,
        /** Verbal nouns/gerund. */
        VBG,
        /** Passive verb. */
        VBN,
        /** Imperfect verbs, present tense. */
        VBP,
        /** Cardinal Number. **/
        CD,
        /** Adjective, comparative. **/
        JJR,
        /** Wh-pronoun. **/
        WP,
        /** Wh-adverb. **/
        WRB,
        /** Ordinal number/numerical adjective. */
        ADJ_NUM,
        /** Determinant noun, singular or mass. */
        DTNN,
        /** Determinant noun, plural. */
        DTNNS,
        /** Determinant proper noun, singular. */
        DTNNP,
        /** Determinant proper noun, plural. */
        DTNNPS,
        /** Determinant adjective. */
        DTJJ,
        /** Determinant comparative adjective. */
        DTJJR,
        /** Nominal quantifier. */
        NOUN_QUANT,
        /** Verbal nominal/active or passive participles. */
        VN,
        /** Punctuation. */
        PUNC;
    }

    /**
     * The tag type constant for the StanfordNLP Arabic Part-of-speech tag set.
     */
    private static final String TAG_TYPE = "ARABPOS";

    @Override
    public Tag buildTag(final String value) {
        for (final StanfordNlpArabicTagSet pos : StanfordNlpArabicTagSet.values()) {
            if (pos.name().equals(value)) {
                return new Tag(pos.name(), TAG_TYPE);
            }
        }
        return new Tag(StanfordNlpArabicTagSet.UNKNOWN.name(), TAG_TYPE);
    }

    @Override
    public List<String> asStringList() {
        return Stream.of(StanfordNlpArabicTagSet.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Override
    public Set<Tag> getTags() {
        return Stream.of(StanfordNlpArabicTagSet.values())//
            .map(e -> new Tag(e.name(), TAG_TYPE))//
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getType() {
        return TAG_TYPE;
    }

}

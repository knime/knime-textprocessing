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
 *   04.07.2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum contains a modified version of the part-of-speech tag set created by Crabbé & Candite in 2008.
 * Originally, it is based on the French Treebank (FTB) tag set, but the Stanford CoreNLP group transformed it
 * to get better results with their French POS tagger.
 * To create a a valid {@link org.knime.ext.textprocessing.data.Tag} instance use
 * {@link org.knime.ext.textprocessing.data.ExtendedFTBCrabbeCanditeTagSet#getTag()}, i.e:
 * <br><br>
 * {@code Tag t = ExtendedFTBCrabbeCanditeTag.A.getTag();}
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.6
 */
public enum ExtendedFTBCrabbeCanditeTagSet {

        /** Unknown type. */
        UNKNOWN,

        /** Adjective. General fall-back category. */
        A,
        /** Adjective. */
        ADJ,
        /** Interrogative adjective. **/
        ADJWH,
        /** Adverb. */
        ADV,
        /** Interrogative adverb. */
        ADVWH,
        /** Conjunction. General fall-back category. */
        C,
        /** Coordinating conjunction. */
        CC,
        /** Clitic pronoun. General fall-back category. */
        CL,
        /** Object clitic pronoun. */
        CLO,
        /** Reflexive clitic pronoun. */
        CLR,
        /** Subject clitic pronoun. */
        CLS,
        /** Subordinating conjunction. */
        CS,
        /** Determiner. */
        DET,
        /** Interrogative determiner. */
        DETWH,
        /** Foreign word. */
        ET,
        /** Interjection. */
        I,
        /** Noun. General fall-back category. */
        N,
        /** Common noun. */
        NC,
        /** Proper noun. */
        NPP,
        /** Preposition. */
        P,
        /** Prefix. */
        PREF,
        /** Full pronoun. */
        PRO,
        /** Relative pronoun. */
        PROREL,
        /** Interrogative pronoun. */
        PROWH,
        /** Punctuation. */
        PUNC,
        /** Symbols. */
        SYM,
        /** Indicative or conditional verb. */
        V,
        /** Imperative verb. */
        VIMP,
        /** Infinitive verb. */
        VINF,
        /** Past participle. */
        VPP,
        /** Present participle. */
        VPR,
        /** Subjunctive verb. */
        VS;

    /**
     * The {@link Tag}.
     */
    private final Tag m_tag;

    /**
     * The tag type constant for this extended version of the French Treebank tag set and  the Crabbé & Candite tag set.
     */
    public static final String TAG_TYPE = "FTBCC+";

    /**
     * Map contains the {@link Tag Tags}.
     */
    private static final Map<String, Tag> TAG_MAP = Stream.of(values())
            .collect(Collectors.toMap(t -> t.getTag().getTagValue(), ExtendedFTBCrabbeCanditeTagSet::getTag));

    /**
     * Creates a new instance of {@code ExtendedFTBCrabbeCanditeTagSet} and creates a
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified POS tag.
     */
    private ExtendedFTBCrabbeCanditeTagSet() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * Returns the {@code Tag}.
     *
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding to the specified object from
     *         {@code ExtendedFTBCrabbeCanditeTagSet}.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the {@code UNKNOWN} tag is returned.
     *
     * @param str The string representing a {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the given string.
     */
    public static Tag stringToTag(final String str) {
        if (TAG_MAP.containsKey(str)) {
            return TAG_MAP.get(str);
        }
        if (isSymbol(str)) {
            return ExtendedFTBCrabbeCanditeTagSet.SYM.getTag();
        }
        return ExtendedFTBCrabbeCanditeTagSet.UNKNOWN.getTag();
    }

    private static Pattern symbolPattern = Pattern.compile("[!#$%&'\"()*+\\-,./\\:;<=>?@^_`{|}~\\[\\]]");

    private static boolean isSymbol(final String str) {
        return symbolPattern.matcher(str).matches();
    }
}

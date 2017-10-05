/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   21.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This enum contains the Penn Treebank part of speech tag set. To create a
 * valid {@link org.knime.ext.textprocessing.data.Tag} instance use
 * {@link org.knime.ext.textprocessing.data.PartOfSpeechTag#getTag()}, i.e: <br>
 * <br>
 * <code>Tag t = PartOfSpeechTag.NN.getTag();</code>
 *
 * @author Kilian Thiel, University of Konstanz
 */
public enum PartOfSpeechTag implements TagBuilder {

    /** Unkown type. */
    UNKNOWN,

    //
    // / all penn-treebank tags
    //
    /** Determiner. */
    DT,
    /** Coordinating conjunction. */
    CC,
    /** Foreign word. */
    FW,
    /** Preposition or subordinating conjunction. */
    IN,
    /** Adjective. */
    JJ,
    /** Modal. */
    MD,
    /** Noun, singular or mass. */
    NN,
    /** Noun, plural. */
    NNS,
    /** Proper noun, singular. */
    NNP,
    /** Proper noun, plural. */
    NNPS,
    /** Predeterminer. */
    PDT,
    /** Personal pronoun. */
    PRP,
    /** Possessive pronoun. */
    PRP$,
    /** Adverb. */
    RB,
    /** Particle. */
    RP,
    /** to. */
    TO,
    /** Symbol. */
    SYM,
    /** Interjection. */
    UH,
    /** Verb, base form. */
    VB,
    /** Verb, past tense. */
    VBD,
    /** Verb, gerund or present participle. */
    VBG,
    /** Verb, past participle. */
    VBN,
    /** Verb, non-3rd person singular present. */
    VBP,
    /** Verb, 3rd person singular present. */
    VBZ,
    /** Cardinal Number. **/
    CD,
    /** Existential there. **/
    EX,
    /** Adjective, comparative. **/
    JJR,
    /** Adjective, superlative. **/
    JJS,
    /** List Item Marker. **/
    LS,
    /** Possessive Ending. **/
    POS,
    /** Adverb, comparative. **/
    RBR,
    /**
     * with the comparative ending -er, with a strictly comparative meaning.
     **/
    Adverbs,
    /** Adverb, superlative. **/
    RBS,
    /** Wh-determiner. **/
    WDT,
    /** Wh-pronoun. **/
    WP,
    /** Possessive wh-pronoun. **/
    WP$,
    /** Wh-adverb. **/
    WRB;

    private final Tag m_tag;

    /**
     * The constant for POS tag types.
     */
    public static final String TAG_TYPE = "POS";

    /**
     * Creates new instance of <code>PartOfSpeechTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified Penn
     * Treebank POS tag.
     */
    private PartOfSpeechTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified <code>PartOfSpeechTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the enum fields as a String list of their names.
     *
     * @return - the enum fields as a String list of their names.
     */
    @Override
    public List<String> asStringList() {
        Enum<PartOfSpeechTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the
     * given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * <code>UNKNOWN</code> tag is returned.
     *
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the
     *         given string.
     */
    public static Tag stringToTag(final String str) {
        if (isSymbol(str)) {
            return PartOfSpeechTag.SYM.getTag();
        }

        for (PartOfSpeechTag pos : values()) {
            if (pos.getTag().getTagValue().equals(str)) {
                return pos.getTag();
            }
        }
        return PartOfSpeechTag.UNKNOWN.getTag();
    }

    private static Pattern symbolPattern = Pattern
            .compile("[!#$%&'\"()*+,./\\:;<=>?@^_`{|}~\\[\\]]");

    private static boolean isSymbol(final String str) {
        if (symbolPattern.matcher(str).matches()) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag buildTag(final String value) {
        return PartOfSpeechTag.stringToTag(value);
    }

    /**
     * @return The default "UNKNOWN" <code>PartOfSpeechTag</code> as
     *         <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return PartOfSpeechTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TAG_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new LinkedHashSet<Tag>(values().length);
        for (PartOfSpeechTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }
}

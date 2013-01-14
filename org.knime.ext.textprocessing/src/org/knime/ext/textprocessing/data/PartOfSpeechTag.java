/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   21.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.HashSet;
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
        Set<Tag> tagSet = new HashSet<Tag>(values().length);
        for (PartOfSpeechTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }
}

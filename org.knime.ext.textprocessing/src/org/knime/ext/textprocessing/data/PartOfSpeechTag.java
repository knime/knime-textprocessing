/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   21.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This enum contains the Penn Treebank part of speech tag set. To create a
 * valid {@link org.knime.ext.textprocessing.data.Tag} instance use
 * {@link org.knime.ext.textprocessing.data.PartOfSpeechTag#getTag()}, i.e:
 * <br><br>
 * <code>Tag t = PartOfSpeechTag.NN.getTag();</code>
 *
 * @author Kilian Thiel, University of Konstanz
 */
public enum PartOfSpeechTag implements TagBuilder {

    /** Unkown type. */
    UNKNOWN,

    //
    /// all penn-treebank tags
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
    /** with the comparative ending -er,
     * with a strictly comparative meaning. **/
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
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified
     * Penn Treebank POS tag.
     */
    private PartOfSpeechTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     * to the specified <code>PartOfSpeechTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the enum fields as a String list of their names.
     *
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<PartOfSpeechTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to
     * the given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * <code>UNKNOWN</code> tag is returned.
     * @param str The string representing a
     * {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to
     * the given string.
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

    private static Pattern m_symbolPattern = Pattern.compile(
            "[!#$%&'\"()*+,./\\:;<=>?@^_`{|}~\\[\\]]");

    private static boolean isSymbol(final String str) {
        if (m_symbolPattern.matcher(str).matches()) {
            return true;
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    public Tag buildTag(final String type, final String value) {
        if (type.equals(TAG_TYPE)) {
            return PartOfSpeechTag.stringToTag(value);
        }
        return null;
    }

    /**
     * @return The default "UNKNOWN" <code>PartOfSpeechTag</code> as
     * <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return PartOfSpeechTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TAG_TYPE;
    }
}

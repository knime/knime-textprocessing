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
 * This enum contains the French Treebank part of speech tag set. To create a
 * valid {@link org.knime.ext.textprocessing.data.Tag} instance use
 * {@link org.knime.ext.textprocessing.data.FrenchTreebankTag#getTag()}, i.e:
 * <br><br>
 * <code>Tag t = FrenchTreebankTag.A.getTag();</code>
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public enum FrenchTreebankTag implements TagBuilder {

    /** Unkown type. */
    UNKNOWN,

    /** adjective. */
    A,
    /** adverb. */
    ADV,
    /** coordinating conjunction. */
    CC,
    /** weak clitic pronoun. */
    CL,
    /** subordinating conjunction. */
    CS,
    /** conjunctions. */
    C,
    /** determiner. */
    D,
    /** foreign word. */
    ET,
    /** interjection. */
    I,
    /** common noun. */
    NC,
    /** proper noun. */
    NP,
    /** proper noun. */
    N,
    /** preposition. */
    P,
    /** prefix. */
    PREF,
    /** strong pronoun. */
    PRO,
    /** verb. */
    V,
    /** punctuation mark. */
    PONCT,
    /** Symbols. */
    SYM;



    private final Tag m_tag;

    /**
     * The constant for French Treebank tag types.
     */
    public static final String TAG_TYPE = "FTB";

    /**
     * Creates new instance of <code>PartOfSpeechTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified Penn
     * Treebank POS tag.
     */
    private FrenchTreebankTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified <code>FrenchTreebankTag</code>.
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
        Enum<FrenchTreebankTag>[] values = values();
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
        for (FrenchTreebankTag pos : values()) {
            if (pos.getTag().getTagValue().equals(str)) {
                return pos.getTag();
            }
        }

        if (isSymbol(str)) {
            return FrenchTreebankTag.SYM.getTag();
        }

        return FrenchTreebankTag.UNKNOWN.getTag();
    }

    private static Pattern symbolPattern = Pattern
            .compile("[!#$%&'\"()*+\\-,./\\:;<=>?@^_`{|}~\\[\\]]");

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
        return FrenchTreebankTag.stringToTag(value);
    }

    /**
     * @return The default "UNKNOWN" <code>FrenchTreebankTag</code> as
     *         <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return FrenchTreebankTag.UNKNOWN;
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
        for (FrenchTreebankTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }
}

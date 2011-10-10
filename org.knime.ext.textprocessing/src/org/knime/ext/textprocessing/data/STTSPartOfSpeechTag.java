/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * {@link org.knime.ext.textprocessing.data.STTSPartOfSpeechTag#getTag()}, i.e:
 * <br><br>
 * <code>Tag t = PartOfSpeechTag.NN.getTag();</code>
 *
 * @author Kilian Thiel, University of Konstanz
 */
public enum STTSPartOfSpeechTag implements TagBuilder {

    /** Unkown type. */
    UNKNOWN,

    //
    /// all STTS tags
    //
    /** attributives Adjektiv. */
    ADJA,
    /** adverbiales oder prädikatives Adjektiv. */
    ADJD,
    /** Adverb. */
    ADV,
    /** Präposition; Zirkumposition links. */
    APPR,
    /** Präposition mit Artikel. */
    APPRART,
    /** Postposition. */
    APPO,
    /** Zirkumposition rechts. */
    APZR,
    /** bestimmter oder unbestimmter Artikel. */
    ART,
    /** Kardinalzahl (Ordinalzahlen sind als ADJA getaggt). */
    CARD,
    /** Fremdsprachliches Material. */
    FM,
    /** Interjektion. */
    ITJ,
    /** unterordnende Konjunktion mit ``zu'' und Infinitiv. */
    KOUI,     
    /** unterordnende Konjunktion mit Satz. */
    KOUS , 
    /** nebenordnende Konjunktion. */
    KON,
    /** Vergleichskonjunktion. */
    KOKOM,
    /** normales Nomen. */
    NN,
    /** Eigennamen. */
    NE,
    /** substituierendes Demonstrativpronomen. */
    PDS,
    /** attribuierendes Demonstrativpronomen. */
    PDAT,
    /** substituierendes Indefinitpronomen. */
    PIS,    
    /** attribuierendes Indefinitpronomen ohne Determiner. */
    PIAT, 
    /** attribuierendes Indefinitpronomen mit Determiner. */
    PIDAT,    
    /** irreflexives Personalpronomen. */
    PPER,
    /** substituierendes Possessivpronomen. */
    PPOSS,
    /** attribuierendes Possessivpronomen. */
    PPOSAT,
    /** substituierendes Relativpronomen. */
    PRELS,
    /** attribuierendes Relativpronomen. */
    PRELAT,
    /** reflexives Personalpronomen. */
    PRF,
    /** substituierendes Interrogativpronomen. */
    PWS,
    /** attribuierendes Interrogativpronomen. */
    PWAT,
    /** adverbiales Interrogativ- oder Relativpronomen. */
    PWAV,
    /** Pronominaladverb. */
    PAV,
    /** ``zu'' vor Infinitiv. */
    PTKZU,
    /** Negationspartikel. */
    PTKNEG,
    /** abgetrennter Verbzusatz. */
    PTKVZ,
    /** Antwortpartikel. */
    PTKANT,
    /** Partikel bei Adjektiv oder Adverb. */
    PTKA,
    /** Kompositions-Erstglied. */
    TRUNC,
    /** finites Verb, voll. */
    VVFIN,
    /** Imperativ, voll. */
    VVIMP,
    /** Infinitiv, voll. */
    VVINF,
    /** Infinitiv mit ``zu'', voll. */
    VVIZU,
    /** Partizip Perfekt, voll. */
    VVPP,
    /** finites Verb, aux. */
    VAFIN,
    /** Imperativ, aux. */
    VAIMP,
    /** Infinitiv, aux. */
    VAINF,
    /** Partizip Perfekt, aux. */
    VAPP,
    /** finites Verb, modal. */
    VMFIN,
    /** Infinitiv, modal. */
    VMINF,
    /** Partizip Perfekt, modal. */
    VMPP,
    /** Nichtwort, Sonderzeichen enthaltend. */
    XY,
    /** Komma. */
    SYM;

    private final Tag m_tag;

    /**
     * The constant for POS tag types.
     */
    public static final String TAG_TYPE = "STTS";

    /**
     * Creates new instance of <code>PartOfSpeechTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified
     * Penn Treebank POS tag.
     */
    private STTSPartOfSpeechTag() {
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
    public List<String> asStringList() {
        Enum<STTSPartOfSpeechTag>[] values = values();
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
            return STTSPartOfSpeechTag.SYM.getTag();
        }

        for (STTSPartOfSpeechTag pos : values()) {
            if (pos.getTag().getTagValue().equals(str)) {
                return pos.getTag();
            }
        }
        return STTSPartOfSpeechTag.UNKNOWN.getTag();
    }

    private static Pattern symbolPattern = Pattern.compile("[\\$\\.,\\(]+");

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
        return STTSPartOfSpeechTag.stringToTag(value);
    }

    /**
     * @return The default "UNKNOWN" <code>STTSPartOfSpeechTag</code> as
     * <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return STTSPartOfSpeechTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TAG_TYPE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new HashSet<Tag>(values().length);
        for (STTSPartOfSpeechTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }     
}

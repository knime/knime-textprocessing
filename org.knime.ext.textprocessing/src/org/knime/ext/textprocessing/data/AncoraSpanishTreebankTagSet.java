/*
 * ------------------------------------------------------------------------
 *
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
 *   29.05.2017 (Julian): created
 */
package org.knime.ext.textprocessing.data;

/**
 * This enum contains the AnCora Spanish Treebank tag set. To create a
 * valid {@link org.knime.ext.textprocessing.data.Tag} instance use
 * {@link org.knime.ext.textprocessing.data.AncoraSpanishTreebankTagSet#getTag()}, i.e:
 * <br><br>
 * {@code Tag t = SimplifiedSpanishTreebankTagSet.AO.getTag();}
 * <br>
 * The tagset is based on the simplified AnCora tag set provided by StanfordNLP.
 * See here for more information: https://nlp.stanford.edu/software/spanish-faq.shtml#tagset
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.4
 */
public enum AncoraSpanishTreebankTagSet {
    /** Unknown type. */
    UNKNOWN,

    /** Adjective (ordinal) */
    AO,
    /** Adjective (descriptive) */
    AQ,
    /** Conjunction (coordinating) */
    CC,
    /** Conjunction (subordinating) */
    CS,
    /** Article (definite) */
    DA,
    /** Demonstrative */
    DD,
    /** Exclamative */
    DE,
    /** Article (indefinite) */
    DI,
    /** Numeral */
    DN,
    /** Possessive */
    DP,
    /** Interrogative */
    DT,
    /** Other punctuation  */
    F,
    /** Inverted exclamation mark */
    FAA,
    /** Exclamation mark */
    FAT,
    /** Comma */
    FC,
    /** Colon */
    FD,
    /** Double quote */
    FE,
    /** Hyphen */
    FG,
    /** Forward slash */
    FH,
    /** Inverted question mark */
    FIA,
    /** Question mark */
    FIT,
    /** Period / full-stop */
    FP,
    /** Left parenthesis */
    FPA,
    /** Right parenthesis */
    FPT,
    /** Ellipsis */
    FS,
    /** Percent sign */
    FT,
    /** Semicolon */
    FX,
    /** Single quote */
    FZ,
    /** Interjection */
    I,
    /** Unknown common noun (neologism, loanword) */
    NC,
    /** Common noun (invariant number) */
    NCN,
    /** Common noun (plural) */
    NCP,
    /** Common noun (singular) */
    NCS,
    /** Proper noun */
    NP,
    /** Impersonal se */
    P,
    /** Demonstrative pronoun */
    PD,
    /** "Exclamative" pronoun */
    PE,
    /** Indefinite pronoun */
    PI,
    /** Numeral pronoun */
    PN,
    /** Personal pronoun */
    PP,
    /** Relative pronoun */
    PR,
    /** Interrogative pronoun */
    PT,
    /** Possessive pronoun */
    PX,
    /** Adverb (general) */
    RG,
    /** Adverb (negating) */
    RN,
    /** Preposition */
    SP,
    /** Verb (auxiliary, gerund) */
    VAG,
    /** Verb (auxiliary, indicative, conditional) */
    VAI,
    /** Verb (auxiliary, indicative, future) */
    VAIF,
    /** Verb (auxiliary, indicative, imperfect) */
    VAII,
    /** Verb (auxiliary, indicative, present) */
    VAIP,
    /** Verb (auxiliary, indicative, preterite) */
    VAIS,
    /** Verb (auxiliary, imperative) */
    VAM,
    /** Verb (auxiliary, infinitive) */
    VAN,
    /** Verb (auxiliary, participle) */
    VAP,
    /** Verb (auxiliary, subjunctive, imperfect) */
    VASI,
    /** Verb (auxiliary, subjunctive, present) */
    VASP,
    /** Verb (main, gerund) */
    VMG,
    /** Verb (main, gerund) */
    VMIC,
    /** Verb (main, indicative, future) */
    VMIF,
    /** Verb (main, indicative, imperfect) */
    VMII,
    /** Verb (main, indicative, present) */
    VMIP,
    /** Verb (main, indicative, preterite) */
    VMIS,
    /** Verb (main, imperative) */
    VMM,
    /** Verb (main, infinitive) */
    VMN,
    /** Verb (main, participle) */
    VMP,
    /** Verb (main, subjunctive, imperfect) */
    VMSI,
    /** Verb (main, subjunctive, present) */
    VMSP,
    /** Verb (semiauxiliary, gerund) */
    VSG,
    /** Verb (semiauxiliary, indicative, conditional) */
    VSIC,
    /** Verb (semiauxiliary, indicative, future) */
    VSIF,
    /** Verb (semiauxiliary, indicative, imperfect) */
    VSII,
    /** Verb (semiauxiliary, indicative, present) */
    VSIP,
    /** Verb (semiauxiliary, indicative, preterite) */
    VSIS,
    /** Verb (semiauxiliary, imperative) */
    VSM,
    /** Verb (semiauxiliary, infinitive) */
    VSN,
    /** Verb (semiauxiliary, participle) */
    VSP,
    /** Verb (semiauxiliary, subjunctive, future) */
    VSS,
    /** Verb (semiauxiliary, subjunctive, imperfect) */
    VSSI,
    /** Verb (semiauxiliary, subjunctive, present) */
    VSSP,
    /** Date */
    W,
    /** Numeral */
    Z,
    /** Numeral qualifier (currency)     */
    ZM,
    /** Numeral qualifier (other units) */
    ZU,
    /** Emoticon or other symbol */
    WORD;

    private final Tag m_tag;

    /**
     * The constant for Spanish Ancora Treebank tag types.
     */
    public static final String TAG_TYPE = "ANCORA";

    /**
     * Creates new instance of {@code SimplifiedSpanishTreebankTag} and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified Penn
     * Treebank POS tag.
     */
    private AncoraSpanishTreebankTagSet() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the
     * given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * {@code UNKNOWN} tag is returned.
     *
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the
     *         given string.
     */
    public static Tag stringToTag(final String str) {
        String fiteredStr = str.replace("0", "");
        for (AncoraSpanishTreebankTagSet pos : AncoraSpanishTreebankTagSet.values()) {
            if (pos.getTag().getTagValue().toLowerCase().equals(fiteredStr)) {
                return pos.getTag();
            }
        }

        return UNKNOWN.getTag();
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified <code>FrenchTreebankTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }
}

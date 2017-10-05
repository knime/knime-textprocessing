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
 *   07.07.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnelearner;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class StanfordNlpNeLearnerConfigKeys {

    private StanfordNlpNeLearnerConfigKeys() {
        /* empty constructor */
    }

    static final String CFG_KEY_DOCUMENT_COL = "Document column";

    static final String CFG_KEY_STRING_COL = "String column";

    static final String CFGKEY_TAG_TYPE = "Tag type";

    static final String CFGKEY_TAG_VALUE = "Tag value";

    static final String CFGKEY_USE_CLASS_FEATURE = "Use Class Feature";

    static final String CFGKEY_USE_WORD = "Use word";

    static final String CFGKEY_USE_NGRAMS = "Use N-Grams";

    static final String CFGKEY_NO_MID_NGRAMS = "No mid N-Grams";

    static final String CFGKEY_MAX_NGRAM_LENG = "Max N-Gram length";

    static final String CFGKEY_USE_PREV = "Use previous";

    static final String CFGKEY_USE_NEXT = "Use next";

    static final String CFGKEY_USE_SEQUENCES = "Use sequences";

    static final String CFGKEY_USE_PREV_SEQUENCES = "Use previos sequences";

    static final String CFGKEY_MAX_LEFT = "Max left";

    static final String CFGKEY_USE_TYPE_SEQS = "Use type sequences";

    static final String CFGKEY_USE_TYPE_SEQS2 = "Use type sequences 2";

    static final String CFGKEY_USE_TYPE_Y_SEQS = "Use type y sequences";

    static final String CFGKEY_WORDSHAPE = "Word shape";

    static final String CFGKEY_USE_DISJUNCTIVE = "Use disjunctive";

    static final String CFGKEY_TOKENIZER = "Word Tokenizer";

}

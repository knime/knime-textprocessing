/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   06.10.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stanfordlemmatizer;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

import edu.stanford.nlp.process.Morphology;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class StanfordLemmatizer implements TermPreprocessing {

    /** Constant for the boolean flag to determine whether the node should fail. */
    public static final boolean DEF_FAIL = false;

    private boolean m_skipTerms;

    private WarningMessage m_warnMessage;

    /**
     * Creates new instance of StanfordLemmatizer.
     *
     * @param msg a WarningMessage object to store any warning message that may appear after processing
     * @param skip boolean whether terms with no POS tags should be skipped or not
     */
    public StanfordLemmatizer(final WarningMessage msg, final boolean skip) {
        m_skipTerms = skip;
        m_warnMessage = msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {

        Morphology morph = new Morphology();
        final List<Tag> tags = term.getTags();
        String tag = "";
        // if term doesn't have any tags
        if (tags.isEmpty()) {
            // either skip or throw an exception
            if (!m_skipTerms) {
                m_warnMessage.set("Warning: Some terms have no POS tags.");
                return term;
            } else {
                throw new RuntimeException("Some terms have no POS tags.");
            }

        }
        // take the first POS tag found that is not UNKNOWN
        for (Tag elem : tags) {
            if (elem.getTagType().equals("POS") && !elem.getTagValue().equals("UNKNOWN")) {
                tag = elem.getTagValue();
                break;
            }
        }
        // also skip if no POS tag is found
        if (tag.isEmpty()) {
            return term;
        }

        final List<Word> words = term.getWords();
        final List<Word> newWords = new ArrayList<Word>();
        for (final Word w : words) {
            newWords.add(new Word(morph.lemma(w.getWord(), tag), w.getWhitespaceSuffix()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * @return the WarningMessage object that contains the warning message
     */
    public WarningMessage getWarnMessage() {
        return m_warnMessage;
    }
}

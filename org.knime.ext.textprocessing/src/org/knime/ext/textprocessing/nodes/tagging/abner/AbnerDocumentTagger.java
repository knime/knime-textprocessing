/*
 * ------------------------------------------------------------------------
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
 *   27.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.BiomedicalNeTag;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

import abner.Tagger;

/**
 * <code>AbnerDocumentTagger</code> is a concrete implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.DocumentTagger} and extends
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger}. The method
 * {@link AbnerDocumentTagger#tagEntities(Sentence)} recognizes biomedical named entities by using ABNER (A Biomedical
 * Named Entity Recognizer). For more details about ABNER see (http://pages.cs.wisc.edu/~bsettles/abner/). The method
 * {@link AbnerDocumentTagger#getTags(String)} returns tag of the type
 * {@link org.knime.ext.textprocessing.data.BiomedicalNeTag}.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerDocumentTagger extends AbstractDocumentTagger {
    /**
     * Name of the Biocreative model of ABNER.
     */
    public static final String MODEL_BIOCREATIVE = "Biocreative";

    /**
     * Name of the NLPBA model of ABNER.
     */
    public static final String MODEL_NLPBA = "NLPBA";

    private Tagger m_tagger;

    /**
     * Creates a new instance of <code>AbnerDocumentTagger</code> with given flag specifying if recognized named
     * entities is set unmodifiable. The specified ABNER model is used for named entity recognition, if no model or a
     * non valid model is specified the "Biocreative" model is used by default.
     *
     * @param setNeUnmodifiable The unmodifiable flag to set.
     * @param model The ABNER model to use for tagging.
     * @deprecated Use {@link #AbnerDocumentTagger(boolean, String, String)} instead to define the tokenizer used for
     *             word tokenization.
     */
    @Deprecated
    public AbnerDocumentTagger(final boolean setNeUnmodifiable, final String model) {
        super(setNeUnmodifiable);

        if (model.equals(MODEL_NLPBA)) {
            m_tagger = new Tagger(Tagger.NLPBA);
        } else {
            m_tagger = new Tagger(Tagger.BIOCREATIVE);
        }
    }

    /**
     * Creates a new instance of <code>AbnerDocumentTagger</code> with given flag specifying if recognized named
     * entities is set unmodifiable. The specified ABNER model is used for named entity recognition, if no model or a
     * non valid model is specified the "Biocreative" model is used by default.
     *
     * @param setNeUnmodifiable The unmodifiable flag to set.
     * @param model The ABNER model to use for tagging.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public AbnerDocumentTagger(final boolean setNeUnmodifiable, final String model, final String tokenizerName) {
        super(setNeUnmodifiable, tokenizerName);

        if (model.equals(MODEL_NLPBA)) {
            m_tagger = new Tagger(Tagger.NLPBA);
        } else {
            m_tagger = new Tagger(Tagger.BIOCREATIVE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(BiomedicalNeTag.stringToTag(tag));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        String[][] nes;

        synchronized (Tagger.class) {
            nes = m_tagger.getEntities(sentence.getText());
        }

        List<TaggedEntity> entities = new ArrayList<TaggedEntity>();
        for (int i = 0; i < nes[0].length; i++) {
            entities.add(new TaggedEntity(nes[0][i], nes[1][i]));
        }
        return entities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }
}

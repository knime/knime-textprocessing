/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   27.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import abner.Tagger;
import java.util.ArrayList;
import java.util.List;
import org.knime.ext.textprocessing.data.BiomedicalNeTag;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

/**
 * <code>AbnerDocumentTagger</code> is a concrete implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.DocumentTagger} and
 * extends
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger}.
 * The method {@link AbnerDocumentTagger#tagEntities(Sentence)} recognizes
 * biomedical named entities by using ABNER (A Biomedical Named Entity
 * Recognizer). For more details about ABNER see
 * (http://pages.cs.wisc.edu/~bsettles/abner/).
 * The method {@link AbnerDocumentTagger#getTags(String)} returns tag of the
 * type {@link org.knime.ext.textprocessing.data.BiomedicalNeTag}.
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
     * Creates a new instance of <code>AbnerDocumentTagger</code> with given
     * flag specifying if recognized named entities is set unmodifiable.
     * The specified ABNER model is used for named entity recognition, if
     * no model or a non valid model is specified the "Biocreative" model is
     * used by default.
     *
     * @param setNeUnmodifiable The unmodifiable flag to set.
     * @param model The ABNER model to use for tagging.
     */
    public AbnerDocumentTagger(final boolean setNeUnmodifiable,
            final String model) {
        super(setNeUnmodifiable);

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
        String[][] nes = m_tagger.getEntities(sentence.getText());
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

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   27.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import org.knime.ext.textprocessing.data.BiomedicalNeTag;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

import java.util.ArrayList;
import java.util.List;

import abner.Tagger;

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
        } else if (model.equals(MODEL_BIOCREATIVE)) {
            m_tagger = new Tagger(Tagger.BIOCREATIVE);
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

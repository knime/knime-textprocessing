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
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

/**
 * The POS tagger node adds part of speech (POS) tags to terms of documents. Here the Penn Treebank part-of-speech tag
 * set is used to define all kinds of tags, see {@link org.knime.ext.textprocessing.data.PartOfSpeechTag} for more
 * details. The underlying tagger model which is used to choose the (hopefully) proper tags for all the terms is an
 * external model of the OpenNLP framework, see (http://opennlp.sourceforge.net) for more details.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class PosDocumentTagger extends AbstractDocumentTagger {

    private POSTagger m_tagger;

    /**
     * Creates a new instance of PosDocumentTagger and loads internally the POS tagging model of the OpenNLP framework
     * to POS tag the documents. If the model file could not be loaded an <code>IOException</code> will be thrown. If
     * <code>setNeUnmodifiable</code> is set <code>true</code> all recognized terms are set to unmodifiable.
     *
     * @param setNeUnmodifiable If true all recognized terms are set unmodifiable.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @throws IOException If the model file could not be loaded.
     * @since 3.3
     */
    public PosDocumentTagger(final boolean setNeUnmodifiable, final String tokenizerName) throws IOException {
        super(setNeUnmodifiable, tokenizerName);
        String modelPath = OpenNlpModelPaths.getOpenNlpModelPaths().getPosTaggerModelFile();
        InputStream is = new FileInputStream(new File(modelPath));
        POSModel model = new POSModel(is);
        m_tagger = new POSTaggerME(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(PartOfSpeechTag.stringToTag(tag));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        SortedSet<String> words = new TreeSet<String>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                words.add(w.getWord());
            }
        }
        String[] wordsArr = words.toArray(new String[0]);
        String[] tagsArr = m_tagger.tag(wordsArr);

        List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>(tagsArr.length);
        for (int i = 0; i < wordsArr.length; i++) {
            taggedEntities.add(new TaggedEntity(wordsArr[i], tagsArr[i]));
        }

        return taggedEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }
}

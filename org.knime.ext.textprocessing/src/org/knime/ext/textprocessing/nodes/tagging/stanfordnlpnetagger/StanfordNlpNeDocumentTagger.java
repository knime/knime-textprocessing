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
 *   30.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
import org.knime.ext.textprocessing.util.StanfordNeModelPaths;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeDocumentTagger extends AbstractDocumentTagger {

    /**
     * Contains the paths to the models.
     */
    private static final StanfordNeModelPaths PATHS = StanfordNeModelPaths.getStanfordNeModelPaths();

    /**
     * The tagger model names and their corresponding models.
     */
    static final HashMap<String, String> TAGGERMODELS = new HashMap<>();

    private AbstractSequenceClassifier<CoreLabel> m_tagger;

    private Tag m_tag;

    static {
        TAGGERMODELS.put("English 3 classes distsim", PATHS.getEnglishAll3ClassDistSimModelFile());
        TAGGERMODELS.put("English 4 classes distsim", PATHS.getEnglishConll4ClassDistSimModelFile());
        TAGGERMODELS.put("English 7 classes distsim", PATHS.getEnglishMuc7ClassDistSimModelFile());
        TAGGERMODELS.put("English 3 classes no distsim", PATHS.getEnglishAll3ClassNoDistSimModelFile());
        TAGGERMODELS.put("English 4 classes no distsim", PATHS.getEnglishConll4ClassNoDistSimModelFile());
        TAGGERMODELS.put("English 7 classes no distsim", PATHS.getEnglishMuc7ClassNoDistSimModelFile());
        TAGGERMODELS.put("English 3 classes no wiki caseless distsim",
            PATHS.getEnglishNoWiki3ClassCaselessDistSimModelFile());
        TAGGERMODELS.put("German dewac", PATHS.getGermanDewacModelFile());
        TAGGERMODELS.put("German hgc", PATHS.getGermanHgcModelFile());
        TAGGERMODELS.put("Spanish ancora distsim", PATHS.getSpanishAncoraDistSimModelFile());
    }

    /**
     * Creates a new instance of {@code StanfordNlpNeDocumentTagger}.
     *
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param model The model.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @throws ClassNotFoundException If the classifier could not be created from given path.
     * @throws IOException If the classifier could not be created from given path.
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final String model, final String tokenizerName)
        throws ClassNotFoundException, IOException {
        super(setNeUnmodifiable, tokenizerName);
        if (!TAGGERMODELS.containsKey(model)) {
            throw new IllegalArgumentException("Model \"" + model + "\" does not exist.");
        }
        String path = TAGGERMODELS.get(model);
        m_tagger = CRFClassifier.getClassifier(path);
    }

    /**
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param crf The CRFClassifer.
     * @param tag The used tag.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final CRFClassifier<CoreLabel> crf,
        final Tag tag, final String tokenizerName) {
        super(setNeUnmodifiable, tokenizerName);
        m_tagger = crf;
        m_tag = tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        if (m_tag == null) {
            tags.add(NamedEntityTag.stringToTag(tag));
        } else if (m_tag != null) {
            tags.add(m_tag);
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<HasWord> wordList = new ArrayList<>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                wordList.add(new edu.stanford.nlp.ling.Word(w.getText()));
            }
        }
        List<CoreLabel> coreLabelList = edu.stanford.nlp.ling.Sentence.toCoreLabelList(wordList);
        List<CoreLabel> taggedWords = m_tagger.classifySentence(coreLabelList);

        List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>();
        for (CoreLabel tw : taggedWords) {
            String answer = tw.getString(CoreAnnotations.AnswerAnnotation.class);
            if (!answer.equals("O")) {
                taggedEntities.add(new TaggedEntity(tw.getString(CoreAnnotations.ValueAnnotation.class),
                    tw.getString(CoreAnnotations.AnswerAnnotation.class)));
            }
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

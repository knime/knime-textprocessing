/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *   14.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.TextprocessingCorePlugin;



/**
 * Provides the paths to the models used by the OpenNlp library. The paths
 * are based on the root directory of the Textprocessing plugin, which is
 * held in the plugin's activator class
 * {@link org.knime.ext.textprocessing.TextprocessingCorePlugin}. Without the
 * activation of the plugin (which is usually done automatically by eclipse
 * by the creation of an instance of the activator class) the plugin root path
 * and the model paths cannot be created / provided.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class OpenNlpModelPaths {
    private static final OpenNlpModelPaths INSTANCE = new OpenNlpModelPaths();

    private static final String SENTENCE_MODEL_POSTFIX = "opennlpmodels/sentdetect/en-sent.bin";

    private static final String TOKENIZATION_EN_MODEL_POSTFIX = "opennlpmodels/tokenize/en-token.bin";

    private static final String TOKENIZATION_DE_MODEL_POSTFIX = "opennlpmodels/tokenize/de-token.bin";

    private static final String POS_MODEL_POSTFIX = "opennlpmodels/pos/en-pos-maxent.bin";

    /**
     * @deprecated tag dictionary is not used in opennlp > 1.3.0 anymore
     */
    @Deprecated
    private static final String POS_DICT_POSTFIX = "opennlpmodels/pos/tagdict";

    private static final String NER_LOCATION_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-location.bin";

    private static final String NER_PERSON_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-person.bin";

    private static final String NER_ORGANIZATION_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-organization.bin";

    private static final String NER_MONEY_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-money.bin";

    private static final String NER_DATE_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-date.bin";

    private static final String NER_TIME_MODEL_POSTFIX = "opennlpmodels/namefind/en-ner-time.bin";

    /**
     * @return The singleton <code>OpenNlpModelPaths</code> instance holding
     * the paths to the OpenNlp models.
     */
    public static OpenNlpModelPaths getOpenNlpModelPaths() {
        return INSTANCE;
    }

    private OpenNlpModelPaths() {

    }

    /**
     * @return the model file of the sentence detection model.
     */
    public String getSentenceModelFile() {
        return TextprocessingCorePlugin.resolvePath(SENTENCE_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the tokenization model.
     * @since 3.3
     */
    public String getEnTokenizerModelFile() {
        return TextprocessingCorePlugin.resolvePath(TOKENIZATION_EN_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @since 3.3
     */
    public String getDeTokenizerModelFile() {
        return TextprocessingCorePlugin.resolvePath(TOKENIZATION_DE_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the pos tagger model.
     */
    public String getPosTaggerModelFile() {
        return TextprocessingCorePlugin.resolvePath(POS_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the pos tagger tag dictionary.
     * @deprecated tag dictionary is not used in opennlp > 1.3.0 anymore
     */
    @Deprecated
    public String getPosTaggerDictFile() {
        return TextprocessingCorePlugin.resolvePath(POS_DICT_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the person recognizer.
     */
    public String getPersonNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_PERSON_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the location recognizer.
     */
    public String getLocationNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_LOCATION_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the organization recognizer.
     */
    public String getOrganizationNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_ORGANIZATION_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the money recognizer.
     */
    public String getMoneyNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_MONEY_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the date recognizer.
     */
    public String getDateNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_DATE_MODEL_POSTFIX).getAbsolutePath();
    }

    /**
     * @return the model file of the time recognizer.
     */
    public String getTimeNERModelFile() {
        return TextprocessingCorePlugin.resolvePath(NER_TIME_MODEL_POSTFIX).getAbsolutePath();
    }
}

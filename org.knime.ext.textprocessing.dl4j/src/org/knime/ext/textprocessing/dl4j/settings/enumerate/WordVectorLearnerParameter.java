/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.settings.enumerate;

/**
 * Parameters for training of word vectors.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public enum WordVectorLearnerParameter {
        /** Minimum frequency threshold, words which are less frequent will be discarded. */
        MIN_WORD_FREQUENCY,
        /** Number of epochs to train the model. */
        LAYER_SIZE,
        /** Size of the context window. */
        WINDOW_SIZE,
        /** What word vectors to train. */
        WORD_VECTOR_TRAINING_MODE,
        /** Learning rate decays in word vector learning, minimum learning rate to use. */
        MIN_LEARNING_RATE,
        /** Whether to enable basic preprocessing (convert to lower case and strip punctuation) of tokens. */
        USE_BASIC_PREPROCESSING,
        /** Whether to skip rows containing missing cells. */
        SKIP_MISSING_CELLS;

    // Parameter default values
    public static final int DEFAULT_INT = 1;

    public static final Double DEFAULT_MIN_LEARNING_RATE = 0.0001;

    public static final int DEFAULT_LAYER_SIZE = 100;

    public static final int DEFAULT_WINDOW_SIZE = 5;

    public static final int DEFAULT_MIN_WORD_FREQUENCY = 0;

    public static final String DEFAULT_WORD_VECTOR_TRAININGS_MODE = "WORD2VEC";

    public static final boolean DEFAULT_USE_BASIC_PREPROCESSING = true;
}

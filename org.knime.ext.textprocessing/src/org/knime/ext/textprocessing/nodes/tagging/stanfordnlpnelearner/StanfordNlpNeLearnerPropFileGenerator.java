/*
 * ------------------------------------------------------------------------
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

import java.io.FileNotFoundException;
import java.util.Properties;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class StanfordNlpNeLearnerPropFileGenerator {

    private Properties m_propFile;

    /**
     * @param trainFile
     * @throws FileNotFoundException
     */
    StanfordNlpNeLearnerPropFileGenerator(final String trainFile) throws FileNotFoundException {
        m_propFile = createPropFile(trainFile, true, true, true, true, 6, true, true, true, true, true, 1, true, true,
            true, "chris2useLC");
    }

    /**
     * @param trainFile
     * @param useClassFeature
     * @param useWord
     * @param useNGrams
     * @param noMidNGrams
     * @param maxNGramLeng
     * @param usePrev
     * @param useNext
     * @param useDisjunctive
     * @param useSequences
     * @param usePrevSequences
     * @param maxLeft
     * @param useTypeSeqs
     * @param useTypeSeqs2
     * @param useTypeySequences
     * @param wordShape
     * @throws FileNotFoundException
     */
    StanfordNlpNeLearnerPropFileGenerator(final String trainFile, final boolean useClassFeature, final boolean useWord,
        final boolean useNGrams, final boolean noMidNGrams, final int maxNGramLeng, final boolean usePrev,
        final boolean useNext, final boolean useDisjunctive, final boolean useSequences, final boolean usePrevSequences,
        final int maxLeft, final boolean useTypeSeqs, final boolean useTypeSeqs2, final boolean useTypeySequences,
        final String wordShape) throws FileNotFoundException {
        m_propFile = createPropFile(trainFile, useClassFeature, useWord, useNGrams, noMidNGrams, maxNGramLeng, usePrev,
            useNext, useDisjunctive, useSequences, usePrevSequences, maxLeft, useTypeSeqs, useTypeSeqs2,
            useTypeySequences, wordShape);
    }

    private Properties createPropFile(final String trainFile, final boolean useClassFeature, final boolean useWord,
        final boolean useNGrams, final boolean noMidNGrams, final int maxNGramLeng, final boolean usePrev,
        final boolean useNext, final boolean useDisjunctive, final boolean useSequences, final boolean usePrevSequences,
        final int maxLeft, final boolean useTypeSeqs, final boolean useTypeSeqs2, final boolean useTypeySequences,
        final String wordShape) throws FileNotFoundException {

        Properties props = new Properties();
        props.setProperty("trainFile", trainFile);
        props.setProperty("map", "word=0,answer=1");
        props.setProperty("useClassFeature", String.valueOf(useClassFeature));
        props.setProperty("useWord", String.valueOf(useWord));
        props.setProperty("useNGrams", String.valueOf(useNGrams));
        props.setProperty("noMidNGrams", String.valueOf(noMidNGrams));
        props.setProperty("maxNGramLeng", String.valueOf(maxNGramLeng));
        props.setProperty("usePrev", String.valueOf(usePrev));
        props.setProperty("useNext", String.valueOf(useNext));
        props.setProperty("useDisjunctive", String.valueOf(useDisjunctive));
        props.setProperty("useSequences", String.valueOf(useSequences));
        props.setProperty("usePrevSequences", String.valueOf(usePrevSequences));
        props.setProperty("maxLeft", String.valueOf(maxLeft));
        props.setProperty("useTypeSeqs", String.valueOf(useTypeSeqs));
        props.setProperty("useTypeSeqs2", String.valueOf(useTypeSeqs2));
        props.setProperty("useTypeySequences", String.valueOf(useTypeySequences));
        props.setProperty("wordShape", wordShape);

        return props;
    }

    /**
     * @return the path of the properties file
     */
    protected Properties getPropFile() {
        return m_propFile;
    }
}

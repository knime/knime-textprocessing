/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 28.01.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class NGramNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns the settings model, storing the N value for
     * ngram creation.
     * @return The settings model with the N value.
     */
    static final SettingsModelIntegerBounded getNModel() {
        return new SettingsModelIntegerBounded(NGramConfigKeys.N,
            NGramNodeModel.DEF_N, NGramNodeModel.MIN_N, NGramNodeModel.MAX_N);
    }

    /**
     * Creates and returns the settings model, storing the n gram type value for
     * n gram creation.
     * @return The settings model with the n gram type.
     */
    static final SettingsModelString getNGramTypeModel() {
        return new SettingsModelString(NGramConfigKeys.NGRAM_TYPE,
            NGramNodeModel.WORD_NGRAM_TYPE);
    }

    /**
     * Creates and returns the settings model, storing the output table type
     * of the n gram creation, which can be a bag of words or a frequency table.
     * @return The settings model with the n gram output table type.
     */
    static final SettingsModelString getNGramOutputTableModel() {
        return new SettingsModelString(NGramConfigKeys.NGRAM_OUTPUT,
            NGramNodeModel.FREQUENCY_NGRAM_OUTPUT);
    }

    /**
     * Creates and returns the settings model, storing the column of the input
     * table, containing the documents to process.
     * @return The settings model with input document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(NGramConfigKeys.DOCUMENT_INPUT_COL,
            BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }

    /**
     * Creates and returns the settings model, storing the number of maximal
     * parallel threads running.
     * @return The settings model with number of maximal parallel threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(
            NGramConfigKeys.NUMBER_OF_THREADS, NGramNodeModel.DEF_THREADS,
            NGramNodeModel.MIN_THREADS, NGramNodeModel.MAX_THREADS);
    }

    /**
     * Creates and returns the settings model, storing the chunk size per
     * thread.
     * @return The settings model with chunk size per thread.
     */
    static final SettingsModelIntegerBounded getChunkSizeModel() {
        return new SettingsModelIntegerBounded(
            NGramConfigKeys.CHUNK_SIZE, NGramNodeModel.DEF_CHUNK_SIZE,
            NGramNodeModel.MIN_CHUNK_SIZE, NGramNodeModel.MAX_CHUNK_SIZE);
    }


    /**
     * Creates new instance of <code>NGramNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public NGramNodeDialog() {
        createNewGroup("NGram settings");

        addDialogComponent(new DialogComponentNumber(getNModel(), "N", 1));

        addDialogComponent(new DialogComponentButtonGroup(getNGramTypeModel(),
            false, "NGram type", new String[]{NGramNodeModel.WORD_NGRAM_TYPE,
            NGramNodeModel.CHAR_NGRAM_TYPE}));

        closeCurrentGroup();

        createNewGroup("Input / Output settings");

        addDialogComponent(new DialogComponentColumnNameSelection(
            getDocumentColumnModel(), "Document column", 0,
            DocumentValue.class));

        addDialogComponent(new DialogComponentButtonGroup(
            getNGramOutputTableModel(), false, "Output table", new String[]{
                NGramNodeModel.FREQUENCY_NGRAM_OUTPUT,
                NGramNodeModel.BOW_NGRAM_OUTPUT}));

        closeCurrentGroup();

        createNewGroup("Processes");

        addDialogComponent(new DialogComponentNumber(getNumberOfThreadsModel(),
            "Number of maximal parallel processes", 1));

        addDialogComponent(new DialogComponentNumber(getChunkSizeModel(),
            "Number of documents per process", 100));

        closeCurrentGroup();
    }
}

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
 *   05.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikalangdetector;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaLangDetectorNodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * The name of the language column to parse.
     */
    static final String DEFAULT_COLNAME = "";

    /**
     * The name of the language column to append.
     */
    static final String DEFAULT_LANG_COLNAME = "Language";

    /**
     * Flag to specify whether to show the confidence value.
     */
    static final boolean DEFAULT_CONFIDENCE = false;

    /**
     * The name of the confidence value column to append.
     */
    static final String DEFAULT_CONFIDENCE_COLNAME = "Confidence Value";

    /**
     * Flag to specify whether to show all detected languages.
     */
    static final boolean DEFAULT_ALL_LANGS = false;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaLangDetectorNodeModel.class);

    private SettingsModelString m_colModel = TikaLangDetectorNodeDialog.createColModel();

    private SettingsModelString m_langColNameModel = TikaLangDetectorNodeDialog.createLangColNameModel();

    private SettingsModelBoolean m_confidenceBooleanModel = TikaLangDetectorNodeDialog.getConfidenceBooleanModel();

    private SettingsModelString m_ConfidenceColNameModel = TikaLangDetectorNodeDialog.createConfidenceColNameModel();

    private SettingsModelBoolean m_allLangsBooleanModel = TikaLangDetectorNodeDialog.getAllLangsBooleanModel();

    /**
     * Creates a new instance of {@code TikaLangDetectorNodeModel}
     */
    public TikaLangDetectorNodeModel() {

    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidSettingsException
     */
    @Override
    public ColumnRearranger createColumnRearranger(final DataTableSpec in) throws InvalidSettingsException {
        int colIndex = -1;
        final int langIndex = 0;
        int valueIndex = -1;
        int colSpecLength = 1;

        if(m_confidenceBooleanModel.getBooleanValue()){
            valueIndex = 1;
            colSpecLength++;
        }

        if(in.findColumnIndex(m_langColNameModel.getStringValue()) >= 0){
            throw new InvalidSettingsException("Language column name already exists.");
        }

        if(m_confidenceBooleanModel.getBooleanValue() && in.findColumnIndex(m_ConfidenceColNameModel.getStringValue()) >= 0){
            throw new InvalidSettingsException("Confidence value column name already exists.");
        }

        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(in);
        if (!verifier.verifyMinimumStringCells(1, false) && !verifier.verifyMinimumDocumentCells(1, false)) {
            throw new InvalidSettingsException("Input table contains no string/document columns!");
        }

        colIndex = in.findColumnIndex(m_colModel.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < in.getNumColumns(); i++) {
                if (in.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    LOGGER.info("Guessing string column \"" + in.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
        }

        if (colIndex < 0) {
            throw new InvalidSettingsException("String/Document column not set");
        }
        m_colModel.setStringValue(in.getColumnSpec(colIndex).getName());

        ColumnRearranger c = new ColumnRearranger(in);

        DataColumnSpec[] newColSpecs = new DataColumnSpec[colSpecLength];
        newColSpecs[langIndex] = this.createColumnSpec(m_allLangsBooleanModel.getBooleanValue(),
            StringCell.TYPE, m_langColNameModel, DEFAULT_LANG_COLNAME);
        if(m_confidenceBooleanModel.getBooleanValue()){
            newColSpecs[valueIndex] = this.createColumnSpec(m_allLangsBooleanModel.getBooleanValue(),
                DoubleCell.TYPE, m_ConfidenceColNameModel, DEFAULT_CONFIDENCE_COLNAME);
        }

        TikaLangDetectorCellFactory factory;
        try {
            factory = new TikaLangDetectorCellFactory(colIndex, newColSpecs, langIndex,
                valueIndex, m_allLangsBooleanModel.getBooleanValue());
            c.append(factory);
        } catch (IOException e) {
            LOGGER.error("Error while loading Tika language models");
        }
        return c;
    }

    private DataColumnSpec createColumnSpec(final boolean collection, final DataType type,
        final SettingsModelString model, final String colname){
        DataColumnSpec newColSpec;
        DataType cellType = type;
        if(collection){
            cellType = ListCell.getCollectionType(type);
        }
        if (model.getStringValue().isEmpty()) {
            newColSpec = new DataColumnSpecCreator(colname, cellType).createSpec();
        } else {
            newColSpec = new DataColumnSpecCreator(model.getStringValue(), cellType).createSpec();
        }
        return newColSpec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_langColNameModel.saveSettingsTo(settings);
        m_confidenceBooleanModel.saveSettingsTo(settings);
        m_ConfidenceColNameModel.saveSettingsTo(settings);
        m_allLangsBooleanModel.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_langColNameModel.validateSettings(settings);
        m_confidenceBooleanModel.validateSettings(settings);
        m_ConfidenceColNameModel.validateSettings(settings);
        m_allLangsBooleanModel.validateSettings(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.loadSettingsFrom(settings);
        m_langColNameModel.loadSettingsFrom(settings);
        m_confidenceBooleanModel.loadSettingsFrom(settings);
        m_ConfidenceColNameModel.loadSettingsFrom(settings);
        m_allLangsBooleanModel.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

}

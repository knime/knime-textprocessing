/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   15.11.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.knime.base.node.util.DefaultDataArray;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The NodeModel of the tag cloud node.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */

@Deprecated
public class TagCloudNodeModel extends NodeModel {

    /** stores the input data table. */
    private DataTable m_data;

    /** stores a copy of the  data, needed to show the tagcloud. */
    private TagCloud m_tagcloud;

   private SettingsModelString m_termColModel =
            TagCloudNodeDialog.getTermColumnModel();

    private SettingsModelString m_valueColModel =
            TagCloudNodeDialog.getValueModel();

    private SettingsModelString m_calcTCTypeModel =
            TagCloudNodeDialog.getTypeofTCcalculationModel();

    private SettingsModelBoolean m_ignoretags =
            TagCloudNodeDialog.getBooleanModel();

    private SettingsModelIntegerBounded m_noOfRows =
            TagCloudNodeDialog.getNoofRowsModel();
    private SettingsModelBoolean m_allRows =
            TagCloudNodeDialog.getUseallrowsBooleanModel();


    /** The selected ID of the Column containing the value. */
    private int m_valueColIndex;

    /** The selected ID of the Column containing the term . */
    private int m_termColIndex;

    /**
     * The name of the configuration file.
     */
    private static final String DATA_FILE_NAME = "tagcloudpoints.data";

    /**
     * The configuration key for the internal model of the Tagcloud.
     */
    public static final String INTERNAL_MODEL = "TagCloudNodel.data";

    /**
     * Initializes NodeModel.
     */
     TagCloudNodeModel() {
        super(1, 0);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        DataTableSpecVerifier verifier
                    = new DataTableSpecVerifier(inSpecs[0]);
        /** Verifies if there are at least one termcell and
         *  one numbercell and initializes the selected column indexes
         */
        verifier.verifyMinimumTermCells(1, true);
        verifier.verifyMinimumNumberCells(1, true);

        setColumnindexes(inSpecs[0]);
        return null;
    }

    /** Initializes the column index for the selected term and value
     * column.
     * @param spec the DataTableSpec of the input table
     */
    protected void setColumnindexes(final DataTableSpec spec) {
    DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        m_termColIndex = verifier.getTermCellIndex();
        m_valueColIndex = verifier.getNumberCellIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        int numofRows = inData[0].getRowCount();
        if (!m_allRows.getBooleanValue()) {
            numofRows = Math.min(m_noOfRows.getIntValue(), numofRows);

        }
        if (numofRows <= 0) {
            m_tagcloud = null;
            setWarningMessage("Empty data table, nothing to display");
            return null;
        }
        m_data = new DefaultDataArray(inData[0], 1, numofRows, exec);

        setColumnindexes(inData[0].getDataTableSpec());

        m_termColIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_termColModel.getStringValue());

        if (m_termColIndex < 0) {
            m_termColIndex = (new DataTableSpecVerifier(
                    inData[0].getSpec())).getTermCellIndex();
        }

        m_valueColIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_valueColModel.getStringValue());

        if (m_valueColIndex < 0) {
            m_valueColIndex = (new DataTableSpecVerifier(
                    inData[0].getSpec())).getNumberCellIndex();
        }

        m_tagcloud = new TagCloud();
        m_tagcloud.createTagCloud(exec, this);

        exec.setProgress(1, "TagCloud completed");
        return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.loadSettingsFrom(settings);
        m_calcTCTypeModel.loadSettingsFrom(settings);
        m_ignoretags.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
        m_allRows.loadSettingsFrom(settings);
        m_noOfRows.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        // Save tagcloud content
        ModelContent modelContent = new ModelContent(INTERNAL_MODEL);
        m_tagcloud.saveTo(modelContent);

        File file = new File(nodeInternDir, DATA_FILE_NAME);
        FileOutputStream fos = new FileOutputStream(file);
        modelContent.saveToXML(fos);
        fos.close();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        File file = new File(nodeInternDir, DATA_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ModelContentRO modelContent = ModelContent.loadFromXML(fis);

        try {
          m_tagcloud = new TagCloud();
          m_tagcloud.loadFrom(modelContent);

        } catch (InvalidSettingsException e1) {
            IOException ioe = new IOException("Could not load settings,"
                    + "due to invalid settings in model content !");
            ioe.initCause(e1);
            fis.close();
            throw ioe;
        }
        fis.close();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_valueColModel.saveSettingsTo(settings);
        m_calcTCTypeModel.saveSettingsTo(settings);
        m_ignoretags.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
        m_noOfRows.saveSettingsTo(settings);
        m_allRows.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.validateSettings(settings);
        m_calcTCTypeModel.validateSettings(settings);
        m_ignoretags.validateSettings(settings);
        m_termColModel.validateSettings(settings);
        m_noOfRows.validateSettings(settings);
        m_allRows.validateSettings(settings);
    }


    /**
     * @return the kind of calculation for the TagCloud
     */
    public String getTCcalcType() {
        return m_calcTCTypeModel.getStringValue();
    }

    /**
     * @return the selected column ID of the value column.
     */
    public int getValueCol() {
        return m_valueColIndex;
    }

    /**
     * @return the input data table
     */
    public DataTable getData() {
        if (m_data != null) {
            return m_data;
        }
        return null;
    }

    /**
     * @return the chosen Column id Containing the Term
     */
    public int getTermCol() {
        return m_termColIndex;
    }


    /**
     * @return the pre calculated TagCloud data
     */
    public TagCloud getTagCloud() {
        return m_tagcloud;
    }

    /**
     * @return true if tags should be ignored
     */
    public boolean ignoreTags() {
        return m_ignoretags.getBooleanValue();
    }

}

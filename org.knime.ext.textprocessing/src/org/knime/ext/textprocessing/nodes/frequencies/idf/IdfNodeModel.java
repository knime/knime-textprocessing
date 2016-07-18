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
 *   18.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.idf;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyNodeModel;

/**
 * The model of the IDF-Node, specifying the proper cell factory
 * {@link org.knime.ext.textprocessing.nodes.frequencies.idf.IdfCellFactory}.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class IdfNodeModel extends FrequencyNodeModel {


    /**
     * idf smooth
     * @since 3.3
     */
    public static final String IDF_SMOOTH = "smooth";


    /**
     * idf normalized
     * @since 3.3
     */
    public static final String IDF_NORMALZED = "normalized";


    /**
     * idf probabilistic
     * @since 3.3
     */
    public static final String IDF_PROBABILISTIC = "probabilistic";

    /**
     * @since 3.3
     */
    public final static String[] m_idfMethods = new String[]{IDF_SMOOTH, IDF_NORMALZED, IDF_PROBABILISTIC};


    private SettingsModelString m_idfListModel = IdfNodeDialog.getIdfOptionsModel();

    /**
     * Creates a new instance of {@IdfNodeModel}.
     */
    public IdfNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initCellFactory(final BufferedDataTable inData,
            final ExecutionContext exec) throws CanceledExecutionException {
        m_cellFac = new IdfCellFactory(getDocumentColIndex(), getTermColIndex(),
              inData, exec, m_idfListModel.getStringValue());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        try {
            m_idfListModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_idfListModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        try {
            m_idfListModel.validateSettings(settings);
        } catch (InvalidSettingsException e) { }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        DataColumnSpec freq =
            new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(
                            inDataSpec, IdfCellFactory.COLNAME),
                            DoubleCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(freq));
    }

    /**
     * @return  smooth The default idf computation method
     * @since 3.3
     */
    public static String getDefaultMethod() {
        return IDF_SMOOTH;
    }
}

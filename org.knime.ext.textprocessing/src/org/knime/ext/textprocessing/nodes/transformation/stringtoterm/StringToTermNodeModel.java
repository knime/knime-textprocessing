/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StringToTermNodeModel extends NodeModel {

    /**
     * The name of the term column to append.
     */
    static final String TERM_COLNAME =
        BagOfWordsDataTableBuilder.DEF_TERM_COLNAME;

    private SettingsModelString m_stringColModel =
        StringToTermNodeDialog.getStringColModel();

    private int m_stringColIndex = -1;

    /**
     * Creates new instance of <code>StringToTermNodeModel</code>.
     */
    public StringToTermNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec spec = inSpecs[0];

        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumStringCells(1, true);

        // checking specified string col and guessing first available string col if no col ist set.
        int colIndex = spec.findColumnIndex(m_stringColModel.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < spec.getNumColumns(); i++) {
                if (spec.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    this.setWarningMessage("Guessing string column \""
                            + spec.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
        }
        // if guess was not successful (no string col available), throw error
        if (colIndex < 0) {
            throw new InvalidSettingsException("Input table contains no string columns!");
        }
        // otherwise set string column index
        m_stringColIndex = colIndex;

        return new DataTableSpec[]{createDataTableSpec(inSpecs[0])};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        BufferedDataTable bfd = inData[0];

        CellFactory fac = new TermCellFactory(m_stringColIndex, bfd.getDataTableSpec());
        ColumnRearranger rearranger = new ColumnRearranger(
                bfd.getDataTableSpec());
        rearranger.append(fac);

        return new BufferedDataTable[]{exec.createColumnRearrangeTable(
                bfd, rearranger, exec)};
    }

    private static final DataTableSpec createDataTableSpec(
            final DataTableSpec inSpec) {
        return new DataTableSpec(inSpec,
                new DataTableSpec(getTermColumnSpec(inSpec)));
    }

    /**
     * @param spec The incoming <code>DataTableSpec</code>.
     * @return The column spec of the term column to append.
     */
    static final DataColumnSpec getTermColumnSpec(final DataTableSpec spec) {
        return new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, TERM_COLNAME),
                TermCell.TYPE).createSpec();
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_stringColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_stringColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_stringColModel.validateSettings(settings);
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
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

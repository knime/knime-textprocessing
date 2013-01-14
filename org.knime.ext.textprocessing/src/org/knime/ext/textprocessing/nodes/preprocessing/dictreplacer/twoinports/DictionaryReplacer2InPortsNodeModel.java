/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 * -------------------------------------------------------------------
 *
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.twoinports;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.DictionaryReplacer;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacer2InPortsNodeModel extends
PreprocessingNodeModel {
    private final SettingsModelString m_replaceColumModel =
        DictionaryReplacer2InPortsNodeDialog.getReplaceColumnModel();

    private final SettingsModelString m_replacementColumModel =
        DictionaryReplacer2InPortsNodeDialog.getReplacementColumnModel();

    /**
     * Constructor of <code>DictionaryReplacer2InPortsNodeModel</code>.
     */
    public DictionaryReplacer2InPortsNodeModel() {
        super(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[1]);
        verifier.verifyMinimumStringCells(2, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalExecute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        final int replaceColIndex =
            inData[1].getDataTableSpec().findColumnIndex(
                    m_replaceColumModel.getStringValue());
        final int replacementColIndex =
            inData[1].getDataTableSpec().findColumnIndex(
                    m_replacementColumModel.getStringValue());

        Hashtable<String, String> dictionary = new Hashtable<String, String>();

        RowIterator it = inData[1].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();

            if (!row.getCell(replaceColIndex).isMissing()
                    && !row.getCell(replacementColIndex).isMissing()) {
                String key =
                    ((StringValue)row.getCell(replaceColIndex)).toString();
                String value =
                    ((StringValue)row.getCell(replacementColIndex)).toString();

                dictionary.put(key, value);
            }
        }

        m_preprocessing = new DictionaryReplacer(dictionary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        // Nothing to do here since preprocessor has been initialized in
        // internalExecute.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_replaceColumModel.loadSettingsFrom(settings);
        m_replacementColumModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_replaceColumModel.saveSettingsTo(settings);
        m_replacementColumModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_replaceColumModel.validateSettings(settings);
        m_replacementColumModel.validateSettings(settings);
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

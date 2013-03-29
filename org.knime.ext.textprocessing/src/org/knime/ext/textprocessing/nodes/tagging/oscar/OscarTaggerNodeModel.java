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
 * ---------------------------------------------------------------------
 *
 * History
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.oscar;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * The node model of the Oscar chemical named entity tagger.
 * Extends {@link org.knime.core.node.NodeModel} and provides methods to
 * configure and execute the node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class OscarTaggerNodeModel extends NodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;

    private int m_docColIndex = -1;

    private SettingsModelBoolean m_setUnmodifiableModel =
        OscarTaggerNodeDialog.createSetUnmodifiableModel();

    private DocumentDataTableBuilder m_dtBuilder;

    /**
     * Creates a new instance of <code>OscarTaggerNodeModel</code> with one
     * table in and one out port.
     */
    public OscarTaggerNodeModel() {
        super(1, 1);
        m_dtBuilder = new DocumentDataTableBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    private void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(spec);
        verfier.verifyDocumentCell(true);
        m_docColIndex = verfier.getDocumentCellIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        DocumentTagger tagger = new OscarDocumentTagger(
                m_setUnmodifiableModel.getBooleanValue());

        RowIterator it = inData[0].iterator();
        int rowCount = inData[0].getRowCount();
        int currDoc = 1;
        m_dtBuilder.openDataTable(exec);
        while (it.hasNext()) {

            double progress = (double)currDoc / (double)rowCount;
            exec.setProgress(progress, "Tagging document " + currDoc + " of "
                    + rowCount);
            exec.checkCanceled();
            currDoc++;

            DataRow row = it.next();
            DocumentValue docVal = (DocumentValue)row.getCell(m_docColIndex);
            m_dtBuilder.addDocument(tagger.tag(docVal.getDocument()),
                                    row.getKey());
        }

        return new BufferedDataTable[]{m_dtBuilder.getAndCloseDataTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_setUnmodifiableModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_setUnmodifiableModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_setUnmodifiableModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // empty ...
    }
}

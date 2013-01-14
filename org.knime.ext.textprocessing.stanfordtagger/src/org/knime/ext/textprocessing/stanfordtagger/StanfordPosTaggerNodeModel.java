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
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.stanfordtagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * The node model of the POS (part of speech) tagger. Extends
 * {@link org.knime.core.node.NodeModel} and provides methods to configure and
 * execute the node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StanfordPosTaggerNodeModel extends NodeModel {

    private int m_docColIndex = -1;

    private DocumentDataTableBuilder m_dtBuilder;
    
	private SettingsModelString m_modeFileModel = 
		StanfordPosTaggerNodeDialog.createStanfordModelModel();

	private SettingsModelString m_mappingFileModel = 
		StanfordPosTaggerNodeDialog.createMappingFileModel();

	private SettingsModelString m_mappingSeparator = 
		StanfordPosTaggerNodeDialog.createSeparatorModel();
	
    /**
     * Creates new instance of <code>PosTaggerNodeModel</code> which adds
     * part of speech tags to terms of documents.
     */
    public StanfordPosTaggerNodeModel() {
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
        List<Document> newDocuments = new ArrayList<Document>();
        
        TagsetMapper tm = new TagsetMapper(m_mappingFileModel.getStringValue(), 
        		m_mappingSeparator.getStringValue());
        
        DocumentTagger tagger = new StanfordPosDocumentTagger(false, 
        		m_modeFileModel.getStringValue(), tm);

        RowIterator it = inData[0].iterator();
        int rowCount = inData[0].getRowCount();
        int currDoc = 1;
        while (it.hasNext()) {
            double progress = (double)currDoc / (double)rowCount;
            exec.setProgress(progress, "Tagging document " + currDoc + " of "
                    + rowCount);
            exec.checkCanceled();
            currDoc++;

            DataRow row = it.next();
            DocumentValue docVal = (DocumentValue)row.getCell(m_docColIndex);
            newDocuments.add(tagger.tag(docVal.getDocument()));
        }

        return new BufferedDataTable[]{m_dtBuilder.createDataTable(
                        exec, newDocuments)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_modeFileModel.loadSettingsFrom(settings);
    	m_mappingFileModel.loadSettingsFrom(settings);
    	m_mappingSeparator.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_modeFileModel.saveSettingsTo(settings);
    	m_mappingFileModel.saveSettingsTo(settings);
    	m_mappingSeparator.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_modeFileModel.validateSettings(settings);
    	m_mappingFileModel.validateSettings(settings);
    }
}

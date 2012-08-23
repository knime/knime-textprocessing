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
 *   13.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentGrabberNodeModel extends NodeModel {

    /**
     * Default database to query.
     */
    public static final String DEFAULT_DATABASE = "PUBMED";

    /**
     * The maximum number of results.
     */
    public static final int MAX_RESULTS = Integer.MAX_VALUE;

    /**
     * The minimum number of results.
     */
    public static final int MIN_RESULTS = 1;

    /**
     * The default number of results.
     */
    public static final int DEF_RESULTS = 1000;

    /**
     * The default setting if results files are deleted after parsing.
     */
    public static final boolean DEF_DELETE_AFTER_PARSE = false;

    /**
     * The default target directory.
     */
    public static final String DEF_DIR = System.getProperty("user.home");


    private SettingsModelString m_queryModel =
        DocumentGrabberNodeDialog.getQueryModel();

    private SettingsModelIntegerBounded m_maxResultsModel =
        DocumentGrabberNodeDialog.getMaxResultsModel();

    private SettingsModelString m_dataBaseModel =
        DocumentGrabberNodeDialog.getDataBaseModel();

    private SettingsModelBoolean m_deleteFilesModel =
        DocumentGrabberNodeDialog.getDeleteFilesModel();

    private SettingsModelString m_directoryModel =
        DocumentGrabberNodeDialog.getDirectoryModel();

    private SettingsModelString m_categoryModel =
        DocumentGrabberNodeDialog.getDocumentCategoryModel();

    private SettingsModelString m_typeModel =
        DocumentGrabberNodeDialog.getDocumentTypeModel();

    private SettingsModelBoolean m_extractMetaInfoSettingsModel =
        DocumentGrabberNodeDialog.getExtractMetaInfoModel();

    private DocumentDataTableBuilder m_dtBuilder;

    /**
     * Creates new instance of <code>DocumentGrabberNodeModel</code>.
     */
    public DocumentGrabberNodeModel() {
        super(0, 1);
        m_dtBuilder = new DocumentDataTableBuilder();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DocumentGrabber grabber =
            DocumentGrabberFactory.getInstance().getGrabber(
                    m_dataBaseModel.getStringValue());

        m_dtBuilder.openDataTable(exec);

        if (grabber != null)  {
            String queryStr = m_queryModel.getStringValue();
            Query query = new Query(queryStr, m_maxResultsModel.getIntValue());

            if (grabber instanceof AbstractDocumentGrabber) {
                boolean delete = m_deleteFilesModel.getBooleanValue();
                DocumentCategory cat = new DocumentCategory(
                        m_categoryModel.getStringValue());

                ((AbstractDocumentGrabber)grabber).setDeleteFiles(delete);
                ((AbstractDocumentGrabber)grabber).setDocumentCategory(cat);
                ((AbstractDocumentGrabber)grabber).setExtractMetaInfo(
                        m_extractMetaInfoSettingsModel.getBooleanValue());
                ((AbstractDocumentGrabber)grabber).setExec(exec);
            }

            grabber.removeAllDocumentParsedListener();
            grabber.addDocumentParsedListener(
                    new InternalDocumentParsedEventListener());
            grabber.fetchAndParseDocuments(
                    new File(m_directoryModel.getStringValue()), query);
        }

        return new BufferedDataTable[]{m_dtBuilder.getAndCloseDataTable()};
    }

    private class InternalDocumentParsedEventListener implements
    DocumentParsedEventListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void documentParsed(final DocumentParsedEvent event) {
            if (m_dtBuilder != null) {
                Document d = event.getDocument();
                if (d != null) {
                    m_dtBuilder.addDocument(d);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        try {
            m_dtBuilder.getAndCloseDataTable();
        } catch (Exception e) { /* Do noting just try */ }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_queryModel.saveSettingsTo(settings);
        m_categoryModel.saveSettingsTo(settings);
        m_dataBaseModel.saveSettingsTo(settings);
        m_deleteFilesModel.saveSettingsTo(settings);
        m_directoryModel.saveSettingsTo(settings);
        m_maxResultsModel.saveSettingsTo(settings);
        m_typeModel.saveSettingsTo(settings);
        m_extractMetaInfoSettingsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_queryModel.validateSettings(settings);
        m_categoryModel.validateSettings(settings);
        m_dataBaseModel.validateSettings(settings);
        m_deleteFilesModel.validateSettings(settings);
        m_directoryModel.validateSettings(settings);
        m_maxResultsModel.validateSettings(settings);
        m_typeModel.validateSettings(settings);
        m_directoryModel.validateSettings(settings);

        try {
            m_extractMetaInfoSettingsModel.validateSettings(settings);
        } catch (InvalidSettingsException e) {
            // catch for the sake of downward compatibility
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_queryModel.loadSettingsFrom(settings);
        m_categoryModel.loadSettingsFrom(settings);
        m_dataBaseModel.loadSettingsFrom(settings);
        m_deleteFilesModel.loadSettingsFrom(settings);
        m_directoryModel.loadSettingsFrom(settings);
        m_maxResultsModel.loadSettingsFrom(settings);
        m_typeModel.loadSettingsFrom(settings);

        try {
            m_extractMetaInfoSettingsModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            // catch for the sake of downward compatibility
        }
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

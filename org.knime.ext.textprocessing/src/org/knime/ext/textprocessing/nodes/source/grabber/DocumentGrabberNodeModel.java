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
 *   13.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
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

    /**
     * The name of the query column.
     */
    static final String QUERYCOL_NAME = "Query";

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

    private SettingsModelBoolean m_appendQueryColumnModel =
            DocumentGrabberNodeDialog.getAppendQueryColumnModel();

    private SettingsModelString m_tokenizerModel =
            DocumentGrabberNodeDialog.getTokenizerModel();

    private DocumentDataTableBuilder m_dtBuilder = new DocumentDataTableBuilder(m_tokenizerModel.getStringValue());

    /**
     * Creates new instance of <code>DocumentGrabberNodeModel</code>.
     */
    public DocumentGrabberNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        m_dtBuilder = new DocumentDataTableBuilder(m_tokenizerModel.getStringValue());
        return new DataTableSpec[]{createColumnRearranger(m_dtBuilder.createDataTableSpec()).createSpec()};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec dataSpec) throws InvalidSettingsException {
        // check target directory
        File dir = new File(m_directoryModel.getStringValue());
        if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
            throw new InvalidSettingsException("Directory " + m_directoryModel.getStringValue()
                + " cannot be accessed.");
        }

        ColumnRearranger cR = new ColumnRearranger(dataSpec);
        if (m_appendQueryColumnModel.getBooleanValue()) {
            cR.append(new QueryStringCellFactory(QUERYCOL_NAME, m_queryModel.getStringValue()));
        }
        return cR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DocumentGrabber grabber = DocumentGrabberFactory.getInstance().getGrabber(m_dataBaseModel.getStringValue());

        try {
            m_dtBuilder.openDataTable(exec);

            if (grabber != null) {
                String queryStr = m_queryModel.getStringValue();
                Query query = new Query(queryStr, m_maxResultsModel.getIntValue());

                if (grabber instanceof AbstractDocumentGrabber) {
                    boolean delete = m_deleteFilesModel.getBooleanValue();
                    DocumentCategory cat = new DocumentCategory(m_categoryModel.getStringValue());

                    ((AbstractDocumentGrabber)grabber).setDeleteFiles(delete);
                    ((AbstractDocumentGrabber)grabber).setDocumentCategory(cat);
                    ((AbstractDocumentGrabber)grabber).setExtractMetaInfo(m_extractMetaInfoSettingsModel
                        .getBooleanValue());
                    ((AbstractDocumentGrabber)grabber).setTokenizerName(m_tokenizerModel.getStringValue());
                    ((AbstractDocumentGrabber)grabber).setExec(exec);
                }

                grabber.removeAllDocumentParsedListener();
                grabber.addDocumentParsedListener(new InternalDocumentParsedEventListener());
                grabber.fetchAndParseDocuments(new File(m_directoryModel.getStringValue()), query);
            }

            BufferedDataTable docTable = m_dtBuilder.getAndCloseDataTable();
            if (m_appendQueryColumnModel.getBooleanValue()) {
                ColumnRearranger cR = createColumnRearranger(docTable.getDataTableSpec());
                docTable = exec.createColumnRearrangeTable(docTable, cR, exec);
            }

            return new BufferedDataTable[]{docTable};
        } finally {
            m_dtBuilder.closeCache();
        }
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
        m_appendQueryColumnModel.saveSettingsTo(settings);
        m_tokenizerModel.saveSettingsTo(settings);
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

        // only validate if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.validateSettings(settings);
        }
        if (settings.containsKey(m_extractMetaInfoSettingsModel.getConfigName())) {
            m_extractMetaInfoSettingsModel.validateSettings(settings);
        }
        if (settings.containsKey(m_appendQueryColumnModel.getConfigName())) {
            m_appendQueryColumnModel.validateSettings(settings);
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

        // only load if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.loadSettingsFrom(settings);
        }
        if (settings.containsKey(m_extractMetaInfoSettingsModel.getConfigName())) {
            m_extractMetaInfoSettingsModel.loadSettingsFrom(settings);
        }
        if (settings.containsKey(m_appendQueryColumnModel.getConfigName())) {
            m_appendQueryColumnModel.loadSettingsFrom(settings);
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


    private class QueryStringCellFactory extends AbstractCellFactory {

        private String m_query;

        QueryStringCellFactory(final String queryColName, final String query) {
            super(new DataColumnSpecCreator(queryColName, StringCell.TYPE).createSpec());
            m_query = query;
        }

        /* (non-Javadoc)
         * @see org.knime.core.data.container.CellFactory#getCells(org.knime.core.data.DataRow)
         */
        @Override
        public DataCell[] getCells(final DataRow row) {
            return new DataCell[]{new StringCell(m_query)};
        }

    }
}

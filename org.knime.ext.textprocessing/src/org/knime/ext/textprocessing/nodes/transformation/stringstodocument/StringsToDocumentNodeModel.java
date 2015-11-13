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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentNodeModel extends NodeModel {

    private static final int INPORT = 0;

    private SettingsModelString m_titleColModel = StringsToDocumentNodeDialog.getTitleStringModel();

    private SettingsModelString m_fulltextColModel = StringsToDocumentNodeDialog.getTextStringModel();

    private SettingsModelString m_authorsColModel = StringsToDocumentNodeDialog.getAuthorsStringModel();

    private SettingsModelString m_authorNameSeparator = StringsToDocumentNodeDialog.getAuthorSplitStringModel();

    private SettingsModelString m_docSourceModel = StringsToDocumentNodeDialog.getDocSourceModel();

    private SettingsModelString m_docCategoryModel = StringsToDocumentNodeDialog.getDocCategoryModel();

    private SettingsModelString m_docTypeModel = StringsToDocumentNodeDialog.getTypeModel();

    private SettingsModelString m_pubDateModel = StringsToDocumentNodeDialog.getPubDatModel();

    private SettingsModelBoolean m_useCatColumnModel = StringsToDocumentNodeDialog.getUseCategoryColumnModel();

    private SettingsModelBoolean m_useSourceColumnModel = StringsToDocumentNodeDialog.getUseSourceColumnModel();

    private SettingsModelString m_catColumnModel = StringsToDocumentNodeDialog.getCategoryColumnModel();

    private SettingsModelString m_sourceColumnModel = StringsToDocumentNodeDialog.getSourceColumnModel();

    private SettingsModelIntegerBounded m_maxThreads = StringsToDocumentNodeDialog.getNumberOfThreadsModel();

    /** The default number of threads value. */
    static final int DEF_THREADS = Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
        (int)Math.ceil(Runtime.getRuntime().availableProcessors()));

    /** The min number of threads. */
    static final int MIN_THREADS = 1;

    /** The max number of threads. */
    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    /**
     * Creates new instance of <code>StringsToDocumentNodeModel</code>.
     */
    public StringsToDocumentNodeModel() {
        super(1, 1);
        m_useCatColumnModel.addChangeListener(
                new CategorySourceUsageChanceListener());
        m_useSourceColumnModel.addChangeListener(
                new CategorySourceUsageChanceListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[INPORT]);
        verifier.verifyMinimumStringCells(1, true);
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INPORT])};
    }

    private static final DataTableSpec createDataTableSpec(final DataTableSpec inDataSpec) {
        return new DataTableSpec(inDataSpec, new DataTableSpec(createNewColSpecs()));
    }

    private static final DataColumnSpec[] createNewColSpecs() {
        final TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataColumnSpec docCol = new DataColumnSpecCreator("Document", docFactory.getDataType()).createSpec();
        return new DataColumnSpec[]{docCol};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        BufferedDataTable inDataTable = inData[INPORT];
        StringsToDocumentConfig conf = new StringsToDocumentConfig();

        // Title
        int titleIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_titleColModel.getStringValue());
        conf.setTitleStringIndex(titleIndex);

        // Fulltext
        int fulltextIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_fulltextColModel.getStringValue());
        conf.setFulltextStringIndex(fulltextIndex);

        // Author names
        int authorIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_authorsColModel.getStringValue());
        conf.setAuthorsStringIndex(authorIndex);

        // Author name separator
        String authorNameSeparator = m_authorNameSeparator.getStringValue();
        if (!authorNameSeparator.isEmpty()
                && authorNameSeparator.length() > 0) {
            conf.setAuthorsSplitChar(authorNameSeparator);
        }

        // Document source
        String docSource = m_docSourceModel.getStringValue();
        if (!docSource.isEmpty() && docSource.length() > 0) {
            conf.setDocSource(docSource);
        }
        int docSourceIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_sourceColumnModel.getStringValue());
        conf.setSourceStringIndex(docSourceIndex);
        boolean useSourceCol = m_useSourceColumnModel.getBooleanValue();
        conf.setUseSourceColumn(useSourceCol);

        // Document category
        String docCat = m_docCategoryModel.getStringValue();
        if (!docCat.isEmpty() && docCat.length() > 0) {
            conf.setDocCat(docCat);
        }
        int docCatIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_catColumnModel.getStringValue());
        conf.setCategoryStringIndex(docCatIndex);
        boolean useCatColumn = m_useCatColumnModel.getBooleanValue();
        conf.setUseCatColumn(useCatColumn);

        // Document type
        String docType = m_docTypeModel.getStringValue();
        if (!docType.isEmpty() && docType.length() > 0) {
            conf.setDocType(docType);
        }

        // Publication Date
        String pubDate = m_pubDateModel.getStringValue();
        if (!pubDate.isEmpty() && pubDate.length() > 0) {
            conf.setPublicationDate(pubDate);
        }

        StringsToDocumentCellFactory cellFac = new StringsToDocumentCellFactory(conf, exec, createNewColSpecs(),
            m_maxThreads.getIntValue());
        try {
            ColumnRearranger rearranger = new ColumnRearranger(inDataTable.getDataTableSpec());
            rearranger.append(cellFac);
            return new BufferedDataTable[]{exec.createColumnRearrangeTable(inDataTable, rearranger, exec)};
        } finally {
            cellFac.closeCache();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fulltextColModel.loadSettingsFrom(settings);
        m_authorsColModel.loadSettingsFrom(settings);
        m_titleColModel.loadSettingsFrom(settings);
        m_authorNameSeparator.loadSettingsFrom(settings);
        m_docSourceModel.loadSettingsFrom(settings);
        m_docCategoryModel.loadSettingsFrom(settings);
        m_docTypeModel.loadSettingsFrom(settings);
        m_pubDateModel.loadSettingsFrom(settings);

        try {
            m_useCatColumnModel.loadSettingsFrom(settings);
            m_useSourceColumnModel.loadSettingsFrom(settings);
            m_catColumnModel.loadSettingsFrom(settings);
            m_sourceColumnModel.loadSettingsFrom(settings);
            m_maxThreads.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            // don't throw error msg
        }
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
        m_fulltextColModel.saveSettingsTo(settings);
        m_authorsColModel.saveSettingsTo(settings);
        m_titleColModel.saveSettingsTo(settings);
        m_authorNameSeparator.saveSettingsTo(settings);
        m_docSourceModel.saveSettingsTo(settings);
        m_docCategoryModel.saveSettingsTo(settings);
        m_docTypeModel.saveSettingsTo(settings);
        m_pubDateModel.saveSettingsTo(settings);
        m_useCatColumnModel.saveSettingsTo(settings);
        m_useSourceColumnModel.saveSettingsTo(settings);
        m_catColumnModel.saveSettingsTo(settings);
        m_sourceColumnModel.saveSettingsTo(settings);
        m_maxThreads.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fulltextColModel.validateSettings(settings);
        m_authorsColModel.validateSettings(settings);
        m_titleColModel.validateSettings(settings);
        m_authorNameSeparator.validateSettings(settings);
        m_docSourceModel.validateSettings(settings);
        m_docCategoryModel.validateSettings(settings);
        m_docTypeModel.validateSettings(settings);
        m_pubDateModel.validateSettings(settings);

        try {
            m_useCatColumnModel.validateSettings(settings);
            m_useSourceColumnModel.validateSettings(settings);
            m_catColumnModel.validateSettings(settings);
            m_sourceColumnModel.validateSettings(settings);
            m_maxThreads.validateSettings(settings);
        } catch (InvalidSettingsException e) {
            // don't throw error msg
        }

        String pubDate = ((SettingsModelString)m_pubDateModel.
                createCloneWithValidatedValue(settings)).getStringValue();

        Pattern p = Pattern.compile("(\\d){2}-(\\d){2}-(\\d){4}");
        Matcher m = p.matcher(pubDate);
        if (!m.matches()) {
            throw new InvalidSettingsException(
                    "Publicationdate is not formatted properly (dd-mm-yyyy)!");
        }

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);
        try {
            df.parse(pubDate);
        } catch (ParseException e) {
            throw new InvalidSettingsException(
                     "Specified date is not valid!\n"
                    + e.getMessage());
        }
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
     * Enables and disables text fields of document source and category.
     * @author Kilian Thiel, KNIME.com, Berlin, Germany
     */
    class CategorySourceUsageChanceListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
                m_docCategoryModel.setEnabled(
                        !m_useCatColumnModel.getBooleanValue());
                m_docSourceModel.setEnabled(
                        !m_useSourceColumnModel.getBooleanValue());
        }
    }
}

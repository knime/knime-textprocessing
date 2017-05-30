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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.nodes.transformation.documenttostring.DocumentDataExtractor;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class StringsToDocumentNodeModel extends SimpleStreamableFunctionNodeModel {

    private SettingsModelString m_titleColModel = StringsToDocumentNodeDialog.getTitleStringModel();

    private SettingsModelString m_fulltextColModel = StringsToDocumentNodeDialog.getTextStringModel();

    private SettingsModelString m_authorsColModel = StringsToDocumentNodeDialog.getAuthorsStringModel();

    private SettingsModelString m_authorNameSeparator = StringsToDocumentNodeDialog.getAuthorSplitStringModel();

    private SettingsModelString m_authorFirstNameModel = StringsToDocumentNodeDialog.getAuthorFirstNameModel();

    private SettingsModelString m_authorLastNameModel = StringsToDocumentNodeDialog.getAuthorLastNameModel();

    private SettingsModelString m_docSourceModel = StringsToDocumentNodeDialog.getDocSourceModel();

    private SettingsModelString m_docCategoryModel = StringsToDocumentNodeDialog.getDocCategoryModel();

    private SettingsModelString m_docTypeModel = StringsToDocumentNodeDialog.getTypeModel();

    private SettingsModelString m_pubDateModel = StringsToDocumentNodeDialog.getPubDatModel();

    private SettingsModelString m_pubDateColModel = StringsToDocumentNodeDialog.getPubDateColumnModel();

    private SettingsModelBoolean m_useCatColumnModel = StringsToDocumentNodeDialog.getUseCategoryColumnModel();

    private SettingsModelBoolean m_useSourceColumnModel = StringsToDocumentNodeDialog.getUseSourceColumnModel();

    private SettingsModelString m_catColumnModel = StringsToDocumentNodeDialog.getCategoryColumnModel();

    private SettingsModelString m_sourceColumnModel = StringsToDocumentNodeDialog.getSourceColumnModel();

    private SettingsModelIntegerBounded m_maxThreads = StringsToDocumentNodeDialog.getNumberOfThreadsModel();

    private SettingsModelBoolean m_useTitleColumnModel = StringsToDocumentNodeDialog.getUseTitleColumnModel();

    private SettingsModelBoolean m_useAuthorsColumnModel = StringsToDocumentNodeDialog.getUseAuthorsColumnModel();

    private SettingsModelBoolean m_usePubDateColumnModel = StringsToDocumentNodeDialog.getUsePubDateColumnModel();

    private SettingsModelString m_tokenizerModel = StringsToDocumentNodeDialog.getTokenizerModel();

    /** The default number of threads value. */
    static final int DEF_THREADS = Math.max(1, Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
        (int)Math.ceil(Runtime.getRuntime().availableProcessors())));

    /** The min number of threads. */
    static final int MIN_THREADS = 1;

    /** The max number of threads. */
    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    /**
     * Creates new instance of {@StringsToDocumentNodeModel}.
     */
    public StringsToDocumentNodeModel() {
        modelStateChanged();
    }

    private final DataColumnSpec[] createNewColSpecs() {
        Map<String, String> props = new HashMap<String,String>();
        props.put(DocumentDataTableBuilder.WORD_TOKENIZER_KEY, m_tokenizerModel.getStringValue());
        final TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataColumnSpecCreator docColSpecCreator = new DataColumnSpecCreator("Document", docFactory.getDataType());
        docColSpecCreator.setProperties(new DataColumnProperties(props));
        return new DataColumnSpec[]{docColSpecCreator.createSpec()};
    }

    /** {@inheritDoc} */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumStringCells(1, true);

        StringsToDocumentConfig conf = new StringsToDocumentConfig();

        // Title
        String docTitle = m_titleColModel.getStringValue();
        if (!docTitle.isEmpty() && docTitle.length() > 0) {
            conf.setDocTitle(docTitle);
        }
        int titleIndex = spec.findColumnIndex(m_titleColModel.getStringValue());
        conf.setTitleStringIndex(titleIndex);
        boolean useTitleCol = m_useTitleColumnModel.getBooleanValue();
        conf.setUseTitleColumn(useTitleCol);

        // Fulltext
        int fulltextIndex = spec.findColumnIndex(m_fulltextColModel.getStringValue());
        conf.setFulltextStringIndex(fulltextIndex);

        // Author names
        String authorNames = m_authorsColModel.getStringValue();
        if (!authorNames.isEmpty() && authorNames.length() > 0) {
            conf.setAuthorNames(authorNames);
        }
        int authorIndex = spec.findColumnIndex(m_authorsColModel.getStringValue());
        conf.setAuthorsStringIndex(authorIndex);
        boolean useAuthorsCol = m_useAuthorsColumnModel.getBooleanValue();
        conf.setUseAuthorsColumn(useAuthorsCol);
        conf.setAuthorFirstName(m_authorFirstNameModel.getStringValue());
        conf.setAuthorLastName(m_authorLastNameModel.getStringValue());

        // Author name separator
        String authorNameSeparator = m_authorNameSeparator.getStringValue();
        if (!authorNameSeparator.isEmpty() && authorNameSeparator.length() > 0) {
            conf.setAuthorsSplitChar(authorNameSeparator);
        }

        // Document source
        String docSource = m_docSourceModel.getStringValue();
        if (!docSource.isEmpty() && docSource.length() > 0) {
            conf.setDocSource(docSource);
        }
        int docSourceIndex = spec.findColumnIndex(m_sourceColumnModel.getStringValue());
        conf.setSourceStringIndex(docSourceIndex);
        boolean useSourceCol = m_useSourceColumnModel.getBooleanValue();
        conf.setUseSourceColumn(useSourceCol);

        // Document category
        String docCat = m_docCategoryModel.getStringValue();
        if (!docCat.isEmpty() && docCat.length() > 0) {
            conf.setDocCat(docCat);
        }
        int docCatIndex = spec.findColumnIndex(m_catColumnModel.getStringValue());
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
        conf.setPublicationDate(pubDate);
        int pubDateIndex = spec.findColumnIndex(m_pubDateColModel.getStringValue());
        conf.setPubDateStringIndedx(pubDateIndex);
        boolean usePubDateColumn = m_usePubDateColumnModel.getBooleanValue();
        conf.setUsePubDateColumn(usePubDateColumn);

        StringsToDocumentCellFactory cellFac = new StringsToDocumentCellFactory(conf, createNewColSpecs(),
            m_maxThreads.getIntValue(), m_tokenizerModel.getStringValue());
        ColumnRearranger rearranger = new ColumnRearranger(spec);
        rearranger.append(cellFac);
        return rearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_fulltextColModel.loadSettingsFrom(settings);
        m_authorsColModel.loadSettingsFrom(settings);
        m_titleColModel.loadSettingsFrom(settings);
        m_authorNameSeparator.loadSettingsFrom(settings);
        m_docSourceModel.loadSettingsFrom(settings);
        m_docCategoryModel.loadSettingsFrom(settings);
        m_docTypeModel.loadSettingsFrom(settings);
        m_pubDateModel.loadSettingsFrom(settings);

        try {
            m_useSourceColumnModel.loadSettingsFrom(settings);
            m_useCatColumnModel.loadSettingsFrom(settings);
            m_catColumnModel.loadSettingsFrom(settings);
            m_sourceColumnModel.loadSettingsFrom(settings);
            m_maxThreads.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_authorFirstNameModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_authorLastNameModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_pubDateColModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try{
            m_useTitleColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try {
            m_useAuthorsColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try{
            m_usePubDateColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }

        // only validate if NodeSettings contain key (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.validateSettings(settings);
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
        m_pubDateColModel.saveSettingsTo(settings);
        m_useCatColumnModel.saveSettingsTo(settings);
        m_useSourceColumnModel.saveSettingsTo(settings);
        m_useTitleColumnModel.saveSettingsTo(settings);
        m_useAuthorsColumnModel.saveSettingsTo(settings);
        m_usePubDateColumnModel.saveSettingsTo(settings);
        m_catColumnModel.saveSettingsTo(settings);
        m_sourceColumnModel.saveSettingsTo(settings);
        m_maxThreads.saveSettingsTo(settings);
        m_authorFirstNameModel.saveSettingsTo(settings);
        m_authorLastNameModel.saveSettingsTo(settings);
        m_tokenizerModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_fulltextColModel.validateSettings(settings);
        m_authorsColModel.validateSettings(settings);
        m_titleColModel.validateSettings(settings);
        m_authorNameSeparator.validateSettings(settings);
        m_docSourceModel.validateSettings(settings);
        m_docCategoryModel.validateSettings(settings);
        m_docTypeModel.validateSettings(settings);
        m_pubDateModel.validateSettings(settings);

        try {
            m_useSourceColumnModel.loadSettingsFrom(settings);
            m_useCatColumnModel.loadSettingsFrom(settings);
            m_catColumnModel.loadSettingsFrom(settings);
            m_sourceColumnModel.loadSettingsFrom(settings);
            m_maxThreads.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_authorFirstNameModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_authorLastNameModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) { }
        try {
            m_pubDateColModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try{
            m_useTitleColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try {
            m_useAuthorsColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }
        try{
            m_usePubDateColumnModel.loadSettingsFrom(settings);
        } catch(InvalidSettingsException e){ }

        // only load if NodeSettings contain key (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.loadSettingsFrom(settings);
        }

        String pubDate = ((SettingsModelString)m_pubDateModel.createCloneWithValidatedValue(settings)).getStringValue();

        if(!pubDate.isEmpty()){
            Pattern p = Pattern.compile("(\\d){2}-(\\d){2}-(\\d){4}");
            Matcher m = p.matcher(pubDate);
            if (!m.matches()) {
                throw new InvalidSettingsException("Publicationdate is not formatted properly (dd-mm-yyyy)!");
            }

            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            df.setLenient(false);
            try {
                df.parse(pubDate);
            } catch (ParseException e) {
                throw new InvalidSettingsException("Specified date is not valid!\n" + e.getMessage());
            }

        }

    }

    /**
     * write a method that check if datatable contains 'title' and 'authors' cols the method should take a string as
     * parameter. If no title and authors document are available, generate them
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }


    /**
     * @since 3.3
     */
    public void modelStateChanged() {
        m_docCategoryModel.setEnabled(!m_useCatColumnModel.getBooleanValue());
        m_docSourceModel.setEnabled(!m_useSourceColumnModel.getBooleanValue());
        m_sourceColumnModel.setEnabled(m_useSourceColumnModel.getBooleanValue());
        m_catColumnModel.setEnabled(m_useCatColumnModel.getBooleanValue());
        m_titleColModel.setEnabled(m_useTitleColumnModel.getBooleanValue());
        m_authorsColModel.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
        m_pubDateColModel.setEnabled(m_usePubDateColumnModel.getBooleanValue());
        m_pubDateModel.setEnabled(!m_usePubDateColumnModel.getBooleanValue());
    }



    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        doSmartDialogSelection(inSpecs[0]);
        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizerModel.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizerModel.getStringValue());
        }
        return super.configure(inSpecs);
    }

    /**
     * @param dataTableSpec
     * @since 3.3
     */
    protected void doSmartDialogSelection(final DataTableSpec dataTableSpec) {
        String[] columns = dataTableSpec.getColumnNames();
        if (settingsNotConfigured()) {
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                if (column.equalsIgnoreCase(DocumentDataExtractor.TITLE.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)) {
                    m_titleColModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor.SOURCE.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)) {
                    m_sourceColumnModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor.AUTHOR.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)) {
                    m_authorsColModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor.CATEGORY.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)) {
                    m_catColumnModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor.TEXT.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)) {
                    m_fulltextColModel.setStringValue(column);
                }
                if(column.equalsIgnoreCase(DocumentDataExtractor.PUB_DATE.getName())
                    && dataTableSpec.getColumnSpec(column).getType().isCompatible(StringValue.class)){
                    m_pubDateColModel.setStringValue(column);


                }
            }
        }
    }

    /**
     * @return true if settings have not been configured before
     * @since 3.3
     */
    protected boolean settingsNotConfigured() {
        return (m_titleColModel.getStringValue().length() == 0 && m_authorsColModel.getStringValue().length() == 0
            && m_fulltextColModel.getStringValue().length() == 0 && m_docSourceModel.getStringValue().length() == 0
            && m_docCategoryModel.getStringValue().length() == 0 && m_pubDateColModel.getStringValue().length() == 0);
    }
}

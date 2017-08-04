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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.nodes.transformation.documenttostring.DocumentDataExtractor2;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The {@link NodeModel} for the Strings To Document node. This class extends the
 * {@link SimpleStreamableFunctionNodeModel} which provides streaming functionality.
 *
 * @author Hermann Azong & Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
class StringsToDocumentNodeModel2 extends SimpleStreamableFunctionNodeModel {

    private final SettingsModelString m_titleColModel = StringsToDocumentNodeDialog2.getTitleStringModel();

    private final SettingsModelString m_fulltextColModel = StringsToDocumentNodeDialog2.getTextStringModel();

    private final SettingsModelString m_authorsColModel = StringsToDocumentNodeDialog2.getAuthorsStringModel();

    private final SettingsModelString m_authorNameSeparator = StringsToDocumentNodeDialog2.getAuthorSplitStringModel();

    private final SettingsModelString m_authorFirstNameModel = StringsToDocumentNodeDialog2.getAuthorFirstNameModel();

    private final SettingsModelString m_authorLastNameModel = StringsToDocumentNodeDialog2.getAuthorLastNameModel();

    private final SettingsModelString m_docSourceModel = StringsToDocumentNodeDialog2.getDocSourceModel();

    private final SettingsModelString m_docCategoryModel = StringsToDocumentNodeDialog2.getDocCategoryModel();

    private final SettingsModelString m_docTypeModel = StringsToDocumentNodeDialog2.getTypeModel();

    private final SettingsModelString m_pubDateModel = StringsToDocumentNodeDialog2.getPubDatModel();

    private final SettingsModelString m_pubDateColModel = StringsToDocumentNodeDialog2.getPubDateColumnModel();

    private final SettingsModelBoolean m_useCatColumnModel = StringsToDocumentNodeDialog2.getUseCategoryColumnModel();

    private final SettingsModelBoolean m_useSourceColumnModel = StringsToDocumentNodeDialog2.getUseSourceColumnModel();

    private final SettingsModelString m_catColumnModel = StringsToDocumentNodeDialog2.getCategoryColumnModel();

    private final SettingsModelString m_sourceColumnModel = StringsToDocumentNodeDialog2.getSourceColumnModel();

    private final SettingsModelIntegerBounded m_maxThreads = StringsToDocumentNodeDialog2.getNumberOfThreadsModel();

    private final SettingsModelBoolean m_useTitleColumnModel = StringsToDocumentNodeDialog2.getUseTitleColumnModel();

    private final SettingsModelBoolean m_useAuthorsColumnModel =
        StringsToDocumentNodeDialog2.getUseAuthorsColumnModel();

    private final SettingsModelBoolean m_usePubDateColumnModel =
        StringsToDocumentNodeDialog2.getUsePubDateColumnModel();

    private final SettingsModelString m_tokenizerModel = StringsToDocumentNodeDialog2.getTokenizerModel();

    private final SettingsModelString m_docColumnModel = StringsToDocumentNodeDialog2.getDocumentColumnModel();

    /**
     * Creates new instance of {@code StringsToDocumentNodeModel2}.
     */
    StringsToDocumentNodeModel2() {
        modelStateChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        doSmartDialogSelection(inSpecs[0]);
        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizerModel.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizerModel.getStringValue());
        }
        return super.configure(inSpecs);
    }

    private final DataColumnSpec[] createNewColSpecs() {
        DataColumnSpecCreator docColSpecCreator = new DataColumnSpecCreator(m_docColumnModel.getStringValue(),
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory().getDataType());
        docColSpecCreator.setProperties(new DataColumnProperties(
            Collections.singletonMap(DocumentDataTableBuilder.WORD_TOKENIZER_KEY, m_tokenizerModel.getStringValue())));
        return new DataColumnSpec[]{docColSpecCreator.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumStringCells(1, true);

        StringsToDocumentConfig2 conf = new StringsToDocumentConfig2();

        // Title
        if (!m_titleColModel.getStringValue().isEmpty()) {
            conf.setDocTitle(m_titleColModel.getStringValue());
        }
        conf.setTitleColumnIndex(spec.findColumnIndex(m_titleColModel.getStringValue()));
        conf.setUseTitleColumn(m_useTitleColumnModel.getBooleanValue());

        // Fulltext
        conf.setFulltextColumnIndex(spec.findColumnIndex(m_fulltextColModel.getStringValue()));

        // Author names
        conf.setAuthorsColumnIndex(spec.findColumnIndex(m_authorsColModel.getStringValue()));
        conf.setUseAuthorsColumn(m_useAuthorsColumnModel.getBooleanValue());
        conf.setAuthorFirstName(m_authorFirstNameModel.getStringValue());
        conf.setAuthorLastName(m_authorLastNameModel.getStringValue());

        // Author name separator
        if (!m_authorNameSeparator.getStringValue().isEmpty()) {
            conf.setAuthorsSplitChar(m_authorNameSeparator.getStringValue());
        }

        // Document source
        if (!m_docSourceModel.getStringValue().isEmpty()) {
            conf.setDocSource(m_docSourceModel.getStringValue());
        }
        conf.setSourceColumnIndex(spec.findColumnIndex(m_sourceColumnModel.getStringValue()));
        conf.setUseSourceColumn(m_useSourceColumnModel.getBooleanValue());

        // Document category
        if (!m_docCategoryModel.getStringValue().isEmpty()) {
            conf.setDocCat(m_docCategoryModel.getStringValue());
        }
        conf.setCategoryColumnIndex(spec.findColumnIndex(m_catColumnModel.getStringValue()));
        conf.setUseCatColumn(m_useCatColumnModel.getBooleanValue());

        // Document type
        conf.setDocType(m_docTypeModel.getStringValue());

        // Publication Date
        conf.setPublicationDate(m_pubDateModel.getStringValue());
        conf.setPubDateColumnIndex(spec.findColumnIndex(m_pubDateColModel.getStringValue()));
        conf.setUsePubDateColumn(m_usePubDateColumnModel.getBooleanValue());

        // New document column
        if (spec.containsName(m_docColumnModel.getStringValue().trim())) {
            throw new InvalidSettingsException("Can't create new column \"" + m_docColumnModel.getStringValue()
            + "\" as input spec already contains such column!");
        }
        if (m_docColumnModel.getStringValue() == null || m_docColumnModel.getStringValue().trim().isEmpty()) {
            throw new InvalidSettingsException("Can't create new column! Column name can't be empty!");
        }

        StringsToDocumentCellFactory2 cellFac = new StringsToDocumentCellFactory2(conf, createNewColSpecs(),
            m_maxThreads.getIntValue(), m_tokenizerModel.getStringValue());

        ColumnRearranger rearranger = new ColumnRearranger(spec);
        rearranger.append(cellFac);

        return rearranger;
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_fulltextColModel.loadSettingsFrom(settings);
        m_authorsColModel.loadSettingsFrom(settings);
        m_titleColModel.loadSettingsFrom(settings);
        m_authorNameSeparator.loadSettingsFrom(settings);
        m_docSourceModel.loadSettingsFrom(settings);
        m_docCategoryModel.loadSettingsFrom(settings);
        m_docTypeModel.loadSettingsFrom(settings);
        m_pubDateModel.loadSettingsFrom(settings);
        m_useSourceColumnModel.loadSettingsFrom(settings);
        m_useCatColumnModel.loadSettingsFrom(settings);
        m_catColumnModel.loadSettingsFrom(settings);
        m_sourceColumnModel.loadSettingsFrom(settings);
        m_maxThreads.loadSettingsFrom(settings);
        m_authorFirstNameModel.loadSettingsFrom(settings);
        m_authorLastNameModel.loadSettingsFrom(settings);
        m_pubDateColModel.loadSettingsFrom(settings);
        m_useTitleColumnModel.loadSettingsFrom(settings);
        m_useAuthorsColumnModel.loadSettingsFrom(settings);
        m_usePubDateColumnModel.loadSettingsFrom(settings);
        m_tokenizerModel.loadSettingsFrom(settings);
        m_docColumnModel.loadSettingsFrom(settings);
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
        m_docColumnModel.saveSettingsTo(settings);
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
        m_useSourceColumnModel.validateSettings(settings);
        m_useCatColumnModel.validateSettings(settings);
        m_catColumnModel.validateSettings(settings);
        m_sourceColumnModel.validateSettings(settings);
        m_maxThreads.validateSettings(settings);
        m_authorFirstNameModel.validateSettings(settings);
        m_authorLastNameModel.validateSettings(settings);
        m_pubDateColModel.validateSettings(settings);
        m_useTitleColumnModel.validateSettings(settings);
        m_useAuthorsColumnModel.validateSettings(settings);
        m_usePubDateColumnModel.validateSettings(settings);
        m_tokenizerModel.validateSettings(settings);
        m_docColumnModel.validateSettings(settings);

        String pubDate = ((SettingsModelString)m_pubDateModel.createCloneWithValidatedValue(settings)).getStringValue();

        if (!pubDate.isEmpty()) {
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

    private void modelStateChanged() {
        m_docCategoryModel.setEnabled(!m_useCatColumnModel.getBooleanValue());
        m_docSourceModel.setEnabled(!m_useSourceColumnModel.getBooleanValue());
        m_sourceColumnModel.setEnabled(m_useSourceColumnModel.getBooleanValue());
        m_catColumnModel.setEnabled(m_useCatColumnModel.getBooleanValue());
        m_titleColModel.setEnabled(m_useTitleColumnModel.getBooleanValue());
        m_authorsColModel.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
        m_pubDateColModel.setEnabled(m_usePubDateColumnModel.getBooleanValue());
        m_pubDateModel.setEnabled(!m_usePubDateColumnModel.getBooleanValue());
    }

    private void doSmartDialogSelection(final DataTableSpec dataTableSpec) {
        String[] columns = dataTableSpec.getColumnNames();
        if (settingsNotConfigured()) {
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                DataType type = dataTableSpec.getColumnSpec(column).getType();
                if (column.equalsIgnoreCase(DocumentDataExtractor2.TITLE.getName())
                    && type.isCompatible(StringValue.class)) {
                    m_titleColModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor2.SOURCE.getName())
                    && type.isCompatible(StringValue.class)) {
                    m_sourceColumnModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor2.AUTHOR.getName())
                    && type.isCompatible(StringValue.class)) {
                    m_authorsColModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor2.CATEGORY.getName())
                    && type.isCompatible(StringValue.class)) {
                    m_catColumnModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor2.TEXT.getName())
                    && type.isCompatible(StringValue.class)) {
                    m_fulltextColModel.setStringValue(column);
                }
                if (column.equalsIgnoreCase(DocumentDataExtractor2.PUB_DATE.getName())
                    && (type.isCompatible(StringValue.class) || type.isCompatible(LocalDateValue.class)
                        || type.isCompatible(DateAndTimeValue.class))) {
                    m_pubDateColModel.setStringValue(column);
                }
            }
        }
    }

    private boolean settingsNotConfigured() {
        return (m_titleColModel.getStringValue().isEmpty() && m_authorsColModel.getStringValue().isEmpty()
            && m_fulltextColModel.getStringValue().isEmpty() && m_docSourceModel.getStringValue().isEmpty()
            && m_docCategoryModel.getStringValue().isEmpty() && m_pubDateColModel.getStringValue().isEmpty());
    }
}

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

import java.util.Collection;
import java.util.stream.Collectors;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.knime.time.util.DialogComponentDateTimeSelection;
import org.knime.time.util.DialogComponentDateTimeSelection.DisplayOption;
import org.knime.time.util.SettingsModelDateTime;

/**
 * Provides the dialog for the String to Document node with all necessary dialog components.
 *
 * @author Hermann Azong & Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
final class StringsToDocumentNodeDialog2 extends DefaultNodeSettingsPane {

    /**
     * Creates and returns a {@link SettingsModelString} specifying the column which has to be used as authors column.
     *
     * @return The {@code SettingsModelString} specifying the author's column.
     */
    static final SettingsModelString getAuthorsStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_AUTHORSCOL, "");
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the string that separates authors.
     *
     * @return The {@code SettingsModelString} specifying the separator string.
     */
    static final SettingsModelString getAuthorSplitStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_AUTHORSPLIT_STR,
            StringsToDocumentConfig2.DEF_AUTHORS_SPLITCHAR);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the author's first name.
     *
     * @return The {@code SettingsModelString} specifying the author's first name.
     */
    static final SettingsModelString getAuthorFirstNameModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_AUTHOR_FIRST_NAME,
            StringsToDocumentConfig2.DEF_AUTHOR_NAMES);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the author's last name.
     *
     * @return The {@code SettingsModelString} specifying the author's last name.
     */
    static final SettingsModelString getAuthorLastNameModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_AUTHOR_LAST_NAME,
            StringsToDocumentConfig2.DEF_AUTHOR_NAMES);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the column which has to be used as
     * title column.
     *
     * @return The {@code SettingsModelString} specifying the column which has to be used as title column.
     */
    static final SettingsModelString getTitleStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_TITLECOL, "");
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the column which has to be used as full
     * text column.
     *
     * @return The {@code SettingsModelString} specifying the column which has to be used as full text column.
     */
    static final SettingsModelString getTextStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_TEXTCOL, "");
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the document source.
     *
     * @return The {@code SettingsModelString} specifying the document source.
     */
    static final SettingsModelString getDocSourceModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_DOCSOURCE,
            StringsToDocumentConfig2.DEF_DOCUMENT_SOURCE);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the document category.
     *
     * @return The {@code SettingsModelString} specifying the document category.
     */
    static final SettingsModelString getDocCategoryModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_DOCCAT,
            StringsToDocumentConfig2.DEF_DOCUMENT_CATEGORY);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the document type.
     *
     * @return The {@code SettingsModelString} specifying the document type.
     */
    static final SettingsModelString getTypeModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_DOCTYPE,
            StringsToDocumentConfig2.DEF_DOCUMENT_TYPE);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelDateTime} specifying the document publication date.
     *
     * @return The {@code SettingsModelDateTime} specifying the document publication date.
     */
    static final SettingsModelDateTime getPubDateModel() {
        return new SettingsModelDateTime(StringsToDocumentConfigKeys2.CFGKEY_PUBDATE,
            StringsToDocumentConfig2.DEF_DOCUMENT_PUBDATE, null, null);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying whether a column is used for category
     * values or not.
     *
     * @return The {@code SettingsModelString} specifying whether a column is used for category values or not.
     */
    static final SettingsModelBoolean getUseCategoryColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys2.CFGKEY_USE_CATCOLUMN,
            StringsToDocumentConfig2.DEF_USE_CATCOLUMN);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying whether a column is used for source
     * values or not.
     *
     * @return The {@code SettingsModelString} specifying whether a column is used for source values or not.
     */
    static final SettingsModelBoolean getUseSourceColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys2.CFGKEY_USE_SOURCECOLUMN,
            StringsToDocumentConfig2.DEF_USE_SOURCECOLUMN);
    }

    /**
     * Creates and return an instance of {@link SettingsModelBoolean} specifying whether a column is used for title
     * values or not
     *
     * @return The {@code SettingsModelBoolean} specifying whether a column is used for title values or not
     */
    static final SettingsModelBoolean getUseTitleColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys2.CFGKEY_USE_TITLECOLUMN,
            StringsToDocumentConfig2.DEF_USE_TITLECOLUMN);
    }

    /**
     * Creates and return an instance of {@link SettingsModelBoolean} specifying whether a column is used for authors
     * values or not
     *
     * @return The {@code SettingsModelBoolean} specifying whether a column is used for authors values or not
     */
    static final SettingsModelBoolean getUseAuthorsColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys2.CFGKEY_USE_AUTHORSCOLUMN,
            StringsToDocumentConfig2.DEF_USE_AUTHORSCOLUMN);
    }

    /**
     * Creates and return an instance of {@link SettingsModelBoolean} specifying whether a column is used for
     * publication dates or not
     *
     * @return The {@code SettingsModelBoolean} specifying whether a column is used for publication dates or not
     */
    static final SettingsModelBoolean getUsePubDateColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys2.CFGKEY_USE_PUBDATECOLUMN,
            StringsToDocumentConfig2.DEF_USE_PUBDATECOLUMN);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the column with the category values.
     *
     * @return The {@code SettingsModelString} specifying the column with the category values.
     */
    static final SettingsModelString getCategoryColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_CATCOLUMN, "");
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the column with the source values.
     *
     * @return The {@code SettingsModelString} specifying the column with the source values.
     */
    static final SettingsModelString getSourceColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_SOURCECOLUMN, "");
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} specifying the column which has to be used as
     * publication date
     *
     * @return The {@code SettingsModelString} specifying the column which has to be used as publication date
     */
    static final SettingsModelString getPubDateColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_PUBDATECOL, "");

    }

    /**
     * Creates and returns an instance of {@link SettingsModelIntegerBounded}, storing the number of maximal parallel
     * threads running.
     *
     * @return The {@code SettingsModelIntegerBounded} with number of maximal parallel threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(StringsToDocumentConfigKeys2.CFGKEY_THREADS,
            Math.max(1,
                Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
                    (int)Math.ceil(Runtime.getRuntime().availableProcessors()))),
            StringsToDocumentConfig2.MIN_THREADS, KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads());
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString}, storing the name of the tokenizer used for word
     * tokenization.
     *
     * @return The {@code SettingsModelString} storing the name of the word tokenizer.
     */
    static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString}, storing the name of the document column to be
     * created.
     *
     * @return The {@code SettingsModelString} storing the name of the document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys2.CFGKEY_DOCCOLUMN,
            StringsToDocumentConfig2.DEF_DOC_COLUMN);
    }

    private SettingsModelString m_docCategoryModel;

    private SettingsModelDateTime m_pubDateModel;

    private SettingsModelString m_docTitleModelCombo;

    private SettingsModelString m_docAuthorModelCombo;

    private SettingsModelString m_docSourceModel;

    private SettingsModelString m_docSourceModelCombo;

    private SettingsModelString m_docCatModelCombo;

    private SettingsModelString m_pubDateModelCombo;

    private SettingsModelBoolean m_useCatColumnModel;

    private SettingsModelBoolean m_useSourceColumnModel;

    private SettingsModelBoolean m_useTitleColumnModel;

    private SettingsModelBoolean m_usePubDateColumnModel;

    private SettingsModelBoolean m_useAuthorsColumnModel;

    private SettingsModelString m_docColModel;

    private DataTableSpec m_spec;

    /**
     * Creates a new instance of {@StringsToDocumentNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public StringsToDocumentNodeDialog2() {

        createNewGroup("Text");
        setHorizontalPlacement(true);
        m_useTitleColumnModel = getUseTitleColumnModel();
        m_docTitleModelCombo = getTitleStringModel();
        m_useTitleColumnModel.addChangeListener(e -> stateChanged());
        addDialogComponent(new DialogComponentBoolean(m_useTitleColumnModel, "Use title from column"));
        addDialogComponent(
            new DialogComponentColumnNameSelection(m_docTitleModelCombo, "Title column", 0, StringValue.class));

        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        m_useAuthorsColumnModel = getUseAuthorsColumnModel();
        m_docAuthorModelCombo = getAuthorsStringModel();
        m_useAuthorsColumnModel.addChangeListener(e -> stateChanged());
        addDialogComponent(new DialogComponentBoolean(m_useAuthorsColumnModel, "Use authors from column"));
        addDialogComponent(
            new DialogComponentColumnNameSelection(m_docAuthorModelCombo, "Authors column", 0, StringValue.class));

        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentString(getAuthorSplitStringModel(), "Author names separator"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentString(getAuthorFirstNameModel(), "Default author first name"));
        addDialogComponent(new DialogComponentString(getAuthorLastNameModel(), "Default author last name"));
        setHorizontalPlacement(false);
        addDialogComponent(
            new DialogComponentColumnNameSelection(getTextStringModel(), "Full text", 0, StringValue.class));

        closeCurrentGroup();
        createNewGroup("Source and Category");
        m_docSourceModel = getDocSourceModel();
        addDialogComponent(new DialogComponentString(m_docSourceModel, "Document source"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        m_useSourceColumnModel = getUseSourceColumnModel();
        m_docSourceModelCombo = getSourceColumnModel();
        m_useSourceColumnModel.addChangeListener(e -> stateChanged());
        addDialogComponent(new DialogComponentBoolean(m_useSourceColumnModel, "Use sources from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_docSourceModelCombo, "Document source column", 0,
            StringValue.class));
        setHorizontalPlacement(false);

        m_docCategoryModel = getDocCategoryModel();
        addDialogComponent(new DialogComponentString(m_docCategoryModel, "Document category"));
        setHorizontalPlacement(true);
        m_useCatColumnModel = getUseCategoryColumnModel();
        m_docCatModelCombo = getCategoryColumnModel();
        m_useCatColumnModel.addChangeListener(e -> stateChanged());
        addDialogComponent(new DialogComponentBoolean(m_useCatColumnModel, "Use categories from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_docCatModelCombo, "Document category column", 0,
            StringValue.class));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Type and Date");
        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(getTypeModel(), "Document type", types));
        // Pub Date
        m_pubDateModel = getPubDateModel();
        m_pubDateModelCombo = getPubDateColumnModel();
        m_usePubDateColumnModel = getUsePubDateColumnModel();
        DialogComponentDateTimeSelection dcs = new DialogComponentDateTimeSelection(m_pubDateModel,
            null, DisplayOption.SHOW_DATE_ONLY);
        addDialogComponent(dcs);
        setHorizontalPlacement(true);
        m_usePubDateColumnModel.addChangeListener(e -> stateChanged());
        addDialogComponent(new DialogComponentBoolean(m_usePubDateColumnModel, "Use publication date from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_pubDateModelCombo, "Publication date column", 0,
            false, LocalDateValue.class));
        closeCurrentGroup();


        createNewGroup("Column");
        m_docColModel = getDocumentColumnModel();
        DialogComponentString docComp = new DialogComponentString(m_docColModel, "Document column:");
        docComp.setToolTipText("Name of the new document column");
        addDialogComponent(docComp);
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Processes");
        addDialogComponent(
            new DialogComponentNumber(getNumberOfThreadsModel(), "Number of maximal parallel processes", 1));
        closeCurrentGroup();

        createNewGroup("Tokenization");
        Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().entrySet().stream()
            .map(e -> e.getKey()).collect(Collectors.toList());
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));
        closeCurrentGroup();
    }

    private void stateChanged() {
        m_docCategoryModel.setEnabled(!m_useCatColumnModel.getBooleanValue());
        m_docSourceModel.setEnabled(!m_useSourceColumnModel.getBooleanValue());
        m_docSourceModelCombo.setEnabled(m_useSourceColumnModel.getBooleanValue());
        m_docCatModelCombo.setEnabled(m_useCatColumnModel.getBooleanValue());
        m_docTitleModelCombo.setEnabled(m_useTitleColumnModel.getBooleanValue());
        m_docAuthorModelCombo.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
        m_pubDateModel.setEnabled(!m_usePubDateColumnModel.getBooleanValue());
        m_pubDateModelCombo.setEnabled(m_usePubDateColumnModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        m_spec = (DataTableSpec)specs[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        if (m_docColModel.getStringValue() != null && m_spec.containsName(m_docColModel.getStringValue().trim())) {
            throw new InvalidSettingsException("Can't create new column \"" + m_docColModel.getStringValue()
            + "\" as input spec already contains such column!");
        }
        if (m_docColModel.getStringValue() == null || m_docColModel.getStringValue().trim().isEmpty()) {
            throw new InvalidSettingsException("Can't create new column! Column name can't be empty!");
        }
        super.saveAdditionalSettingsTo(settings);
    }

}

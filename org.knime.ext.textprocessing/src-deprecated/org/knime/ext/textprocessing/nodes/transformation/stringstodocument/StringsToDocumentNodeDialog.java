/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.data.time.duration.DurationValue;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.data.time.localtime.LocalTimeValue;
import org.knime.core.data.time.period.PeriodValue;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * Provides the dialog for the String to Document node with all necessary dialog components.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 * @deprecated
 */
@Deprecated
public class StringsToDocumentNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the column which has to be used as
     *         authors column.
     */
    static final SettingsModelString getAuthorsStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_AUTHORSCOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the separator string.
     */
    static final SettingsModelString getAuthorSplitStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_AUTHORSPLIT_STR,
            StringsToDocumentConfig.DEF_AUTHORS_SPLITCHAR);
    }

    static final SettingsModelString getAuthorFirstNameModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_AUTHOR_FIRST_NAME,
            StringsToDocumentConfig.DEF_AUTHOR_NAMES);

    }

    static final SettingsModelString getAuthorLastNameModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_AUTHOR_LAST_NAME,
            StringsToDocumentConfig.DEF_AUTHOR_NAMES);

    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the column which has to be used as
     *         title column.
     */
    static final SettingsModelString getTitleStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_TITLECOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the column which has to be used as
     *         full text column.
     */
    static final SettingsModelString getTextStringModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_TEXTCOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the document source.
     */
    static final SettingsModelString getDocSourceModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_DOCSOURCE,
            StringsToDocumentConfig.DEF_DOCUMENT_SOURCE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the document category.
     */
    static final SettingsModelString getDocCategoryModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_DOCCAT,
            StringsToDocumentConfig.DEF_DOCUMENT_CATEGORY);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the document type.
     */
    static final SettingsModelString getTypeModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_DOCTYPE,
            StringsToDocumentConfig.DEF_DOCUMENT_TYPE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the document publication date.
     */
    static final SettingsModelString getPubDatModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_PUBDATE,
            StringsToDocumentConfig.DEF_DOCUMENT_PUBDATE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} specifying whether a column is used for
     *         category values or not.
     */
    static final SettingsModelBoolean getUseCategoryColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys.CFGKEY_USE_CATCOLUMN,
            StringsToDocumentConfig.DEF_USE_CATCOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} specifying whether a column is used for source
     *         values or not.
     */
    static final SettingsModelBoolean getUseSourceColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys.CFGKEY_USE_SOURCECOLUMN,
            StringsToDocumentConfig.DEF_USE_SOURCECOLUMN);
    }

    /**
     *
     * @return Creates and return an instance of{@SettingModelBoolean} specifying whether a column is used for title
     *         values or not
     */
    static final SettingsModelBoolean getUseTitleColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys.CFGKEY_USE_TITLECOLUMN,
            StringsToDocumentConfig.DEF_USE_TITLECOLUMN);
    }

    /**
     *
     * @return Creates and return an instance of{@SettingModelBoolean} specifying whether a column is used for authors
     *         values or not
     */
    static final SettingsModelBoolean getUseAuthorsColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys.CFGKEY_USE_AUTHORSCOLUMN,
            StringsToDocumentConfig.DEF_USE_AUTHORSCOLUMN);
    }

    /**
     *
     * @return Creates and return an instance of{@SettingModelBoolean} specifying whether a column is used for
     *         publication dates or not
     */
    static final SettingsModelBoolean getUsePubDateColumnModel() {
        return new SettingsModelBoolean(StringsToDocumentConfigKeys.CFGKEY_USE_PUBDATECOLUMN,
            StringsToDocumentConfig.DEF_USE_PUBDATECOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the column with the category values.
     */
    static final SettingsModelString getCategoryColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_CATCOLUMN, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} specifying the column with the source values.
     */
    static final SettingsModelString getSourceColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_SOURCECOLUMN, "");
    }

    /**
     *
     * @return Creates and returns an instance of{@SettingsModellString} specifying the column which has to be used as
     *         publication date
     */
    static final SettingsModelString getPubDateColumnModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_PUBDATECOL, "");

    }

    /**
     * Creates and returns the settings model, storing the number of maximal parallel threads running.
     *
     * @return The settings model with number of maximal parallel threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(StringsToDocumentConfigKeys.CFGKEY_THREADS,
            StringsToDocumentNodeModel.DEF_THREADS, StringsToDocumentNodeModel.MIN_THREADS,
            StringsToDocumentNodeModel.MAX_THREADS);
    }

    /**
     * Creates and returns the settings model, storing the name of the tokenizer used for word tokenization.
     *
     * @return The settings model with name of word tokenizer.
     */
    static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(StringsToDocumentConfigKeys.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    private SettingsModelString m_docCategoryModel;

    private SettingsModelString m_pubDateModel;

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

    /**
     * Creates a new instance of {@StringsToDocumentNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public StringsToDocumentNodeDialog() {

        createNewGroup("Text");
        setHorizontalPlacement(true);
        m_useTitleColumnModel = getUseTitleColumnModel();
        m_docTitleModelCombo = getTitleStringModel();
        m_useTitleColumnModel.addChangeListener(new UsageChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_useTitleColumnModel, "Use title from column"));
        addDialogComponent(
            new DialogComponentColumnNameSelection(m_docTitleModelCombo, "Title column", 0, StringValue.class));

        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        m_useAuthorsColumnModel = getUseAuthorsColumnModel();
        m_docAuthorModelCombo = getAuthorsStringModel();
        m_useAuthorsColumnModel.addChangeListener(new UsageChangeListener());
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
        m_useSourceColumnModel.addChangeListener(new UsageChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_useSourceColumnModel, "Use sources from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_docSourceModelCombo, "Document source column", 0,
            StringValue.class));
        setHorizontalPlacement(false);

        m_docCategoryModel = getDocCategoryModel();
        addDialogComponent(new DialogComponentString(m_docCategoryModel, "Document category"));
        setHorizontalPlacement(true);
        m_useCatColumnModel = getUseCategoryColumnModel();
        m_docCatModelCombo = getCategoryColumnModel();
        m_useCatColumnModel.addChangeListener(new UsageChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_useCatColumnModel, "Use categories from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_docCatModelCombo, "Document category column", 0,
            StringValue.class));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Type and Date");
        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(getTypeModel(), "Document type", types));
        m_pubDateModel = getPubDatModel();
        DialogComponentString dcs = new DialogComponentString(m_pubDateModel, "Publication date (dd-mm-yyyy)");
        dcs.setToolTipText("Date has to be specified like \"dd-mm-yyyy!\"");
        addDialogComponent(dcs);
        // Pub Date
        setHorizontalPlacement(true);
        m_usePubDateColumnModel = getUsePubDateColumnModel();
        m_pubDateModelCombo = getPubDateColumnModel();
        m_usePubDateColumnModel.addChangeListener(new UsageChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_usePubDateColumnModel, "Use publication date from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_pubDateModelCombo, "Publication date column", 0,
            new StringAndLocalDateValueColumnFilter()));
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

    /**
     * Enables and disables text fields of document source and category.
     *
     * @author Kilian Thiel, KNIME.com, Berlin, Germany
     */
    class UsageChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_docCategoryModel.setEnabled(!m_useCatColumnModel.getBooleanValue());
            m_docSourceModel.setEnabled(!m_useSourceColumnModel.getBooleanValue());
            m_docSourceModelCombo.setEnabled(m_useSourceColumnModel.getBooleanValue());
            m_docCatModelCombo.setEnabled(m_useCatColumnModel.getBooleanValue());
            m_docTitleModelCombo.setEnabled(m_useTitleColumnModel.getBooleanValue());
            m_docAuthorModelCombo.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
            m_pubDateModel.setEnabled(!m_usePubDateColumnModel.getBooleanValue());
            m_pubDateModelCombo.setEnabled(m_usePubDateColumnModel.getBooleanValue());

        }
    }

    /**
     * This {@link ColumnFilter} is used for the {@link DialogComponentColumnNameSelection} to select columns that
     * contain date information. It includes columns that are compatible to the new {@link LocalDateValue} and
     * {@link StringValue} (for backwards compatibility). Since {@link Document}s can handle dates but no time, other
     * date & time types like {@link LocalDateTimeValue} are excluded by this ColumnFilter implementation to prevent
     * unwanted erasure of additional time information as decided in AP-7461. To use columns with other date & time
     * types the user has to convert them to date columns.
     *
     * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
     */
    private class StringAndLocalDateValueColumnFilter implements ColumnFilter {

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("deprecation")
        @Override
        public boolean includeColumn(final DataColumnSpec colSpec) {
            if (colSpec == null) {
                throw new NullPointerException("Column specification must not be null");
            }
            if (colSpec.getType().isCompatible(LocalDateValue.class)) {
                return true;
            } else if (colSpec.getType().isCompatible(DateAndTimeValue.class)) {
                return true;
            } else if (colSpec.getType().isCompatible(StringValue.class)
                && !(colSpec.getType().isCompatible(LocalDateTimeValue.class)
                    || colSpec.getType().isCompatible(ZonedDateTimeValue.class)
                    || colSpec.getType().isCompatible(LocalTimeValue.class)
                    || colSpec.getType().isCompatible(DurationValue.class)
                    || colSpec.getType().isCompatible(PeriodValue.class))) {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String allFilteredMsg() {
            return "No column in spec is compatible to \"" + StringValue.class.getSimpleName() + "\" or \""
                + LocalDateValue.class.getSimpleName() + "\".";
        }

    }

}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   04.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.util.Collection;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
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
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class RSSFeedReaderNodeDialog extends DefaultNodeSettingsPane {
    /**
     * @return Returns the SettingsModelString containing the name of the url column.
     */
    static final SettingsModelString createFeedUrlColumnModel() {
        return new SettingsModelString(RSSFeedReaderConfigKeys.CFGKEY_FEED_URL_COLUMN, "");
    }

    /**
     * @return Returns the SettingsModelBoolean containing the boolean value for creating a document column for feed
     *         entries.
     */
    static final SettingsModelBoolean createDocumentColumnModel() {
        return new SettingsModelBoolean(RSSFeedReaderConfigKeys.CFGKEY_CREATE_DOC_COLUMN,
            RSSFeedReaderNodeModel2.DEF_CREATE_DOC_COLUMN);
    }

    /**
     * @return Returns the SettingsModelString containing the name for the document column.
     */
    static final SettingsModelString createDocColumnNameModel() {
        return new SettingsModelString(RSSFeedReaderConfigKeys.CFGKEY_DOC_COL_NAME,
            RSSFeedReaderNodeModel2.DEF_DOC_COL_NAME);
    }

    /**
     * @return Returns the SettingsModelBoolean containing the boolean value for creating an XML column for feed
     *         entries.
     */
    static final SettingsModelBoolean createXMLColumnModel() {
        return new SettingsModelBoolean(RSSFeedReaderConfigKeys.CFGKEY_CREATE_XML_COLUMN,
            RSSFeedReaderNodeModel2.DEF_CREATE_XML_COLUMN);
    }

    /**
     * @return Returns the SettingsModelString containing the name for the XML column.
     */
    static final SettingsModelString createXmlColumnNameModel() {
        return new SettingsModelString(RSSFeedReaderConfigKeys.CFGKEY_XML_COL_NAME,
            RSSFeedReaderNodeModel2.DEF_XML_COL_NAME);
    }

    /**
     * @return Returns the SettingsModelIntegerBounded for the number of used threads.
     */
    static final SettingsModelIntegerBounded createNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(RSSFeedReaderConfigKeys.CFGKEY_NUMBER_OF_THREADS,
            RSSFeedReaderNodeModel2.DEF_THREADS, RSSFeedReaderNodeModel2.MIN_THREADS, RSSFeedReaderNodeModel2.MAX_THREADS);
    }

    /**
     * @return Returns the SettingsModelIntegerBounded for the time until the connection times out (in ms).
     */
    static final SettingsModelIntegerBounded createTimeOutModel() {
        return new SettingsModelIntegerBounded(RSSFeedReaderConfigKeys.CFGKEY_TIMEOUT,
            RSSFeedReaderNodeModel2.DEF_TIMEOUT, RSSFeedReaderNodeModel2.MIN_TIMEOUT, RSSFeedReaderNodeModel2.MAX_TIMEOUT);
    }

    /**
     * @return Returns the SettingsModelBoolean containing the boolean value for creating an HTTP column for feed entries.
     */
    static final SettingsModelBoolean getHttpResponseCodeModel() {
        return new SettingsModelBoolean(RSSFeedReaderConfigKeys.CFGKEY_GET_HTTP_RESPONSE_CODE_COLUMN,
            RSSFeedReaderNodeModel2.DEF_GET_HTTP_RESPONSE_CODE_COLUMN);
    }

    /**
     * @return Returns the SettingsModelString containing the name of the HTTP column.
     */
    static final SettingsModelString createHttpColumnNameModel() {
        return new SettingsModelString(RSSFeedReaderConfigKeys.CFGKEY_HTTP_COL_NAME,
            RSSFeedReaderNodeModel2.DEF_HTTP_COL_NAME);
    }

    static SettingsModelString getTokenizerModel() {
        return new SettingsModelString(RSSFeedReaderConfigKeys.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    private final SettingsModelBoolean m_createDocCol = createDocumentColumnModel();

    private final SettingsModelBoolean m_createXmlCol = createXMLColumnModel();

    private final SettingsModelBoolean m_createHttpCol = getHttpResponseCodeModel();

    private final SettingsModelString m_docColName = createDocColumnNameModel();

    private final SettingsModelString m_xmlColName = createXmlColumnNameModel();

    private final SettingsModelString m_httpColName = createHttpColumnNameModel();

    private final SettingsModelString m_tokenizerName = getTokenizerModel();

    @SuppressWarnings("unchecked")
    RSSFeedReaderNodeDialog() {
        // component for the url column selection
        SettingsModelString feedUrlColumn = createFeedUrlColumnModel();
        DialogComponentColumnNameSelection feedUrlColComp =
            new DialogComponentColumnNameSelection(feedUrlColumn, "URL column", 0, StringValue.class);
        feedUrlColComp.setToolTipText("The RSS feed urls.");
        addDialogComponent(feedUrlColComp);

        // components for number of threads and timeout settings
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumber(createNumberOfThreadsModel(), "Number of threads", 1));
        addDialogComponent(new DialogComponentNumber(createTimeOutModel(), "Timeout (in milliseconds)", 100));
        setHorizontalPlacement(false);

        // components for additional Document and/or XML columns
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_createDocCol, "Create Document column"));
        m_createDocCol.addChangeListener(e -> toggleColumnNameFields());
        DialogComponentString docColNameComp = new DialogComponentString(m_docColName, "", true, 20);
        docColNameComp.setToolTipText("Name of the Document column");
        addDialogComponent(docColNameComp);
        setHorizontalPlacement(false);

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_createXmlCol, "Create XML column"));
        m_createXmlCol.addChangeListener(e -> toggleColumnNameFields());
        DialogComponentString xmlColNameComp = new DialogComponentString(m_xmlColName, "", true, 20);
        xmlColNameComp.setToolTipText("Name of the XML column");
        addDialogComponent(xmlColNameComp);
        setHorizontalPlacement(false);

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_createHttpCol, "Create HTTP status code column"));
        m_createHttpCol.addChangeListener(e -> toggleColumnNameFields());
        DialogComponentString httpColNameComp = new DialogComponentString(m_httpColName, "", true, 20);
        httpColNameComp.setToolTipText("Name of the HTTP column");
        addDialogComponent(httpColNameComp);

        setHorizontalPlacement(false);

        Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().keySet();
        addDialogComponent(new DialogComponentStringSelection(m_tokenizerName, "Word tokenizer", tokenizerList));
    }

    void toggleColumnNameFields(){
        m_docColName.setEnabled(m_createDocCol.getBooleanValue());
        m_xmlColName.setEnabled(m_createXmlCol.getBooleanValue());
        m_httpColName.setEnabled(m_createHttpCol.getBooleanValue());
        m_tokenizerName.setEnabled(m_createDocCol.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        super.loadSettingsFrom(settings, specs);
        toggleColumnNameFields();
    }
}

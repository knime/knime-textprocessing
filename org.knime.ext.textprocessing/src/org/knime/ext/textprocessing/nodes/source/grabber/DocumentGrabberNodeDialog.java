/*
========================================================================
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
 *   20.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactory;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentGrabberNodeDialog extends DefaultNodeSettingsPane {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentGrabberNodeDialog.class);

    /**
     * @return Creates and returns the settings model of the query.
     */
    public static final SettingsModelString getQueryModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_QUERY, "");
    }

    /**
     * @return Creates and returns the settings model of the maximum results.
     */
    public static final SettingsModelIntegerBounded getMaxResultsModel() {
        return new SettingsModelIntegerBounded(DocumentGrabberConfigKeys.CFGKEY_MAX_RESULTS,
            DocumentGrabberNodeModel.DEF_RESULTS, DocumentGrabberNodeModel.MIN_RESULTS,
            DocumentGrabberNodeModel.MAX_RESULTS);
    }

    /**
     * @return Creates and returns the settings model of the data base to use.
     */
    public static final SettingsModelString getDataBaseModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_DATATBASE,
            DocumentGrabberNodeModel.DEFAULT_DATABASE);
    }

    /**
     * @return Creates and returns the settings model of the deleted files flag.
     */
    public static final SettingsModelBoolean getDeleteFilesModel() {
        return new SettingsModelBoolean(DocumentGrabberConfigKeys.CFGKEY_DELETE,
            DocumentGrabberNodeModel.DEF_DELETE_AFTER_PARSE);
    }

    /**
     * @return Creates and returns the settings model of the directory to save the files to.
     */
    public static final SettingsModelString getDirectoryModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_DIR, DocumentGrabberNodeModel.DEF_DIR);
    }

    /**
     * @return Creates and returns the settings model of the document category to set.
     */
    public static final SettingsModelString getDocumentCategoryModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_DOC_CAT, "");
    }

    /**
     * @return Creates and returns the settings model of the document type to set.
     */
    public static final SettingsModelString getDocumentTypeModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_DOC_TYPE, "");
    }

    /**
     * @return Creates and returns the settings model of the extract meta info option.
     * @since 2.7
     */
    public static final SettingsModelBoolean getExtractMetaInfoModel() {
        return new SettingsModelBoolean(DocumentGrabberConfigKeys.CFGKEY_EXTRACT_META_INFO, false);
    }

    /**
     * @return Creates and returns the settings model of the append query column setting.
     * @since 2.8
     */
    public static final SettingsModelBoolean getAppendQueryColumnModel() {
        return new SettingsModelBoolean(DocumentGrabberConfigKeys.CFGKEY_APPEND_QUERYCOLUM, false);
    }

    /**
     * @return Creates and returns the settings model for the tokenizer used for word tokenization.
     * @since 3.3
     */
    public static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(DocumentGrabberConfigKeys.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    private DialogComponentButtonLabel m_buttonLabel;

    private SettingsModelString m_queryModel;

    private SettingsModelString m_databaseModel;

    private SettingsModelInteger m_maxResultsModel;

    private SettingsModelString m_directoryModel;

    /**
     * Creates new instance of <code>DocumentGrabberNodeDialog</code>.
     */
    public DocumentGrabberNodeDialog() {

        createNewGroup("Query and databse settings");

        m_queryModel = getQueryModel();
        addDialogComponent(new DialogComponentMultiLineString(m_queryModel, "Query: "));

        m_buttonLabel = new DialogComponentButtonLabel("Number of results ", " 0 ");
        m_buttonLabel.addActionListener(new ButtonListener());
        addDialogComponent(m_buttonLabel);

        setHorizontalPlacement(true);

        m_maxResultsModel = getMaxResultsModel();
        addDialogComponent(new DialogComponentNumber(m_maxResultsModel, "Maximal results: ", 100));

        addDialogComponent(new DialogComponentBoolean(getAppendQueryColumnModel(), "Append query column"));

        setHorizontalPlacement(false);

        setHorizontalPlacement(true);

        List<String> namesAsList = new ArrayList<String>(DocumentGrabberFactory.getInstance().getGrabberNames());
        m_databaseModel = getDataBaseModel();
        addDialogComponent(new DialogComponentStringSelection(m_databaseModel, "Database: ", namesAsList));

        addDialogComponent(
            new DialogComponentBoolean(getExtractMetaInfoModel(), "Extract meta information if provided by database"));

        setHorizontalPlacement(false);

        closeCurrentGroup();

        createNewGroup("Path and document settings");

        m_directoryModel = getDirectoryModel();
        addDialogComponent(new DialogComponentFileChooser(m_directoryModel, "DocumentsDirectory - save",
            JFileChooser.SAVE_DIALOG, true));

        addDialogComponent(new DialogComponentBoolean(getDeleteFilesModel(), "Delete after parsing"));

        addDialogComponent(new DialogComponentString(getDocumentCategoryModel(), "Document Category:"));

        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(getDocumentTypeModel(), "Document Type:", types));

        Set<String> tokenizerList = new TreeSet<String>();
        for (ImmutableMap.Entry<String, TokenizerFactory> entry : TokenizerFactoryRegistry.getTokenizerFactoryMap()
            .entrySet()) {
            tokenizerList.add(entry.getKey());
        }
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));

        closeCurrentGroup();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);

        String dir = m_directoryModel.getStringValue();
        File f = new File(dir);
        if (!f.isDirectory()) {
            throw new InvalidSettingsException("Selected directory: " + dir + " is not a directory!");
        } else if (!f.exists()) {
            throw new InvalidSettingsException("Selected directory: " + dir + " does not exist!");
        } else if (!f.canWrite()) {
            throw new InvalidSettingsException("Selected directory: " + dir + " is not writable!");
        } else if (f.listFiles().length > 0) {
            throw new InvalidSettingsException("Selected directory: " + dir + " is not empty!");
        }

    }

    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    class ButtonListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            String database = m_databaseModel.getStringValue();
            String query = m_queryModel.getStringValue();

            DocumentGrabber grabber = DocumentGrabberFactory.getInstance().getGrabber(database);
            int numberOfResults;
            try {
                numberOfResults = grabber.numberOfResults(new Query(query, m_maxResultsModel.getIntValue()));
                m_buttonLabel.setLabelText(Integer.toString(numberOfResults));
            } catch (Exception e1) {
                LOGGER.warn("Could not send query !");
            }
        }
    }
}

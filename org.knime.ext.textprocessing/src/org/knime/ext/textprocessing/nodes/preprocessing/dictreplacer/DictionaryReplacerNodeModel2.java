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
 *   12.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamableFunctionPreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The {@code NodeModel} for the Dict Replacer node.
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class DictionaryReplacerNodeModel2 extends StreamableFunctionPreprocessingNodeModel {

    /** Node logger. */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(DictionaryReplacerNodeModel2.class);

    /** The default dictionary file path. */
    public static final String DEF_DICTFILE = System.getProperty("user.home");

    /** The default valid dictionary file extensions (txt). */
    public static final String[] VALID_DICTFILE_EXTENIONS = new String[]{"txt"};

    /** The default separator. */
    public static final String DEFAULT_SEPARATOR = ",";

    /** {@link SettingsModelString} storing the path of the file containing the dictionary. */
    private final SettingsModelString m_fileModel = DictionaryReplacerNodeDialog2.getDictionaryFileModel();

    /** {@link SettingsModelString} storing the name of the word tokenizer. */
    private final SettingsModelString m_tokenizerModel = DictionaryReplacerNodeDialog2.getTokenizerModel();

    /** {@link SettingsModelOptionalString} storing a String used to replace words not available in the dictionary. */
    private final SettingsModelOptionalString m_replaceUnknownWordsModel =
        DictionaryReplacerNodeDialog2.getReplaceUnknownWordsModel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {
        final Map<String, String> dictionary = new HashMap<>();
        final File f = new File(m_fileModel.getStringValue());
        if (f.exists() && f.canRead() && f.isFile()) {
            try (final BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String[] keyVal = line.trim().split(DEFAULT_SEPARATOR);
                    if (keyVal.length == 2) {
                        dictionary.put(keyVal[0], keyVal[1]);
                    }
                }
            } catch (final FileNotFoundException e) {
                LOGGER.warn("Not such file !");
            } catch (final IOException e) {
                LOGGER.warn("Cant read from file");
            }
        }
        if (!m_replaceUnknownWordsModel.isActive()) {
            return new DictionaryReplacer(dictionary, m_tokenizerModel.getStringValue());
        }
        return new DictionaryReplacer(dictionary, m_replaceUnknownWordsModel.getStringValue(),
            m_tokenizerModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(inSpecs[0]);
        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizerModel.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizerModel.getStringValue());
        }
        if (!dataTableSpecVerifier.verifyTokenizer(m_tokenizerModel.getStringValue())) {
            setWarningMessage(dataTableSpecVerifier.getTokenizerWarningMsg());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
        // only load if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.loadSettingsFrom(settings);
        }
        if (settings.containsKey(m_replaceUnknownWordsModel.getKey())) {
            m_replaceUnknownWordsModel.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_tokenizerModel.saveSettingsTo(settings);
        m_replaceUnknownWordsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        // only validate if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.validateSettings(settings);
        }
        if (settings.containsKey(m_replaceUnknownWordsModel.getKey())) {
            m_replaceUnknownWordsModel.validateSettings(settings);
        }
    }
}

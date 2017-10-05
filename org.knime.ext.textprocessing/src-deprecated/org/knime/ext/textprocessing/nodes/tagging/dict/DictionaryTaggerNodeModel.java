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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeModel;


/**
 *
 * @author thiel, University of Konstanz
 * @deprecated Use custom node model instead.
 */
@Deprecated
public class DictionaryTaggerNodeModel extends TaggerNodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default value of the case sensitive setting.
     */
    public static final boolean DEFAULT_CASE_SENSITIVE = true;

    /**
     * The default value of the default tag.
     */
    public static final String DEFAULT_TAG =
        NamedEntityTag.UNKNOWN.getTag().getTagValue();

    /**
     * The default tag type.
     */
    public static final String DEFAULT_TAG_TYPE = "NE";


    private SettingsModelBoolean m_setUnmodifiableModel =
        DictionaryTaggerNodeDialog.createSetUnmodifiableModel();

    private SettingsModelString m_tagModel =
        DictionaryTaggerNodeDialog.createTagModel();

    private SettingsModelString m_tagTypeModel =
        DictionaryTaggerNodeDialog.createTagTypeModel();

    private SettingsModelString m_fileModel =
        DictionaryTaggerNodeDialog.createFileModel();

    private SettingsModelBoolean m_caseSensitiveModel =
        DictionaryTaggerNodeDialog.createCaseSensitiveModel();



    /**
     * Creates a new instance of <code>DictionaryTaggerNodeModel</code> with one
     * table in and one out port.
     */
    public DictionaryTaggerNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     * @since 2.9
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        // Read file with named entities
        final Set<String> namedEntities = new LinkedHashSet<String>();
        final File file = new File(m_fileModel.getStringValue());
        if (file.exists() && file.canRead() && file.isFile()) {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                namedEntities.add(line.trim());
            }
            br.close();
        } else {
            throw new InvalidSettingsException("Specified dictionary file does not exist!");
        }

        final String tagTypeStr = m_tagTypeModel.getStringValue();
        final String tagStr = m_tagModel.getStringValue();
        final Tag tag = TagFactory.getInstance().getTagSetByType(tagTypeStr).buildTag(tagStr);

        return new DictionaryDocumentTagger(m_setUnmodifiableModel.getBooleanValue(), namedEntities, tag,
            m_caseSensitiveModel.getBooleanValue(), getTokenizerName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_caseSensitiveModel.loadSettingsFrom(settings);
        m_tagModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
        m_setUnmodifiableModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_caseSensitiveModel.saveSettingsTo(settings);
        m_tagModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_caseSensitiveModel.validateSettings(settings);
        m_tagModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);

        // check selected file
        String file = ((SettingsModelString)m_fileModel.
                createCloneWithValidatedValue(settings)).getStringValue();
        File f = new File(file);
        if (!f.isFile() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected file: "
                    + file + " is not valid!");
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
}

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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.inport;

import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel;

/**
 *
 * @author thiel, University of Konstanz
 * @deprecated Use custom node model instead.
 */
@Deprecated
public class DictionaryTaggerNodeModel extends AbstractDictionaryTaggerModel {

    /**
     * The default value of the exact match setting.
     *
     * @since 2.8
     */
    public static final boolean DEFAULT_EXACTMATCH = true;

    private SettingsModelBoolean m_exactMatchModel = DictionaryTaggerNodeDialog.createExactMatchModel();

    /**
     * Creates a new instance of <code>DictionaryTaggerNodeModel</code> with two table in ports and one out port.
     */
    public DictionaryTaggerNodeModel() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#createDocumentTagger(java.util.Set)
     */
    @Override
    protected DocumentTagger createDocumentTagger(final Set<String> dictionary) {
        return new DictionaryDocumentTagger(getUnmodifiableSetting(), dictionary, getTagSetting(),
            getCaseSensitiveSetting(), m_exactMatchModel.getBooleanValue(), getTokenizerName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#loadValidatedSettingsFromInternal
     * (org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFromInternal(final NodeSettingsRO settings) throws InvalidSettingsException {
        // added in 2.7.4, only load settings if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_exactMatchModel.getConfigName())) {
            m_exactMatchModel.loadSettingsFrom(settings);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#saveSettingsToInternal(org.knime
     * .core.node.NodeSettingsWO)
     */
    @Override
    protected void saveSettingsToInternal(final NodeSettingsWO settings) {
        m_exactMatchModel.saveSettingsTo(settings);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#validateSettingsInternal(org.knime
     * .core.node.NodeSettingsRO)
     */
    @Override
    protected void validateSettingsInternal(final NodeSettingsRO settings) throws InvalidSettingsException {
        // added in 2.7.4, only validate settings if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_exactMatchModel.getConfigName())) {
            m_exactMatchModel.validateSettings(settings);
        }
    }
}

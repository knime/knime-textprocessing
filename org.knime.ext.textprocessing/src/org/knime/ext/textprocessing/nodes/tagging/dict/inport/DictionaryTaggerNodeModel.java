/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
 */
public class DictionaryTaggerNodeModel extends AbstractDictionaryTaggerModel {

    /**
     * The default value of the exact match setting.
     * @since 2.8
     */
    public static final boolean DEFAULT_EXACTMATCH = true;


    private SettingsModelBoolean m_exactMatchModel =
            DictionaryTaggerNodeDialog.createExactMatchModel();

    /**
     * Creates a new instance of <code>DictionaryTaggerNodeModel</code> with two
     * table in ports and one out port.
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
                                            getCaseSensitiveSetting(), m_exactMatchModel.getBooleanValue());
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
        try {
            // added in 2.7.4
            m_exactMatchModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException ise) {
            m_exactMatchModel.setBooleanValue(DEFAULT_EXACTMATCH);
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
        // added in 2.7.4
        // m_exactMatchModel.validateSettings(settings);
    }
}

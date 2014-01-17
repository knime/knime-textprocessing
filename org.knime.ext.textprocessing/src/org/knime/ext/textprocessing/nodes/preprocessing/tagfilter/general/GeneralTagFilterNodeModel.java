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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.general;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilter;

/**
 * The node model of the general tag filter.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class GeneralTagFilterNodeModel extends PreprocessingNodeModel {

    /**
     * The default value of the "strict filtering" setting.
     */
    public static final boolean DEF_STRICT = false;

    /**
     * The default value of the "filter matching" setting.
     */
    public static final boolean DEF_FILTER_MATCHING = false;

    private final SettingsModelBoolean m_filterMatchingModel =
            GeneralTagFilterNodeDialog.getFilterMatchingModel();

    private final SettingsModelBoolean m_strictFilteringModel =
            GeneralTagFilterNodeDialog.getStrictFilteringModel();

    private final SettingsModelString m_tagTypeModel =
            GeneralTagFilterNodeDialog.getTagTypeModel();

    private final SettingsModelStringArray m_validTagsModel =
            GeneralTagFilterNodeDialog.getValidTagsModel();


    /**
     * @return A proper instance of <code>TagBuilder</code> that provides the
     * set of valid tags.
     */
    protected TagBuilder getTagBuilder() {
        final String selectedTagType = m_tagTypeModel.getStringValue();
        return TagFactory.getInstance().getTagSetByType(selectedTagType);
    }

    /**
     * @return The type of the valid tags.
     */
    protected String getValidTagType() {
        return getTagBuilder().getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void initPreprocessing() {
        final TagBuilder tb = getTagBuilder();
        final Set<Tag> validTags = new HashSet<Tag>(
                m_validTagsModel.getStringArrayValue().length);

        for (final String s : m_validTagsModel.getStringArrayValue()) {
            final Tag t = tb.buildTag(s);
            if (t != null) {
                validTags.add(t);
            }
        }

        m_preprocessing = new TagFilter(validTags, getValidTagType(),
                m_strictFilteringModel.getBooleanValue(),
                m_filterMatchingModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadValidatedSettingsFrom(
            final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_strictFilteringModel.loadSettingsFrom(settings);
        m_validTagsModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        try {
            m_filterMatchingModel.loadSettingsFrom(settings);
        } catch (final InvalidSettingsException e) {
            // this is a older version set it to the old behavior
            m_filterMatchingModel.setBooleanValue(DEF_FILTER_MATCHING);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_strictFilteringModel.saveSettingsTo(settings);
        m_validTagsModel.saveSettingsTo(settings);
        m_filterMatchingModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_strictFilteringModel.validateSettings(settings);
        m_validTagsModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void reset() {
        // Nothing to reset ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

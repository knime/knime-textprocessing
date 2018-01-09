/*
 * ------------------------------------------------------------------------
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
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter2.TagFilter;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter2.TagFilterNodeModel2;

/**
 * The node model of the general tag filter.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 * @deprecated use {@link TagFilterNodeModel2} instead.
 */
@Deprecated
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

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

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
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;

/**
 * The abstract class <code>TagFilterNodeModel</code> provides a functionality
 * to filter out terms with tags assigned that are not specified as valid.
 * Underlying implementations have to implement
 * {@link TagFilterNodeModel#getTagBuilder()} and
 * {@link TagFilterNodeModel#getValidTagType()}, to specify the valid type
 * of the tags to consider during the filtering process as well as a
 * {@link org.knime.ext.textprocessing.data.TagBuilder} that provides the set
 * of valid tags.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class TagFilterNodeModel extends PreprocessingNodeModel {

    /**
     * The default value of the "strict filtering" setting.
     */
    public static final boolean DEF_STRICT = false;


    private SettingsModelBoolean m_strictFilteringModel =
        TagFilterNodeDialog.getStrictFilteringModel();

    private SettingsModelStringArray m_validTagsModel =
        TagFilterNodeDialog.getValidTagsModel();


    /**
     * @return A proper instance of <code>TagBuilder</code> that provides the
     * set of valid tags.
     */
    protected abstract TagBuilder getTagBuilder();

    /**
     * @return The type of the valid tags.
     */
    protected abstract String getValidTagType();

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void initPreprocessing() {
        TagBuilder tb = getTagBuilder();
        Set<Tag> validTags = new HashSet<Tag>(
                m_validTagsModel.getStringArrayValue().length);

        for (String s : m_validTagsModel.getStringArrayValue()) {
            Tag t = tb.buildTag(getValidTagType(), s);
            if (t != null) {
                validTags.add(t);
            }
        }

        m_preprocessing = new TagFilter(validTags, getValidTagType(),
                m_strictFilteringModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_strictFilteringModel.loadSettingsFrom(settings);
        m_validTagsModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_strictFilteringModel.saveSettingsTo(settings);
        m_validTagsModel.saveSettingsTo(settings);
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

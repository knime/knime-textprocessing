/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.pos;

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
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class TagFilterNodeModel extends PreprocessingNodeModel {

    public static final boolean DEF_STRICT = false;
    
    
    private SettingsModelBoolean m_strictFilteringModel = 
        TagFilterNodeDialog.getStrictFilteringModel();
    
    private SettingsModelStringArray m_validTagsModel = 
        TagFilterNodeDialog.getValidTagsModel();
    
    
    protected abstract TagBuilder getTagBuilder();
    
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

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
 *   01.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * A {@link org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane}
 * which provides additionally a tab that contains a checkbox to specify
 * if deep preprocessing have to be applied or not.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PreprocessingNodeSettingsPane extends DefaultNodeSettingsPane {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(PreprocessingNodeSettingsPane.class);  
    
    private DeepPreprocessingDialogTab m_deepPreproTab = 
        new DeepPreprocessingDialogTab();
    
    /**
     * Creates new instance of <code>PreprocessingNodeSettingsPane</code>.
     */
    public PreprocessingNodeSettingsPane() {
        addTab("Deep Preprocessing", m_deepPreproTab);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
        m_deepPreproTab.saveSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        try {
            m_deepPreproTab.loadSettings(settings);
        } catch (InvalidSettingsException e) {
            LOGGER.info(e.getMessage());
        }
    }
}

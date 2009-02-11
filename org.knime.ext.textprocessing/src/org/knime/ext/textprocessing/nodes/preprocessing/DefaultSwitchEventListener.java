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
 *   09.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DefaultSwitchEventListener implements ChangeListener {
    
    private SettingsModelString m_stringModel;
    
    private SettingsModelBoolean m_booleanModel;
    
    /**
     * Creates new instance of <code>DefaultSwitchEventListener</code> with
     * given <code>SettingsModelString</code> to set disabled when given
     * <code>SettingsModelBoolean</code> has been unchecked and enables it
     * when the boolean model has been checked.
     * 
     * @param stringModel The model to enable or disable.
     * @param booelanModel The model to check or uncheck.
     */
    public DefaultSwitchEventListener(
            final SettingsModelString stringModel, 
            final SettingsModelBoolean booelanModel) {
        if (stringModel == null || booelanModel == null) {
            throw new IllegalArgumentException(
                    "Given models may not be null!");
        }
        
        m_stringModel = stringModel;
        m_booleanModel = booelanModel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        m_stringModel.setEnabled(m_booleanModel.getBooleanValue());
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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

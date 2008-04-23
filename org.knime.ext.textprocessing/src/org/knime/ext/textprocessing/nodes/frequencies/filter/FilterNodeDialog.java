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
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterNodeDialog extends DefaultNodeSettingsPane {

    public static final SettingsModelString getColModel() {
        return new SettingsModelString(FilterConfigKeys.CFG_KEY_FILTERCOL,
                "");
    }
    
    public static final SettingsModelString getSelectionModel() {
        return new SettingsModelString(FilterConfigKeys.CFG_KEY_SELECTION,
                FilterNodeModel.DEF_SELECTION);
    }
    
    public static final SettingsModelDoubleRange getMinMaxModel() {
        return new SettingsModelDoubleRange(FilterConfigKeys.CFG_KEY_MINMAX,
                FilterNodeModel.DEF_MIN_THRESHOLD, 
                FilterNodeModel.DEF_MAX_THRESHOLD);
    }
    
    public static final SettingsModelIntegerBounded getNumberModel() {
        return new SettingsModelIntegerBounded(FilterConfigKeys.CFG_KEY_NUMBER,
                FilterNodeModel.DEF_NUMBER, FilterNodeModel.MIN_NUMBER,
                FilterNodeModel.MAX_NUMBER);
    }
    
    
    private SettingsModelDoubleRange m_minMaxModel;
    
    private SettingsModelIntegerBounded m_numberModel;
    
    private SettingsModelString m_selectionModel;
    
    public FilterNodeDialog() {
        // Column Selection
        addDialogComponent(new DialogComponentColumnNameSelection(
                getColModel(), "Filter Column", 0, IntValue.class, 
                DoubleValue.class));
        
        // Filter Option
        ButtonGroupEnumInterface[] filteringOptions = new FilteringOptions[2];
        filteringOptions[0] = new FilteringOptions(
                FilterNodeModel.SELECTION_THRESHOLD);
        filteringOptions[1] = new FilteringOptions(
                FilterNodeModel.SELECTION_NUMBER);
        
        m_selectionModel = getSelectionModel();
        m_selectionModel.addChangeListener(new FilterOptionChangeListener());
        
        DialogComponentButtonGroup filterOptionButtons = 
            new DialogComponentButtonGroup(m_selectionModel, "Filtering by", 
                    false, filteringOptions);
        addDialogComponent(filterOptionButtons);
        
        // Min Max Settings
        createNewGroup("Min Max Settings");
        m_minMaxModel = getMinMaxModel();
        DialogComponentDoubleRange minMax = new DialogComponentDoubleRange(
                m_minMaxModel, FilterNodeModel.MIN_MIN_THRESHOLD,
                FilterNodeModel.MAX_MAX_THRESHOLD, 0.1, "");
        addDialogComponent(minMax);
        closeCurrentGroup();
        
        // Number Settings
        m_numberModel = getNumberModel();
        createNewGroup("Number Settings");
        addDialogComponent(new DialogComponentNumber(m_numberModel, "Number", 
                0.1));
        closeCurrentGroup();
        
        enableModels();
    }
    
    private void enableModels() {
        if (m_selectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_NUMBER)) {
            m_numberModel.setEnabled(true);
            m_minMaxModel.setEnabled(false);
        } else if (m_selectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_THRESHOLD)) {
            m_numberModel.setEnabled(false);
            m_minMaxModel.setEnabled(true);            
        }
    }
    
    private class FilterOptionChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        public void stateChanged(final ChangeEvent e) {
            enableModels();
        }
    }
    
    private class FilteringOptions implements ButtonGroupEnumInterface {

        private String m_selection;
        
        private FilteringOptions(final String selection) {
            m_selection = selection;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getActionCommand() {
            return m_selection;
        }

        /**
         * {@inheritDoc}
         */
        public String getText() {
            return m_selection;
        }

        /**
         * {@inheritDoc}
         */
        public String getToolTip() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isDefault() {
            if (m_selection.equals(FilterNodeModel.DEF_SELECTION)) {
                return true;
            }
            return false;
        }
    }
}

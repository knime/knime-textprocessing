/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Provides the dialog of the document vector node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentVectorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelBoolean</code> specifying of a bitvector have to
     * be created or not.
     */
    public static final SettingsModelBoolean getBooleanModel() {
        return new SettingsModelBoolean(DocumentVectorConfigKeys.CFGKEY_BOOLEAN,
                DocumentVectorNodeModel.DEFAULT_BOOLEAN);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> containing the name of the column with
     * the vector values to use.
     */
    public static final SettingsModelString getColumnModel() {
        return new SettingsModelString(
                DocumentVectorConfigKeys.CFGKEY_VALUE_COL,
                DocumentVectorNodeModel.DEFAULT_COL);
    }
    
    private SettingsModelString m_columnModel;
    
    private SettingsModelBoolean m_booleanModel;
    
    /**
     * Creates a new instance of <code>DocumentVectorNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public DocumentVectorNodeDialog() {
        m_columnModel = getColumnModel();
        m_booleanModel = getBooleanModel();
        m_booleanModel.addChangeListener(new InternalChangeListener());
        
        addDialogComponent(new DialogComponentBoolean(
                m_booleanModel, "Bitvector"));
        
        addDialogComponent(new DialogComponentColumnNameSelection(
                m_columnModel, "Vector value", 0, DoubleValue.class));
        
        checkUncheck();
    }
    
    private void checkUncheck() {
        if (m_booleanModel.getBooleanValue()) {
            m_columnModel.setEnabled(false);
        } else {
            m_columnModel.setEnabled(true);
        }
    }
    
    /**
     * Listens to changed and enables / disables the model of the column 
     * selection drop down box.
     * 
     * @author Kilian Thiel, University of Konstanz
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        public void stateChanged(final ChangeEvent e) {
            checkUncheck();
        }
    }
}

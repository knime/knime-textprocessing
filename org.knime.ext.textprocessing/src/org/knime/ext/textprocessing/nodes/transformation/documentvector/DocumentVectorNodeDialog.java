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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelString</code> containing the name of the document
     * column. 
     */
    public static final SettingsModelString getDocumentColModel() {
        return new SettingsModelString(DocumentVectorConfigKeys.CFGKEY_DOC_COL,
                DocumentVectorNodeModel.DEFAULT_DOCUMENT_COLNAME);
    }
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelBoolean</code> specifying whether tags will be ignored
     * or not.
     */
    public static final SettingsModelBoolean getIgnoreTagsModel() {
        return new SettingsModelBoolean(
                DocumentVectorConfigKeys.CFGKEY_IGNORE_TAGS,
                DocumentVectorNodeModel.DEFAULT_IGNORE_TAGS);
    }
    
    
    private SettingsModelString m_columnModel;
    
    private SettingsModelBoolean m_booleanModel;
    
    /**
     * Creates a new instance of <code>DocumentVectorNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public DocumentVectorNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getDocumentColModel(), "Document column", 0, 
                DocumentValue.class));
        
        addDialogComponent(new DialogComponentBoolean(
                getIgnoreTagsModel(), "Ignore tags"));        
        
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

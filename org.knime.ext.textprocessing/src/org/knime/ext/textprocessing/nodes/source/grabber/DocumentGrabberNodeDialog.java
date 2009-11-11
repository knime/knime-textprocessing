/*
========================================================================
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
 *   20.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;


/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentGrabberNodeDialog extends DefaultNodeSettingsPane {
    
    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(DocumentGrabberNodeDialog.class);    
    
    /**
     * @return Creates and returns the settings model of the query.
     */
    public static final SettingsModelString getQueryModel() {
        return new SettingsModelString(
                DocumentGrabberConfigKeys.CFGKEY_QUERY, ""); 
    }
    
    /**
     * @return Creates and returns the settings model of the maximum results.
     */
    public static final SettingsModelIntegerBounded getMaxResultsModel() {
        return new SettingsModelIntegerBounded(
                DocumentGrabberConfigKeys.CFGKEY_MAX_RESULTS, 
                DocumentGrabberNodeModel.DEF_RESULTS, 
                DocumentGrabberNodeModel.MIN_RESULTS,
                DocumentGrabberNodeModel.MAX_RESULTS); 
    }
    
    /**
     * @return Creates and returns the settings model of the data base to use.
     */
    public static final SettingsModelString getDataBaseModel() {
        return new SettingsModelString(
                DocumentGrabberConfigKeys.CFGKEY_DATATBASE,
                DocumentGrabberNodeModel.DEFAULT_DATABASE);
    }
    
    /**
     * @return Creates and returns the settings model of the deleted files flag.
     */
    public static final SettingsModelBoolean getDeleteFilesModel() {
        return new SettingsModelBoolean(
                DocumentGrabberConfigKeys.CFGKEY_DELETE,
                DocumentGrabberNodeModel.DEF_DELETE_AFTER_PARSE);
    }
    
    /**
     * @return Creates and returns the settings model of the directory to save
     * the files to.
     */
    public static final SettingsModelString getDirectoryModel() {
        return new SettingsModelString(
                DocumentGrabberConfigKeys.CFGKEY_DIR, 
                DocumentGrabberNodeModel.DEF_DIR);
    }
    
    /**
     * @return Creates and returns the settings model of the document category
     * to set.
     */
    public static final SettingsModelString getDocumentCategoryModel() {
        return new SettingsModelString(
                DocumentGrabberConfigKeys.CFGKEY_DOC_CAT, "");
    }
    
    /**
     * @return Creates and returns the settings model of the document type to
     * set.
     */
    public static final SettingsModelString getDocumentTypeModel() {
        return new SettingsModelString(
                DocumentGrabberConfigKeys.CFGKEY_DOC_TYPE, "");
    }
         
    
    private DialogComponentButtonLabel m_buttonLabel;
    private SettingsModelString m_queryModel;
    private SettingsModelString m_databaseModel;
    private SettingsModelInteger m_maxResultsModel;    
    
    /**
     * Creates new instance of <code>DocumentGrabberNodeDialog</code>.
     */
    public DocumentGrabberNodeDialog() {
        
        m_queryModel = getQueryModel();
        addDialogComponent(new DialogComponentMultiLineString(m_queryModel, 
                "Query: "));
        
        m_buttonLabel = new DialogComponentButtonLabel(
                "Number of results ", " 0 ");
        m_buttonLabel.addActionListener(new ButtonListener());
        addDialogComponent(m_buttonLabel);

        List<String> namesAsList = new ArrayList<String>(
                DocumentGrabberFactory.getInstance().getGrabberNames());
        m_databaseModel = getDataBaseModel();
        addDialogComponent(new DialogComponentStringSelection(m_databaseModel, 
                "Database: ", namesAsList));
        
        m_maxResultsModel = getMaxResultsModel();
        addDialogComponent(new DialogComponentNumber(m_maxResultsModel, 
                "Maximal results: ", 100));
        
        
        addDialogComponent(new DialogComponentFileChooser(
                getDirectoryModel(), "DocumentsDirectory - save", 
                        JFileChooser.SAVE_DIALOG, true));
        
        addDialogComponent(new DialogComponentBoolean(
                getDeleteFilesModel(), "Delete after parsing: "));
        
        addDialogComponent(new DialogComponentString(
                getDocumentCategoryModel(), "Document Category:"));
        
        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(
                getDocumentTypeModel(), "Document Type:", types));        
    }
    
    /**
     * 
     * @author Kilian Thiel, University of Konstanz
     */
    class ButtonListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        public void actionPerformed(final ActionEvent e) {
            String database = m_databaseModel.getStringValue();
            String query = m_queryModel.getStringValue();
            
            DocumentGrabber grabber = 
                DocumentGrabberFactory.getInstance().getGrabber(database);
            int numberOfResults;
            try {
                numberOfResults = grabber.numberOfResults(
                        new Query(query, m_maxResultsModel.getIntValue()));
                m_buttonLabel.setLabelText(Integer.toString(numberOfResults));
            } catch (Exception e1) {
                LOGGER.warn("Could not send query !");
            }
        }
    }
}

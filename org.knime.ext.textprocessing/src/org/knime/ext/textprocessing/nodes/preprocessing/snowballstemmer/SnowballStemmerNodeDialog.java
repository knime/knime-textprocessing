/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class SnowballStemmerNodeDialog extends PreprocessingNodeSettingsPane {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(SnowballStemmerNodeDialog.class);
    
    /**
     * @return Creates and returns new instance of 
     * <code>SettingsModelString</code> containing the name of the snowball 
     * stemmer.
     */
    public static SettingsModelString getStemmerNameModel() {
        return new SettingsModelString(
                SnowballStemmerConfigKeys.CFG_KEY_STEMMER_NAME,
                SnowballStemmerNodeModel.DEF_STEMMER_NAME);
    }
    
    /**
     * Creates new instance of <code>SnowballStemmerNodeDialog</code>.
     */
    public SnowballStemmerNodeDialog() {
        super();
        
        createNewTab("Stemmer options");
        setSelected("Stemmer options");
        
        List<String> names;
        try {
            names = new ArrayList<String>(
                    SnowballStemmerFactory.getInstance().getStemmerNames());
            addDialogComponent(new DialogComponentStringSelection(
                    getStemmerNameModel(), "Snowball Stemmer", names));
            
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        } catch (InstantiationException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        }
    }
}

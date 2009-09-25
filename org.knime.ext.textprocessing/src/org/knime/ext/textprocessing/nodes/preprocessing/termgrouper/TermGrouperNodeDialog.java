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
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.termgrouper;

import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class TermGrouperNodeDialog extends PreprocessingNodeSettingsPane {
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelString</code> containing the tag grouping policy.
     */
    public static SettingsModelString getTagGroupingPolicyModel() {
        return new SettingsModelString(
                TermGrouperConfigKeys.CFGKEY_GROUPPOLICY,
                TermGrouperNodeModel.DEFAULT_POLICY);
    }
    
    /**
     * Creates a new instance of <code>TermGrouperNodeDialog</code> .
     */
    public TermGrouperNodeDialog() {
        super();
        
        createNewTab("Grouping options");
        setSelected("Grouping options");
        
        List<String> options = new ArrayList<String>();
        options.add(TermGrouper.DELETE_ALL);
        options.add(TermGrouper.KEEP_ALL);
        options.add(TermGrouper.DELETE_CONFLICTING);
        addDialogComponent(new DialogComponentStringSelection(
                getTagGroupingPolicyModel(), "Tag grouping policy", options));
    }
}

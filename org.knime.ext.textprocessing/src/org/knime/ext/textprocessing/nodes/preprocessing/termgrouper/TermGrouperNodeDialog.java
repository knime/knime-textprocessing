/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

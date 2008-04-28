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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.ListSelectionModel;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 * Provides the dialog components and the complete of the tag filter node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class TagFilterNodeDialog extends PreprocessingNodeSettingsPane 
{

    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelBoolean</code> which specifies if strict
     * filtering is truned on or off.
     */
    public static SettingsModelBoolean getStrictFilteringModel() {
        return new SettingsModelBoolean(TagFilterConfigKeys.CFGKEY_STRICT,
                TagFilterNodeModel.DEF_STRICT);
    }
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelStringArray</code> which contains the set of
     * specified valid tags.
     */
    public static SettingsModelStringArray getValidTagsModel() {
        return new SettingsModelStringArray(
                TagFilterConfigKeys.CFGKEY_VALIDTAGS, new String[]{});
    }
    
    /**
     * @return The set of all tags which can be chosen as valid.
     * This method has to be implemented by underlying implementations, to
     * specify the set of all tags of a certain type. 
     */
    protected abstract Set<Tag> getTags();
    
    /**
     * Creates a new instance of <code>TagFilterNodeDialog</code>.
     */
    public TagFilterNodeDialog() {
        super();
        
        createNewTab("Tag settings");
        setSelected("Tag settings");
        
        addDialogComponent(new DialogComponentBoolean(
                getStrictFilteringModel(), "Strict filtering"));
        
        Collection<String> tagStrs = new ArrayList<String>();
        Set<Tag> validTags = getTags();
        for (Tag t : validTags) {
            tagStrs.add(t.getTagValue());
        }
        addDialogComponent(new DialogComponentStringListSelection(
                getValidTagsModel(), "Tags", tagStrs, 
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true, 10));
    }
}

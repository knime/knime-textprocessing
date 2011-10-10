/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TagFactory;

/**
 * The dialog class of the dictionary named entity recognizer node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryTaggerNodeDialog extends DefaultNodeSettingsPane {
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} 
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     * 
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static final SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_UNMODIFIABLE, 
                DictionaryTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the flag specifying whether the search for named entities
     * have to be done case sensitive or not.
     */
    public static final SettingsModelBoolean createCaseSensitiveModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_CASE_SENSITIVE,
                DictionaryTaggerNodeModel.DEFAULT_CASE_SENSITIVE);
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the file with the named entities to watch out for.
     */
    public static final SettingsModelString createFileModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_FILE, "");
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag type to assign to each found named entity.
     */
    public static final SettingsModelString createTagTypeModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_TAG_TYPE,
                DictionaryTaggerNodeModel.DEFAULT_TAG_TYPE);
    }    
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag to assign to each found named entity.
     */
    public static final SettingsModelString createTagModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_TAG, 
                DictionaryTaggerNodeModel.DEFAULT_TAG);
    }
    
    /**
     * Creates a new instance of <code>AbnerTaggerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not. 
     */
    public DictionaryTaggerNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(
                        createSetUnmodifiableModel(), 
                        "Set named entities unmodifiable"));
        
        addDialogComponent(new DialogComponentBoolean(
                createCaseSensitiveModel(), 
                "Case sensitive"));
        
        addDialogComponent(new DialogComponentFileChooser(
                createFileModel(), 
                DictionaryTaggerNodeDialog.class.toString()));
        
        
        // tag type model
        m_tagtypemodel = createTagTypeModel();
        m_tagtypemodel.addChangeListener(new InternalChangeListener());
        
        // tag list
        String selectedTagType = m_tagtypemodel.getStringValue();
        List<String> tags = TagFactory.getInstance()
                .getTagSetByType(selectedTagType).asStringList();
        m_tagSelection = new DialogComponentStringSelection(
                    createTagModel(), "Tag value", tags);

        this.setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentStringSelection(
                m_tagtypemodel, "Tag type", 
                TagFactory.getInstance().getTagTypes()));
        
        addDialogComponent(m_tagSelection);
    }
    
    private DialogComponentStringSelection m_tagSelection;
    
    private SettingsModelString m_tagtypemodel;
    
    /**
     * 
     * @author thiel, University of Konstanz
     */
    class InternalChangeListener implements ChangeListener {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedTagType = m_tagtypemodel.getStringValue();
            List<String> tags = TagFactory.getInstance()
                    .getTagSetByType(selectedTagType).asStringList();
            m_tagSelection.replaceListItems(tags, "");
        }
    }
}

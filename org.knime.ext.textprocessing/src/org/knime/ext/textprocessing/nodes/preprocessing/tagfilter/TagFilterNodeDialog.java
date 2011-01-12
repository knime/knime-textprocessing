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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ListSelectionModel;

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
     * <code>SettingsModelBoolean</code> which specifies if terms that
     * have the selected tags should be filtered or not.
     */
    public static SettingsModelBoolean getFilterMatchingModel() {
        return new SettingsModelBoolean(
                TagFilterConfigKeys.CFGKEY_FILTER_MATCHING,
                TagFilterNodeModel.DEF_FILTER_MATCHING);
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
        addDialogComponent(new DialogComponentBoolean(
                getFilterMatchingModel(), "Filter matching"));

        final List<String> tagStrs = new ArrayList<String>();
        final Set<Tag> validTags = getTags();
        for (final Tag t : validTags) {
            tagStrs.add(t.getTagValue());
        }
        Collections.sort(tagStrs);
        addDialogComponent(new DialogComponentStringListSelection(
                getValidTagsModel(), "Tags", tagStrs,
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true, 10));
    }
}

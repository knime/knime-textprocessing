/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilterConfigKeys;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilterNodeModel;

/**
 * Provides the dialog components of the general tag filter node.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class GeneralTagFilterNodeDialog extends PreprocessingNodeSettingsPane {

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
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag type to assign to each found named entity.
     */
    public static final SettingsModelString getTagTypeModel() {
        return new SettingsModelString(
                GeneralTagFilterConfigKeys.CFGKEY_TAGTYPE,
                NamedEntityTag.TAG_TYPE);
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


    private SettingsModelString m_tagtypemodel;

    private DialogComponentStringListSelection m_tagValuesSelectionList;

    /**
     * Creates a new instance of <code>TagFilterNodeDialog</code>.
     */
    public GeneralTagFilterNodeDialog() {
        super();

        createNewTab("Tag settings");
        setSelected("Tag settings");

        addDialogComponent(new DialogComponentBoolean(
                getStrictFilteringModel(), "Strict filtering"));
        addDialogComponent(new DialogComponentBoolean(
                getFilterMatchingModel(), "Filter matching"));

        m_tagtypemodel = getTagTypeModel();
        m_tagtypemodel.addChangeListener(new InternalChangeListener());

        addDialogComponent(new DialogComponentStringSelection(m_tagtypemodel,
            "Tag type", TagFactory.getInstance().getTagTypes()));

        m_tagValuesSelectionList = new DialogComponentStringListSelection(
            getValidTagsModel(), "Tags", getTagList(),
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true, 10);

        addDialogComponent(m_tagValuesSelectionList);
    }

    private List<String> getTagList() {
        final String selectedTagType = m_tagtypemodel.getStringValue();
        final Set<Tag> validTags = TagFactory.getInstance()
                .getTagSetByType(selectedTagType).getTags();
        final List<String> tagStrs = new ArrayList<String>();
        for (final Tag t : validTags) {
            tagStrs.add(t.getTagValue());
        }
        Collections.sort(tagStrs);

        return tagStrs;
    }

    /**
     *
     * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_tagValuesSelectionList.replaceListItems(getTagList(), "");
        }
    }
}

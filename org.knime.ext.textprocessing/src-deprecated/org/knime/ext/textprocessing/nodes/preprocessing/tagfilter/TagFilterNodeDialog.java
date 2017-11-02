/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ListSelectionModel;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter2.TagFilterNodeDialog2;

/**
 * Provides the dialog components and the complete of the tag filter node.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated use {@link TagFilterNodeDialog2} instead.
 */
@Deprecated
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

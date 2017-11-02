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
 * Created on 09.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.preferences;

import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngineSettings;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngines;


/**
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
public class SearchEnginePreferenceInitializer extends AbstractPreferenceInitializer {

    /** Preference key of the search engines. */
    public static final String PREF_SEARCHENGINES = "knime.textprocessing.searchengines";


    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TextprocessingCorePlugin.getDefault().getPreferenceStore();

        //set default values
        store.setDefault(PREF_SEARCHENGINES, getDefaultSearchEnginesSettingsString());
    }

    /**
     * Returns the search engine settings as settings string.
     *
     * @return the search engines settings string
     */
    public static String getSearchEnginesSettingsString() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!existsSearchEnginesSettings()) {
            return getDefaultSearchEnginesSettingsString();
        }
        return pStore.getString(PREF_SEARCHENGINES);
    }

    /**
     * @return {@code true} if search engine settings exist, otherwise {@code false}.
     */
    public static boolean existsSearchEnginesSettings() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        return pStore.contains(PREF_SEARCHENGINES);
    }

    /**
     * Creates and returns the default search engine settings as preference settings string.
     * @return the default search engine settings as preference settings string.
     */
    public static String getDefaultSearchEnginesSettingsString() {
        StringBuffer sb = new StringBuffer();
        List<SearchEngineSettings> settings = SearchEngines.getDefaultSearchEngines();
        int count = 0;
        for (SearchEngineSettings setting : settings) {
            sb.append(setting.getSettingsString());
            if (count < settings.size() - 1) {
                sb.append(SearchEngineSettings.SETTINGS_SEPARATOR);
            }
            count++;
        }
        return sb.toString();
    }
}

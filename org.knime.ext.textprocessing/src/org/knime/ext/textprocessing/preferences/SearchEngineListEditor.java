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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngineSettings;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngines;


/**
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
class SearchEngineListEditor extends ListEditor {

    private final Map<String, SearchEngineSettings> m_searchEngineSettings;

    /**
     * @param parent the parent composite
     */
    public SearchEngineListEditor(final Composite parent) {
        super(SearchEnginePreferenceInitializer.PREF_SEARCHENGINES,
              "List of configured search engines:", parent);
        m_searchEngineSettings = new TreeMap<String, SearchEngineSettings>();

        // select the newly created element
        getAddButton().addSelectionListener(new SelectionAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected(final SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == getAddButton()) {
                    int index = getList().getSelectionIndex();
                    if (index >= 0) {
                        getList().select(index + 1);
                    } else {
                        getList().select(0);
                    }
                    selectionChanged();
                    updateList();
                }
            }
        });

        /* Add our own selection listener before the parent's. Otherwise
         * the parent just removes the item and there is no chance to retrieve
         * the removed item.*/
        Button removeButton = getRemoveButton();
        Listener[] listeners = removeButton.getListeners(SWT.Selection);
        // remove registered selection listeners
        for (Listener listener : listeners) {
            removeButton.removeListener(SWT.Selection, listener);
        }
        // insert this listener as first selection listener
        removeButton.addSelectionListener(new SelectionAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected(final SelectionEvent event) {
                org.eclipse.swt.widgets.List list = getList();

                int index = list.getSelectionIndex();
                if (index >= 0) {
                    SearchEngineSettings setting = m_searchEngineSettings.remove(list.getItem(index));
                    if (setting != null) {
                        SearchEngines.getInstance().removeSearchEngine(setting);
                        updateList();
                    }
                }
            }
        });
        // add the previously registered listeners
        for (Listener listener : listeners) {
            removeButton.addListener(SWT.Selection, listener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#loadDefault()
     */
    @Override
    public void loadDefault() {
        super.loadDefault();
        SearchEngines.getInstance().loadDefault();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.ListEditor#createList(java.lang.String[])
     */
    @Override
    protected String createList(final String[] items) {
        String res = "";
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                res += SearchEngineSettings.SETTINGS_SEPARATOR;
            }
            String label = items[i];
            SearchEngineSettings settings = m_searchEngineSettings.get(label);
            if (settings != null) {
                res += settings.getSettingsString();
            }
        }
        return res;
    }

    private void updateList() {
        org.eclipse.swt.widgets.List listEditorList = getList();
        listEditorList.removeAll();
        m_searchEngineSettings.clear();

        SearchEngines searchEngines = SearchEngines.getInstance();
        for (SearchEngineSettings setting : searchEngines.getSearchEngineSetting()) {
            listEditorList.add(setting.getDisplayName());
            m_searchEngineSettings.put(setting.getDisplayName(), setting);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.ListEditor#getNewInputObject()
     */
    @Override
    protected String getNewInputObject() {
        NewSearchEngineDialog dlg = new NewSearchEngineDialog(getShell());
        if (dlg.open() != Window.OK) {
            return null;
        }
        SearchEngineSettings newSettings = dlg.getSettings();
        m_searchEngineSettings.put(newSettings.getDisplayName(), newSettings);
        SearchEngines.getInstance().addSearchEngine(newSettings);

        return newSettings.getDisplayName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
     */
    @Override
    protected String[] parseString(final String stringList) {
        List<SearchEngineSettings> settings = SearchEngineSettings.parseSettings(stringList);
        String[] labels = new String[settings.size()];
        for (int i = 0; i < labels.length; i++) {
            SearchEngineSettings setting = settings.get(i);
            m_searchEngineSettings.put(setting.getDisplayName(), setting);
            labels[i] = setting.getDisplayName();
        }
        return labels;
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   22.08.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.filter.StringFilterPanel;

/**
 * A string twin list with include and exclude list.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
public class DialogComponentStringFilter extends DialogComponent {

    private final StringFilterPanel m_stringFilterPanel;

    private String[] m_allColumns;

    private Set<String> m_includes;

    private Set<String> m_excludes;

    static final String CFG_CONFIGROOTNAME = "filter config";

    /**
     * @param model The {@code SettingsModelFilterString} storing include and exclude lists.
     * @param columnNames A String array containing the column names as Strings.
     * @param showSelectionListsOnly If true, the panel shows no additional options like search box,
     *            force-include-option, etc.
     */
    public DialogComponentStringFilter(final SettingsModelFilterString model, final String[] columnNames,
        final boolean showSelectionListsOnly) {
        super(model);
        initLists();
        m_allColumns = columnNames;
        m_stringFilterPanel = new StringFilterPanel(showSelectionListsOnly);
        getComponentPanel().setLayout(new BorderLayout());
        getComponentPanel().add(m_stringFilterPanel);

        // when the user input changes we need to update the model.
        m_stringFilterPanel.addChangeListener(e -> updateModel());
        getModel().addChangeListener(e -> updateComponent());
        updateModel();
    }

    /**
     * Update the settings from the component into the settings model.
     */
    private void updateModel() {
        final SettingsModelFilterString filterModel = (SettingsModelFilterString)getModel();
        m_includes = m_stringFilterPanel.getIncludedNamesAsSet();
        m_excludes = m_stringFilterPanel.getExcludedNamesAsSet();
        filterModel.setNewValues(m_includes, m_excludes, false);

    }

    private void initLists() {
        m_excludes = new HashSet<String>();
        m_includes = new HashSet<String>();
    }

    /**
     * Returns a String array containing the column names.
     *
     * @return Array of available columns names.
     */
    public String[] getAllColumns() {
        return m_allColumns;
    }

    /**
     * Method to set the column names
     *
     * @param allColumns String array of available column names
     */
    public void setAllColumns(final String[] allColumns) {
        m_allColumns = allColumns;
    }

    /**
     * Returns the includes list.
     *
     * @return The includes list.
     */
    public Set<String> getIncludesList() {
        return m_includes;
    }

    /**
     * Returns the excludes list.
     *
     * @return The excludes list.
     */
    public Set<String> getExcludesList() {
        return m_excludes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        updateModel();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // no need

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_stringFilterPanel.setEnabled(enabled);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        m_stringFilterPanel.setToolTipText(text);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateComponent() {
        final SettingsModelFilterString filterModel = (SettingsModelFilterString)getModel();
        m_includes = new HashSet<String>(filterModel.getIncludeList());
        m_excludes = new HashSet<String>(filterModel.getExcludeList());
        m_stringFilterPanel.update(filterModel.getIncludeList(), filterModel.getExcludeList(),
            m_allColumns);
        StringFilterConfiguration config = new StringFilterConfiguration(CFG_CONFIGROOTNAME);
        m_stringFilterPanel.saveConfiguration(config);
        m_stringFilterPanel.setEnabled(filterModel.isEnabled());
    }

    /**
     * @param config
     */
    protected void saveConfiguration(final StringFilterConfiguration config) {
        m_stringFilterPanel.saveConfiguration(config);
    }

    /**
     * @param config
     * @param names
     */
    protected void loadConfiguration(final StringFilterConfiguration config, final String[] names) {
        m_stringFilterPanel.loadConfiguration(config, names);
        m_allColumns = names;
        m_includes = m_stringFilterPanel.getIncludeList();
        m_excludes = m_stringFilterPanel.getExcludeList();

        updateModel();
        updateComponent();
    }

}

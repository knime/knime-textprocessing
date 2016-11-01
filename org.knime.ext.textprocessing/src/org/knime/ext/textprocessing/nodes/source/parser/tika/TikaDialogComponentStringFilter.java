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
 *   18.10.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.filter.StringFilterPanel;

/**
 * Dialog component string filter for all Tika nodes.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public final class TikaDialogComponentStringFilter extends DialogComponent {

    private final StringFilterPanel m_stringPanel;

    private String[] m_allTypes;

    private String m_type;

    private List<String> m_ext_incList;

    private List<String> m_ext_excList;

    private List<String> m_mime_incList;

    private List<String> m_mime_excList;

    /**
     * @param model the settingsmodel
     * @param type the type, either EXT or MIME
     * @param allTypes list of all available types, depending on the type
     */
    public TikaDialogComponentStringFilter(final SettingsModelFilterString model, final String type,
        final String[] allTypes) {
        this(model, type, allTypes, false);
    }

    /**
     * @param model the settingsmodel
     * @param type the type, either EXT or MIME
     * @param allTypes list of all available types, depending on the type
     * @param showSelectionListOnly if true, the panel shows no additional options like search box,
     *            force-include-option, etc.
     */
    public TikaDialogComponentStringFilter(final SettingsModelFilterString model, final String type,
        final String[] allTypes, final boolean showSelectionListOnly) {
        super(model);
        initLists();
        m_type = type;
        m_allTypes = allTypes;
        m_stringPanel = new StringFilterPanel(showSelectionListOnly);
        m_stringPanel.update(model.getIncludeList(), model.getExcludeList(), m_allTypes);
        getComponentPanel().setLayout(new BorderLayout());
        getComponentPanel().add(m_stringPanel);

        // when the user input changes we need to update the model.
        m_stringPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateModel();
            }
        });

        getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateComponent();
            }
        });
        updateModel();
    }

    /**
     * Update the settings from the component into the settings model.
     */
    private void updateModel() {
        final SettingsModelFilterString filterModel = (SettingsModelFilterString)getModel();
        final Set<String> inclList = m_stringPanel.getIncludedNamesAsSet();
        final Set<String> exclList = m_stringPanel.getExcludedNamesAsSet();

        filterModel.setNewValues(inclList, exclList, false);
        setListBasedOnType(new ArrayList<String>(inclList), new ArrayList<String>(exclList));
    }

    private void initLists() {
        m_ext_incList = new ArrayList<String>();
        m_ext_excList = new ArrayList<String>();
        m_mime_incList = new ArrayList<String>();
        m_mime_excList = new ArrayList<String>();
    }

    /**
     * Update the include and exclude lists. This method should be called by a change listener Exclude list will be set
     * to empty if both include/exclude lists are empty to each corresponding type.
     *
     */
    public void updateLists() {
        if (Objects.equals(m_type, "EXT")) {
            if (m_ext_incList.isEmpty() && m_ext_excList.isEmpty()) {
                m_stringPanel.update(new ArrayList<String>(Arrays.asList(m_allTypes)), new ArrayList<String>(),
                    m_allTypes);
            } else {
                m_stringPanel.update(m_ext_incList, m_ext_excList, m_allTypes);
            }
        } else if (Objects.equals(m_type, "MIME")) {
            if (m_mime_incList.isEmpty() && m_mime_excList.isEmpty()) {
                m_stringPanel.update(new ArrayList<String>(Arrays.asList(m_allTypes)), new ArrayList<String>(),
                    m_allTypes);
            } else {
                m_stringPanel.update(m_mime_incList, m_mime_excList, m_allTypes);
            }
        }
    }

    private void setListBasedOnType(final List<String> incList, final List<String> excList) {
        if (Objects.equals(m_type, "EXT")) {
            m_ext_incList = incList;
            m_ext_excList = excList;
        } else if (Objects.equals(m_type, "MIME")) {
            m_mime_incList = incList;
            m_mime_excList = excList;
        }
    }

    /**
     * @return list of available types, depending on the type
     */
    public String[] getAllTypes() {
        return m_allTypes;
    }

    /**
     * @param allTypes list of available types to be set
     */
    public void setAllTypes(final String[] allTypes) {
        m_allTypes = allTypes;
    }

    /**
     * @return type, either EXT or MIME
     */
    public String getType() {
        return m_type;
    }

    /**
     * @param type the type to be set
     */
    public void setType(final String type) {
        m_type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateComponent() {
        final SettingsModelFilterString filterModel = (SettingsModelFilterString)getModel();
        m_stringPanel.update(filterModel.getIncludeList(), filterModel.getExcludeList(), m_allTypes);
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
        m_stringPanel.setEnabled(enabled);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        m_stringPanel.setToolTipText(text);

    }

}

/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   Apr 11, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import static org.knime.core.node.util.DataColumnSpecListCellRenderer.createInvalidSpec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialog;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ConfigurationRequestEvent;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ConfigurationRequestListener;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ListModifier;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.SearchedItemsSelectionMode;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane2;
import org.knime.ext.textprocessing.nodes.tagging.dict.CommonDictionaryTaggerSettingModels;

/**
 * The {@link NodeDialog} for the {@code DictionaryTaggerMultiColumnNodeModel}. It extends the
 * {@link TaggerNodeSettingsPane2} to provide options shared between all tagger nodes.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
final class DictionaryTaggerMultiColumnNodeDialogPane extends TaggerNodeSettingsPane2 {

    /**
     * Dummy panel to have a reference preferred size for the {@code IndividualsPanel.}
     */
    private static final DictionaryTaggerPanel DUMMY_PANEL = new DictionaryTaggerPanel(
        new DictionaryTaggerConfiguration("DUMMY"), DataColumnSpecListCellRenderer.createInvalidSpec("DUMMY"));

    /**
     * Map containing a {@code DataColumnSpec} and a specific {@code DictionaryTaggerConfiguration} holding properties
     * for tagging terms based on the dictionary that is stored in the key column.
     */
    private final Map<DataColumnSpec, DictionaryTaggerConfiguration> m_columnToSettings;

    /**
     * The {@code IndividualsPanel} which holds all {@code DictionaryTaggerPanel}s.
     */
    private final IndividualsPanel m_individualsPanel;

    /**
     * Panel that provides a list of possible columns to select and a search field.
     */
    private final ColumnSelectionSearchableListPanel m_searchableListPanel;

    /**
     * Set of column names that are no longer available in the {@code DataTableSpec}.
     */
    private final Set<String> m_errornousColNames;

    /**
     * {@code JScrollPane} holding the {@code IndividualsPanel}
     */
    private final JScrollPane m_individualsScrollPanel;

    /**
     * Provides functionality to add/remove {@code DataColumnSpecs}.
     */
    private ListModifier m_searchableListModifier;

    /**
     * The {@link MultipleDictionaryTaggerConfiguration} containing the configurations for every single dictionary
     * column.
     */
    private MultipleDictionaryTaggerConfiguration m_multipleDictTaggerConfig;

    /**
     * A {@link SettingsModelBoolean} containing the flag specifying whether the terms should be set unmodifiable after
     * being tagged or not.
     */
    private final SettingsModelBoolean m_setUnmodifiableModel =
        CommonDictionaryTaggerSettingModels.createSetUnmodifiableModel();

    /**
     * The dictionary table spec.
     */
    private DataTableSpec m_dictTableSpec;

    /**
     * Creates a new instance of {@code DictionaryTaggerMultiColumnNodeDialogPane}.
     */
    DictionaryTaggerMultiColumnNodeDialogPane() {
        super();

        // Dictionary Tagger Tab
        m_columnToSettings = new LinkedHashMap<>();
        m_errornousColNames = new HashSet<>();

        m_searchableListPanel = new ColumnSelectionSearchableListPanel(SearchedItemsSelectionMode.SELECT_FIRST,
            spec -> m_columnToSettings.containsKey(spec));

        m_searchableListPanel.addConfigurationRequestListener(new ConfigurationRequestListener() {

            @Override
            public void configurationRequested(final ConfigurationRequestEvent searchEvent) {
                switch (searchEvent.getType()) {
                    case CREATION:
                        createAndAddDictTaggerPanelSetting();
                        break;
                    case DELETION:
                        for (DataColumnSpec spec : m_searchableListPanel.getSelectedColumns()) {
                            DictionaryTaggerConfiguration dictTaggerColumnSetting = m_columnToSettings.get(spec);
                            if (dictTaggerColumnSetting != null) {
                                int indexIndividualIndex = getIndexIndividualIndex(m_columnToSettings.get(spec));
                                removeFromIndividualPanel(
                                    (DictionaryTaggerPanel)m_individualsPanel.getComponent(indexIndividualIndex));
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        m_searchableListPanel.showSelectionPanel(false);

        final JPanel leftPanel = new JPanel(new BorderLayout());

        final JPanel setUnmodifiablePanel = new JPanel(new BorderLayout());
        final JCheckBox unmodifiableBox = new JCheckBox("Set entities unmodifiable");
        unmodifiableBox.setSelected(m_setUnmodifiableModel.getBooleanValue());
        unmodifiableBox.addItemListener(e -> m_setUnmodifiableModel.setBooleanValue(unmodifiableBox.isSelected()));
        setUnmodifiablePanel.add(unmodifiableBox, BorderLayout.CENTER);
        setUnmodifiablePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        leftPanel.add(m_searchableListPanel, BorderLayout.CENTER);
        leftPanel.add(setUnmodifiablePanel, BorderLayout.SOUTH);

        m_individualsPanel = new IndividualsPanel();
        m_individualsScrollPanel = new JScrollPane(m_individualsPanel);

        final JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(leftPanel, BorderLayout.CENTER);
        tabPanel.add(m_individualsScrollPanel, BorderLayout.EAST);

        addTab("Dictionary Tagger Selection", tabPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        try {
            m_setUnmodifiableModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            // just catch...
        }

        // retrieve possible specs
        DataTableSpec spec = specs[1];
        List<DataColumnSpec> possibleSpecs = new ArrayList<>();
        for (String colName : spec.getColumnNames()) {
            DataColumnSpec colSpec = spec.getColumnSpec(colName);
            if (colSpec.getType().isCompatible(StringValue.class)) {
                possibleSpecs.add(colSpec);
            }
        }
        DataTableSpec possibleSpec = new DataTableSpec(possibleSpecs.toArray(new DataColumnSpec[0]));

        if (spec.getNumColumns() == 0 || possibleSpecs.isEmpty()) {
            throw new NotConfigurableException("No (String) columns at input.");
        }

        // update searchable list panel based on possible specs
        m_searchableListModifier = m_searchableListPanel.update(possibleSpec);

        // get multi tagger config based on node settings
        try {
            m_multipleDictTaggerConfig = new MultipleDictionaryTaggerConfiguration(settings);
        } catch (InvalidSettingsException is) {
            // just catch
        }

        // get the dictionary table spec
        m_dictTableSpec = specs[1];
        // fill the individuals panel with dictionary tagger panels
        fillIndividualsPanel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
        m_multipleDictTaggerConfig.save(settings);

        clearBorders();
    }

    /**
     * Create a {@code DictionaryTaggerPanel} and add it to the {@code IndividualsPanel}.
     */
    private final void createAndAddDictTaggerPanelSetting() {
        // there can only by one or non column selected.
        final DataColumnSpec selected = m_searchableListPanel.getSelectedColumn();
        if (selected != null && !m_columnToSettings.containsKey(selected)) {
            DictionaryTaggerConfiguration dictTaggerColumnSetting =
                new DictionaryTaggerConfiguration(selected.getName());
            m_columnToSettings.put(selected, dictTaggerColumnSetting);
            m_multipleDictTaggerConfig.add(dictTaggerColumnSetting);
            addToIndividualPanel(new DictionaryTaggerPanel(dictTaggerColumnSetting, selected));
        }
    }

    /**
     * Fill the {@code IndividualsPanel} with {@code DictionaryTaggerPanels} based on a
     * {@code MultipleDictionaryTaggerConfiguration} object.
     */
    private final void fillIndividualsPanel() {
        // clear settings, errornous settings and remove DictionaryTaggerPanels from IndividualsPanel
        m_columnToSettings.clear();
        m_errornousColNames.clear();
        m_individualsPanel.removeAll();
        // fill panel
        if (m_multipleDictTaggerConfig != null) {
            for (DictionaryTaggerConfiguration dictTaggerColumnSetting : m_multipleDictTaggerConfig.getConfigs()) {
                final String colName = dictTaggerColumnSetting.getColumnName();

                DataColumnSpec colSpec = m_dictTableSpec.getColumnSpec(colName);

                if (colSpec == null) {
                    colSpec = createInvalidSpec(colName);
                    m_searchableListModifier.addAdditionalColumn(colSpec);
                }
                m_columnToSettings.put(colSpec, dictTaggerColumnSetting);
                DictionaryTaggerPanel panel = new DictionaryTaggerPanel(dictTaggerColumnSetting, colSpec);
                addToIndividualPanel(panel);
            }
            // reconfigure up/down buttons of each DictionaryTaggerPanel
            configureButtons();
        }
    }

    /**
     * Resets all marked components.
     */
    private final void clearBorders() {
        m_errornousColNames.clear();
        for (int i = 0; i < m_individualsPanel.getComponentCount(); i++) {
            DictionaryTaggerPanel panel = ((DictionaryTaggerPanel)m_individualsPanel.getComponent(i));
            if (panel.hasValidSpec()) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            }
        }
    }

    /**
     * Returns the index of a {@code DictionaryTaggerPanel} within an {@code IndividualsPanel} based on a
     * {@code DictionaryTaggerConfiguration}.
     *
     * @param config
     * @return Returns the index of a {@code DictionaryTaggerPanel} within an {@code IndividualsPanel}.
     */
    private final int getIndexIndividualIndex(final DictionaryTaggerConfiguration config) {
        for (int i = 0; i < m_individualsPanel.getComponentCount(); i++) {
            DictionaryTaggerPanel component = (DictionaryTaggerPanel)m_individualsPanel.getComponent(i);
            if (component.getSettings().getColumnName().equals(config.getColumnName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes {@code DictionaryTaggerPanel} from IndividualsPanel.
     *
     * @param panel The {@code DictionaryTaggerPanel} to remove.
     */
    private final void removeFromIndividualPanel(final DictionaryTaggerPanel panel) {
        if (m_searchableListPanel.isAdditionalColumn(panel.getColumnSpec())) {
            m_searchableListModifier.removeAdditionalColumn(panel.getColumnSpec().getName());
        }
        m_columnToSettings.remove(panel.getColumnSpec());
        m_individualsPanel.remove(panel);
        m_multipleDictTaggerConfig.remove(panel.getSettings());
        // reconfigure buttons
        configureButtons();
        m_individualsPanel.revalidate();
        m_individualsPanel.repaint();
        m_searchableListPanel.revalidate();
        m_searchableListPanel.repaint();
    }

    /**
     * Adds a {@code DictionaryTaggerPanel} to IndividualsPanel.
     *
     * @param panel The {@code DictionaryTaggerPanel} to add.
     */
    private final void addToIndividualPanel(final DictionaryTaggerPanel panel) {
        panel.addPropertyChangeListener(DictionaryTaggerPanel.REMOVE_ACTION,
            propertyChangeEvent -> removeFromIndividualPanel(panel));
        panel.addPropertyChangeListener(DictionaryTaggerPanel.UP_ACTION, propertyChangeEvent -> movePanelUp(panel));
        panel.addPropertyChangeListener(DictionaryTaggerPanel.DOWN_ACTION, propertyChangeEvent -> movePanelDown(panel));
        m_individualsPanel.add(panel);
        // reconfigure buttons
        configureButtons();
        m_individualsPanel.revalidate();
        m_individualsPanel.ensureLastVisible();
        m_searchableListPanel.revalidate();
        m_searchableListPanel.repaint();
    }

    /**
     * Moves a {@code DictionaryTaggerPanel} up within an {@code IndividualsPanel}.
     *
     * @param panel1 The {@code DictionaryTaggerPanel} to move up.
     */
    private void movePanelUp(final DictionaryTaggerPanel panel1) {
        final int panelIndex = getIndexIndividualIndex(panel1.getSettings());
        DictionaryTaggerPanel panel2 = (DictionaryTaggerPanel)m_individualsPanel.getComponent(panelIndex - 1);
        DataColumnSpec spec1 = panel1.getColumnSpec();
        DictionaryTaggerConfiguration config1 = new DictionaryTaggerConfiguration(panel1.getSettings());
        panel1.setSettings(panel2.getSettings(), panel2.getColumnSpec());
        panel2.setSettings(config1, spec1);
        // reconfigure buttons
        configureButtons();
        m_individualsPanel.repaint();
        m_individualsPanel.revalidate();
    }

    /**
     * Moves a {@code DictionaryTaggerPanel} up within an {@code IndividualsPanel}.
     *
     * @param panel1 The {@code DictionaryTaggerPanel} to move down.
     */
    private void movePanelDown(final DictionaryTaggerPanel panel1) {
        final int panelIndex = getIndexIndividualIndex(panel1.getSettings());
        DictionaryTaggerPanel panel2 = (DictionaryTaggerPanel)m_individualsPanel.getComponent(panelIndex + 1);
        DataColumnSpec spec1 = panel1.getColumnSpec();
        DictionaryTaggerConfiguration config1 = new DictionaryTaggerConfiguration(panel1.getSettings());
        panel1.setSettings(panel2.getSettings(), panel2.getColumnSpec());
        panel2.setSettings(config1, spec1);
        // reconfigure buttons
        configureButtons();
        m_individualsPanel.repaint();
        m_individualsPanel.revalidate();
    }

    /**
     * Reconfigures the up/down buttons of every panel.
     */
    private void configureButtons() {
        final int componentCount = m_individualsPanel.getComponentCount();
        for (Component panel : m_individualsPanel.getComponents()) {
            DictionaryTaggerPanel dictPanel = (DictionaryTaggerPanel)panel;
            if (componentCount == 1) {
                dictPanel.enableUpButton(false);
                dictPanel.enableUpButton(false);
            } else {
                int individualIndex = getIndexIndividualIndex(dictPanel.getSettings());
                // case for panel between first and last panel
                if (individualIndex > 0 && individualIndex < componentCount - 1) {
                    dictPanel.enableUpButton(true);
                    dictPanel.enableDownButton(true);
                } else if (individualIndex == 0) {
                    dictPanel.enableUpButton(false);
                    dictPanel.enableDownButton(true);
                } else if (individualIndex == componentCount - 1) {
                    dictPanel.enableUpButton(true);
                    dictPanel.enableDownButton(false);
                }
            }
        }
    }

    /**
     * Panel hosting the individual panels. It implements {@link Scrollable} to allow for correct jumping to the next
     * enclosed panel. It allows overwrites getPreferredSize() to return the sum of all individual heights.
     */
    @SuppressWarnings("serial")
    private static class IndividualsPanel extends JPanel implements Scrollable {

        /** Set box layout. */
        public IndividualsPanel() {
            BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(layout);
        }

        /** {@inheritDoc} */
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return DUMMY_PANEL.getPreferredSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableBlockIncrement(final Rectangle visibleRect, //
            final int orientation, final int direction) {
            int rh = getComponentCount() > 0 ? getComponent(0).getHeight() : 0;
            return (rh > 0) ? Math.max(rh, (visibleRect.height / rh) * rh) : visibleRect.height;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
            return getComponentCount() > 0 ? getComponent(0).getHeight() : 100;
        }

        /** {@inheritDoc} */
        @Override
        public Dimension getPreferredSize() {
            int height = 0;
            int width = 0;
            if (getComponentCount() < 1) {
                return DUMMY_PANEL.getPreferredSize();
            }
            for (Component c : getComponents()) {
                Dimension h = c.getPreferredSize();
                height += h.height;
                width = Math.max(width, h.width);
            }
            return new Dimension(width, height);
        }

        /**
         * Ensures that the last added component is visible.
         */
        public final void ensureLastVisible() {
            if (getComponentCount() > 2) {
                //the bounds of the last added components is zeroed, so we use the second last.
                Rectangle bounds = getComponent(getComponentCount() - 2).getBounds();
                bounds.y += getPreferredSize().getHeight() / 2;
                scrollRectToVisible(bounds);
            }
        }
    }

}

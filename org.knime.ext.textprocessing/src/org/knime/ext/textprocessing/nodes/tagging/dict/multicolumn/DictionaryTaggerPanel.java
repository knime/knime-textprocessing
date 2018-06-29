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

import static org.knime.core.node.util.DataColumnSpecListCellRenderer.isInvalid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.knime.core.node.util.SharedIcons;
import org.knime.ext.textprocessing.data.TagFactory;

/**
 * The {@code DictionaryTaggerPanel} which holds the tagger options for each column.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class DictionaryTaggerPanel extends JPanel {

    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = -199770705515091178L;

    /**
     * Abbreviate names to this number of letters.
     */
    private static final int MAX_LETTERS = 20;

    /**
     * Fired if the remove button is pressed.
     */
    static final String REMOVE_ACTION = "REMOVE_ACTION";

    /**
     * Fired if the up button is pressed.
     */
    static final String UP_ACTION = "UP_ACTION";

    /**
     * Fired if the up button is pressed.
     */
    static final String DOWN_ACTION = "DOWN_ACTION";

    /**
     * The settings that is displayed by the panel.
     */
    private final DictionaryTaggerSettings m_settings;

    /**
     * The column spec related to the panel.
     */
    private DataColumnSpec m_columnSpec;

    /**
     * The "arrow up" button.
     */
    private final JButton m_upButton;

    /**
     * The "arrow down" button.
     */
    private final JButton m_downButton;

    /**
     * The case sensitivity checkbox.
     */
    private final JCheckBox m_caseSensitivityChecker;

    /**
     * The exact match checkbox.
     */
    private final JCheckBox m_exactMatchChecker;

    /**
     * The tag value selection combo box.
     */
    private final JComboBox<String> m_tagValueSelection;

    /**
     * The tag type selection combo box.
     */
    private final JComboBox<String> m_tagTypeSelection;

    /**
     * The label holding the column name.
     */
    private final JLabel m_nameLabel;

    /**
     * Creates a new instance of {@code DictionaryTaggerPanel}.
     *
     * @param settings The {@code DocumentTaggerSettings} containing the settings values used for tagging.
     * @param spec The {@code DataColumnSpec} containing the name of the column which contains the dictionary used for
     *            tagging.
     */
    DictionaryTaggerPanel(final DictionaryTaggerSettings settings, final DataColumnSpec spec) {
        m_settings = settings;
        m_columnSpec = spec;
        final String colName = settings.getColumnName();

        String labelName = colName.length() > MAX_LETTERS ? colName.substring(0, MAX_LETTERS) + "..." : colName;

        m_nameLabel = new JLabel(labelName);
        m_nameLabel.setToolTipText(colName);

        m_upButton = new JButton(SharedIcons.MOVE_UP.get());
        m_upButton.addActionListener(e -> firePropertyChange(UP_ACTION, null, null));

        m_downButton = new JButton(SharedIcons.MOVE_DOWN.get());
        m_downButton.addActionListener(e -> firePropertyChange(DOWN_ACTION, null, null));

        JButton removeButton = new JButton(SharedIcons.DELETE_TRASH.get());
        removeButton.addActionListener(e -> firePropertyChange(REMOVE_ACTION, null, null));

        m_caseSensitivityChecker = new JCheckBox("Case sensitive", settings.getCaseSensitivityOption());
        m_caseSensitivityChecker
            .addItemListener(e -> m_settings.setCaseSensitivityOption(m_caseSensitivityChecker.isSelected()));

        m_exactMatchChecker = new JCheckBox("Exact match", settings.getExactMatchOption());
        m_exactMatchChecker.addItemListener(e -> m_settings.setExactMatchOption(m_exactMatchChecker.isSelected()));

        m_tagValueSelection = new JComboBox<>(
            TagFactory.getInstance().getTagSetByType(settings.getTagType()).asStringList().toArray(new String[0]));
        m_tagValueSelection.setSelectedItem(settings.getTagValue());
        m_tagValueSelection.addItemListener(e -> m_settings.setTagValue((String)m_tagValueSelection.getSelectedItem()));
        m_tagValueSelection.setPrototypeDisplayValue(TagFactory.getInstance().getLongestTagValue());

        m_tagTypeSelection = new JComboBox<>(TagFactory.getInstance().getTagTypes().toArray(new String[0]));

        m_tagTypeSelection.setSelectedItem(settings.getTagType());
        m_tagTypeSelection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                m_tagValueSelection.removeAllItems();
                String[] tagSetByType =
                    TagFactory.getInstance().getTagSetByType((String)e.getItem()).asStringList().toArray(new String[0]);

                for (String value : tagSetByType) {
                    m_tagValueSelection.addItem(value);
                }
                m_settings.setTagType((String)m_tagTypeSelection.getSelectedItem());
            }
        });
        m_tagTypeSelection.setPrototypeDisplayValue(TagFactory.getInstance().getLongestTagType());

        setBorder(isInvalid(spec) ? BorderFactory.createLineBorder(Color.RED, 2)
            : BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel buttonLayout = new JPanel(new GridLayout(0, 3));
        buttonLayout.add(m_upButton);
        buttonLayout.add(m_downButton);
        buttonLayout.add(removeButton);
        buttonLayout.setPreferredSize(new Dimension(80, 20));

        // Panel for name label and remove button
        JPanel northLayout = new JPanel(new BorderLayout(15, 0));
        northLayout.add(m_nameLabel, BorderLayout.WEST);
        northLayout.add(buttonLayout, BorderLayout.EAST);

        // Panel for case sensitivity and exact match check box
        JPanel centerLayout = new JPanel(new BorderLayout());
        centerLayout.add(m_caseSensitivityChecker, BorderLayout.WEST);
        centerLayout.add(m_exactMatchChecker, BorderLayout.EAST);

        // Panel for tag type and tag selection
        JPanel southLayout = new JPanel(new BorderLayout(20, 0));
        southLayout.add(m_tagTypeSelection, BorderLayout.WEST);
        southLayout.add(m_tagValueSelection, BorderLayout.EAST);

        setLayout(new FlowLayout());

        // Add three panels from above to a super panel
        JPanel dtp = new JPanel(new BorderLayout(0, 10));
        dtp.add(northLayout, BorderLayout.NORTH);
        dtp.add(centerLayout, BorderLayout.CENTER);
        dtp.add(southLayout, BorderLayout.SOUTH);
        add(dtp);
    }

    /**
     * Returns a {@code DictionaryTaggerSettings} that is displayed by an instance of this panel.
     *
     * @return Returns a {@code DictionaryTaggerSettings} that is displayed by an instance of this panel.
     */
    DictionaryTaggerSettings getSettings() {
        return m_settings;
    }

    /**
     * Returns a {@code DataColumnSpec} that is related to an instance of this panel.
     *
     * @return Returns a {@code DataColumnSpec} that is related to an instance of this panel.
     */
    DataColumnSpec getColumnSpec() {
        return m_columnSpec;
    }

    /**
     * @return {@code true} if {@link DataColumnSpecListCellRenderer#isInvalid(DataColumnSpec)} returns {@code false}
     *         for the current spec.
     */
    boolean hasValidSpec() {
        return !isInvalid(m_columnSpec);
    }

    /**
     * Method to enable/disable the arrow up button.
     *
     * @param enable Set true to enable the button, otherwise false.
     */
    void enableUpButton(final boolean enable) {
        m_upButton.setEnabled(enable);
    }

    /**
     * Method to enable/disable the arrow down button.
     *
     * @param enable Set true to enable the button, otherwise false.
     */
    void enableDownButton(final boolean enable) {
        m_downButton.setEnabled(enable);
    }

    /**
     * Sets the settings of the {@code DictionaryTaggerPanel} based on a {@code DictionaryTaggerSettings} and
     * a {@code DataTableSpec}.
     *
     * @param settings The {@code DictionaryTaggerSettings} holding the settings to be set.
     * @param spec The {@code DataColumnSpec} to be set.
     */
    void setSettings(final DictionaryTaggerSettings settings, final DataColumnSpec spec) {
        final String newName = settings.getColumnName();
        m_nameLabel.setText(newName.length() > MAX_LETTERS ? newName.substring(0, MAX_LETTERS) + "..." : newName);
        m_settings.setColumnName(newName);
        m_caseSensitivityChecker.setSelected(settings.getCaseSensitivityOption());
        m_exactMatchChecker.setSelected(settings.getExactMatchOption());
        m_tagTypeSelection.setSelectedItem(settings.getTagType());
        m_tagValueSelection.setSelectedItem(settings.getTagValue());
        m_columnSpec = spec;
    }
}

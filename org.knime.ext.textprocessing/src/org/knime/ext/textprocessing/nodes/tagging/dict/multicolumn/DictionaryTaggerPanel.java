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
 *   Apr 11, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import static org.knime.core.node.util.DataColumnSpecListCellRenderer.isInvalid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import org.knime.ext.textprocessing.data.TagFactory;

/**
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin Germany
 */
@SuppressWarnings("serial")
public class DictionaryTaggerPanel extends JPanel {

    /** abbreviate names to this number of letters. */
    private static final int MAX_LETTERS = 25;

    /**
     * fired if the remove button is pressed.
     */
    public static final String REMOVE_ACTION = "REMOVE_ACTION";

    private final DictionaryTaggerColumnSetting m_settings;

    private final DataColumnSpec m_columnSpec;

    /**
     * @param colSet
     * @param spec
     */
    public DictionaryTaggerPanel(final DictionaryTaggerColumnSetting colSet, final DataColumnSpec spec) {

        m_settings = colSet;
        m_columnSpec = spec;
        final String colName = colSet.getColumnName();
        boolean caseSensitive = colSet.caseSensitive();
        boolean exactMatch = colSet.exactMatch();
        String tagType = colSet.tagType();
        String tagValue = colSet.tagValue();

        String labelName =
                colName.length() > MAX_LETTERS ? colName.substring(0, MAX_LETTERS) + "..." : colName;

        final JLabel nameLabel = new JLabel(labelName);
        nameLabel.setToolTipText(colName);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> firePropertyChange(REMOVE_ACTION, null, null));

        final JCheckBox caseSensitivityChecker = new JCheckBox("Case sensitive", caseSensitive);

        final JCheckBox exactMatchChecker = new JCheckBox("Exact match", exactMatch);

        final JComboBox<String> tagValueSelection =
            new JComboBox<String>(TagFactory.getInstance().getTagSetByType(tagType).asStringList().toArray(new String[0]));
        tagValueSelection.setSelectedItem(tagValue);

        final JComboBox<String> tagTypeSelection = new JComboBox<String>(TagFactory.getInstance().getTagTypes().toArray(new String[0]));

        tagTypeSelection.setSelectedItem(tagType);

        tagTypeSelection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                tagValueSelection.removeAllItems();
                String[] tagSetByType =
                    TagFactory.getInstance().getTagSetByType((String)e.getItem()).asStringList().toArray(new String[0]);

                for (String value : tagSetByType) {
                    tagValueSelection.addItem(value);
                }
            }
        });

        setBorder(isInvalid(spec) ? BorderFactory.createLineBorder(Color.RED, 2) : BorderFactory.createLineBorder(
            Color.BLACK, 1));

        JPanel northLayout = new JPanel(new BorderLayout());
        northLayout.add(nameLabel, BorderLayout.WEST);
        northLayout.add(removeButton, BorderLayout.EAST);

        JPanel centerLayout = new JPanel(new BorderLayout());
        centerLayout.add(caseSensitivityChecker, BorderLayout.WEST);
        centerLayout.add(exactMatchChecker, BorderLayout.EAST);

        JPanel southLayout = new JPanel(new BorderLayout());
        southLayout.add(tagTypeSelection, BorderLayout.WEST);
        southLayout.add(tagValueSelection, BorderLayout.EAST);

        setLayout(new FlowLayout());

        JPanel dtp = new JPanel(new BorderLayout());
        dtp.add(northLayout, BorderLayout.NORTH);
        dtp.add(centerLayout, BorderLayout.CENTER);
        dtp.add(southLayout, BorderLayout.SOUTH);
        add(dtp);
    }

    DictionaryTaggerColumnSetting getSettings() {
        return m_settings;
    }

    DataColumnSpec getColumnSpec() {
        return m_columnSpec;
    }

    /**
     * @return <code>true</code> if {@link DataColumnSpecListCellRenderer#isInvalid(DataColumnSpec)} returns
     *         <code>false</code> for the current spec
     */
    boolean hasValidSpec() {
        return !isInvalid(m_columnSpec);
    }
}

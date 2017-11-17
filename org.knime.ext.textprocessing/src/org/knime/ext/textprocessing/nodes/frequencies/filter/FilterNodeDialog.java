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
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The dialog class of the filter node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the name of the column to
     * apply the filtering.
     */
    public static final SettingsModelString getColModel() {
        return new SettingsModelString(FilterConfigKeys.CFG_KEY_FILTERCOL,
                "");
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the mode of filtering
     */
    public static final SettingsModelString getSelectionModel() {
        return new SettingsModelString(FilterConfigKeys.CFG_KEY_SELECTION,
                FilterNodeModel.DEF_SELECTION);
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelDoubleRange</code> containing the filter's min and
     * max value.
     */
    public static final SettingsModelDoubleRange getMinMaxModel() {
        return new SettingsModelDoubleRange(FilterConfigKeys.CFG_KEY_MINMAX,
                FilterNodeModel.DEF_MIN_THRESHOLD,
                FilterNodeModel.DEF_MAX_THRESHOLD);
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelIntegerBounded</code> containing the number k of terms
     * to keep (the rest is filtered).
     */
    public static final SettingsModelIntegerBounded getNumberModel() {
        return new SettingsModelIntegerBounded(FilterConfigKeys.CFG_KEY_NUMBER,
                FilterNodeModel.DEF_NUMBER, FilterNodeModel.MIN_NUMBER,
                FilterNodeModel.MAX_NUMBER);
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelBoolean</code> specifying if deep filtering has to be
     * applied.
     */
    public static final SettingsModelBoolean getDeepFilteringModel() {
        return new SettingsModelBoolean(FilterConfigKeys.CFG_KEY_DEEPFILTERING,
                FilterNodeModel.DEF_DEEP_FILTERING);
    }

    /**
     * @return Creates and returns a new instance of {@code SettingsModelString} containing the name of the document
     *         column.
     * @since 3.5
     */
    public static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(FilterConfigKeys.CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelBoolean</code> specifying if modification of
     * unmodifiable terms is done.
     */
    public static final SettingsModelBoolean getModifyUnmodifiableModel() {
        return new SettingsModelBoolean(
                FilterConfigKeys.CFG_KEY_MODIFY_UNMODIFIABLE,
                FilterNodeModel.DEF_MODIFY_UNMODIFIABLE);
    }

    private final SettingsModelDoubleRange m_minMaxModel;

    private final SettingsModelIntegerBounded m_numberModel;

    private final SettingsModelString m_selectionModel;

    /**
     * Creates a new instance of <code>FilterNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public FilterNodeDialog() {
        removeTab("Options");
        createNewTabAt("Deep Filtering", 2);
        DialogComponentBoolean comp = new DialogComponentBoolean(
                getDeepFilteringModel(), "Deep filtering");
        comp.setToolTipText(
        "Be aware that deep filtering is more time consuming!");

        DialogComponentColumnNameSelection comp3 =
            new DialogComponentColumnNameSelection(
                    FilterNodeDialog.getDocumentColumnModel(),
                    "Document column", 0, DocumentValue.class);
        addDialogComponent(comp3);


        addDialogComponent(comp);
                createNewTabAt("Filter Settings", 1);

        // Modify Unmodifiable
        addDialogComponent(new DialogComponentBoolean(
                getModifyUnmodifiableModel(), "Filter unmodifiable terms"));

        // Column Selection
        addDialogComponent(new DialogComponentColumnNameSelection(
                getColModel(), "Filter column", 0, IntValue.class,
                DoubleValue.class));


        // Filter Option
        ButtonGroupEnumInterface[] filteringOptions = new FilteringOptions[2];
        filteringOptions[0] = new FilteringOptions(
                FilterNodeModel.SELECTION_THRESHOLD);
        filteringOptions[1] = new FilteringOptions(
                FilterNodeModel.SELECTION_NUMBER);

        m_selectionModel = getSelectionModel();
        m_selectionModel.addChangeListener(new FilterOptionChangeListener());

        DialogComponentButtonGroup filterOptionButtons =
            new DialogComponentButtonGroup(m_selectionModel, "Filtering by",
                    false, filteringOptions);
        addDialogComponent(filterOptionButtons);

        // Min Max Settings
        createNewGroup("Min Max Settings");
        m_minMaxModel = getMinMaxModel();
        DialogComponentDoubleRange minMax = new DialogComponentDoubleRange(
                m_minMaxModel, FilterNodeModel.MIN_MIN_THRESHOLD,
                FilterNodeModel.MAX_MAX_THRESHOLD, 0.1, "");
        addDialogComponent(minMax);
        closeCurrentGroup();

        // Number Settings
        m_numberModel = getNumberModel();
        createNewGroup("Number Settings");
        addDialogComponent(new DialogComponentNumber(m_numberModel, "Number",
                100));
        closeCurrentGroup();

        enableModels();
    }

    private void enableModels() {
        if (m_selectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_NUMBER)) {
            m_numberModel.setEnabled(true);
            m_minMaxModel.setEnabled(false);
        } else if (m_selectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_THRESHOLD)) {
            m_numberModel.setEnabled(false);
            m_minMaxModel.setEnabled(true);
        }
    }

    private class FilterOptionChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            enableModels();
        }
    }

    private class FilteringOptions implements ButtonGroupEnumInterface {

        private final String m_selection;

        private FilteringOptions(final String selection) {
            m_selection = selection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getActionCommand() {
            return m_selection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return m_selection;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getToolTip() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefault() {
            if (m_selection.equals(FilterNodeModel.DEF_SELECTION)) {
                return true;
            }
            return false;
        }
    }
}

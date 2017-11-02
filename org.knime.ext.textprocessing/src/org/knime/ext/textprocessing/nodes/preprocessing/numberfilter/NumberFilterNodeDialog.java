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
 *   24.04.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.numberfilter;

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 * The {@code NodeDialog} for the NumberFilter node. It extends the {@link PreprocessingNodeSettingsPane2} to keep
 * general preprocessing options and provides the possibility to choose between to different kinds of filtering.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
class NumberFilterNodeDialog extends PreprocessingNodeSettingsPane2 {
    /**
     * Returns and creates a {@code SettingsModelBoolean} for filtering terms that contain numbers.
     *
     * @return a settings model
     */
    static final SettingsModelString getFilteringModeModel() {
        return new SettingsModelString(NumberFilterConfigKeys.CFGKEY_FILTERINGMODE, NumberFilter.DEF_FILTERINGMODE);
    }

    /**
     * Creates a new instance of the dialog.
     */
    public NumberFilterNodeDialog() {
        ButtonGroupEnumInterface[] modes = new ButtonGroupEnumInterface[2];
        modes[0] = new FilterModeButtonGroup("Filter terms representing numbers", true,
            "Filters terms, that represent numbers", NumberFilter.FILTERINGMODE_TERM_REPRESENTS_NUMBER);
        modes[1] = new FilterModeButtonGroup("Filter terms containing numbers", false,
            "Filters any terms, that contain numbers", NumberFilter.FILTERINGMODE_TERM_CONTAINS_NUMBER);

        createNewTab("Filter options");
        setSelected("Filter options");
        addDialogComponent(new DialogComponentButtonGroup(getFilteringModeModel(), "Filtering mode", true, modes));
    }

    private final class FilterModeButtonGroup implements ButtonGroupEnumInterface {
        private String m_text;

        private String m_tooltip;

        private boolean m_default;

        private String m_command;

        private FilterModeButtonGroup(final String text, final boolean isDefault, final String toolTip,
            final String command) {
            m_text = text;
            m_tooltip = toolTip;
            m_default = isDefault;
            m_command = command;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getActionCommand() {
            return m_command;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return m_text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getToolTip() {
            return m_tooltip;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefault() {
            return m_default;
        }
    }
}

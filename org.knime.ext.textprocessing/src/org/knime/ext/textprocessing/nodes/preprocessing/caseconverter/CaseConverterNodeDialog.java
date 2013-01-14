/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   03.04.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.caseconverter;

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class CaseConverterNodeDialog extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a <code>SettingsModelString</code> which
     * contains the case to convert to.
     */
    public static SettingsModelString getCaseModel() {
        return new SettingsModelString(CaseConverterConfigKeys.CFG_KEY_CASE,
                CaseConverterNodeModel.DEF_CASE);
    }

    /**
     * Creates a new instance of <code>CaseConverterNodeDialog</code> providing
     * a button group with radio buttons to select the case to convert to.
     */
    public CaseConverterNodeDialog() {
        super();

        createNewTab("Converter options");
        setSelected("Converter options");

        ButtonGroupEnumInterface[] options = new ButtonGroupEnumInterface[2];
        options[0] = new CaseButtonGroup("To lower case", true,
                "Converts to lower case", CaseConverter.LOWER_CASE);
        options[1] = new CaseButtonGroup("To upper case", false,
                "Converts to upper case", CaseConverter.UPPER_CASE);

        addDialogComponent(new DialogComponentButtonGroup(getCaseModel(),
                "Case to convert to", false, options));
    }

    private final class CaseButtonGroup implements ButtonGroupEnumInterface {

        private String m_text;

        private String m_tooltip;

        private boolean m_default;

        private String m_command;

        private CaseButtonGroup(final String text, final boolean isDefault,
                final String toolTip, final String command) {
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

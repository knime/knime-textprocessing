/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
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
    
    private class CaseButtonGroup implements ButtonGroupEnumInterface {

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
        public String getActionCommand() {
            return m_command;
        }

        /**
         * {@inheritDoc}
         */
        public String getText() {
            return m_text;
        }

        /**
         * {@inheritDoc}
         */
        public String getToolTip() {
            return m_tooltip;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isDefault() {
            return m_default;
        }
    }
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   16.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.ncharsfilter;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 * Dialog of the N chars filter node, providing a spinner to specify the
 * number N of the N chars filter.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NCharsFilterNodeDialog extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelIntegerBounded</code> containing the number N.
     */
    public static final SettingsModelIntegerBounded getNModel() {
        return new SettingsModelIntegerBounded(NCharsFilterConfigKeys.CFGKEY_N,
                NCharsFilterNodeModel.DEF_N, NCharsFilterNodeModel.MIN_N,
                NCharsFilterNodeModel.MAX_N);
    }
    
    
    /**
     * Creates a new instance of <code>NCharsFilterNodeDialog</code> providing
     * a spinner to specify the number N.
     */
    public NCharsFilterNodeDialog() {
        super();
        
        createNewTab("Filter options");
        setSelected("Filter options");
        
        addDialogComponent(new DialogComponentNumber(
                getNModel(), "N Chars", 1));
    }
}

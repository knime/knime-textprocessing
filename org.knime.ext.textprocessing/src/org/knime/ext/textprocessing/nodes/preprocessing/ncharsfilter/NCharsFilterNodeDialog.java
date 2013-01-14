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

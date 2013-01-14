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
 *   25.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termtostructure;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStructureNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column which has to
     * be used as title column.
     */
    public static final SettingsModelString getTermColModel() {
        return new SettingsModelString(
                TermToStructureConfigKeys.CFGKEY_TERMCOL, 
                BagOfWordsDataTableBuilder.DEF_TERM_COLNAME);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the type of the format to
     * convert structures to.
     */
    public static final SettingsModelString getFormatTypeModel() {
        return new SettingsModelString(
                TermToStructureConfigKeys.CFGKEY_FORMAT_TYPE, 
                TermToStructureNodeModel.DEF_FORMAT_TYPE);
    }    
    
    /**
     * Creates a new instance of <code>TermToStringNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public TermToStructureNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getTermColModel(), "Term Column", 0, TermValue.class));
        
        List<String> formats = new ArrayList<String>();
        for (FormatType f : FormatType.values()) {
            formats.add(f.toString());
        }
        addDialogComponent(new DialogComponentStringSelection(
                getFormatTypeModel(), "Format type", formats));
    }
}

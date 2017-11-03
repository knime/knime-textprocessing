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

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStructureNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of <code>SettingsModelString</code> specifying the column which has to be
     *         used as title column.
     */
    public static final SettingsModelString getTermColModel() {
        return new SettingsModelString(TermToStructureConfigKeys.CFGKEY_TERMCOL, "");
    }

    /**
     * @return Creates and returns an instance of <code>SettingsModelString</code> specifying the type of the format to
     *         convert structures to.
     */
    public static final SettingsModelString getFormatTypeModel() {
        return new SettingsModelString(TermToStructureConfigKeys.CFGKEY_FORMAT_TYPE,
            TermToStructureNodeModel.DEF_FORMAT_TYPE);
    }

    /**
     * Creates a new instance of <code>TermToStringNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public TermToStructureNodeDialog() {
        addDialogComponent(
            new DialogComponentColumnNameSelection(getTermColModel(), "Term Column", 0, TermValue.class));

        List<String> formats = new ArrayList<String>();
        for (FormatType f : FormatType.values()) {
            formats.add(f.toString());
        }
        addDialogComponent(new DialogComponentStringSelection(getFormatTypeModel(), "Format type", formats));
    }
}

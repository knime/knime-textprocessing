/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   15.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermValue;


/**
 * The configuration dialog for the tagcloud.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
public class TagCloudNodeDialog extends DefaultNodeSettingsPane {

    private final SettingsModelString m_term;
    private final SettingsModelString m_columnvalue;
    private final SettingsModelString m_typeofcalctc;
    private final SettingsModelBoolean m_ignoretags;
    private SettingsModelIntegerBounded m_noOfRows;
    private SettingsModelBoolean m_allRows;



    /**
     *Creates new instance of <code>TagCloudNodeDialog</code>.
    */
    @SuppressWarnings("unchecked")
    public TagCloudNodeDialog() {
        removeTab("Options");
        createNewTabAt("Options", 1);


        createNewGroup("Rows to display:");
        m_noOfRows = getNoofRowsModel();
        m_allRows = getUseallrowsBooleanModel();
        m_allRows.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_noOfRows.setEnabled(!m_allRows.getBooleanValue());
            }
        });
        addDialogComponent(new DialogComponentBoolean(m_allRows,
                TagCloudConfigKeys.ALL_ROWS_LABEL));
        addDialogComponent(
                new DialogComponentNumber(m_noOfRows,
                        TagCloudConfigKeys.NO_OF_ROWS_LABEL, new Integer(1)));

        m_term = getTermColumnModel();
        m_columnvalue = getValueModel();
        m_ignoretags = getBooleanModel();
        m_typeofcalctc = getTypeofTCcalculationModel();

        closeCurrentGroup();
        createNewGroup(" Column settings:");
        addDialogComponent(new DialogComponentColumnNameSelection(
                m_term,
                "Term column", 0, TermValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(
                m_columnvalue,
                "Value column", 0, IntValue.class, DoubleValue.class));

        closeCurrentGroup();
        createNewGroup(" Tagcloud settings:");

        addDialogComponent(new DialogComponentBoolean(
                m_ignoretags,
                TagCloudConfigKeys.CFGKEY_IGNORE_TAGS));

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0;
               i < TagCloudConfigKeys.CFG_TYPEOFTCCALCI.length; i++) {
            list.add(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[i]);
        }
        addDialogComponent(
                new DialogComponentStringSelection(m_typeofcalctc,
                TagCloudConfigKeys.CFG_TYPEOFTCCALC, list));
        closeCurrentGroup();
    }

    /**
     ** @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the type of
     * tagcloud which should be calculated.
     */
    public static final SettingsModelString getTypeofTCcalculationModel() {
        return new SettingsModelString(
                TagCloudConfigKeys.CFG_TYPEOFTCCALC,
                TagCloudConfigKeys.CFG_TYPEOFTCCALCI[0]);
    }

    /**
     * * @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the column containing
     * the Term.
     */
    public static final SettingsModelString getTermColumnModel() {
        return new SettingsModelString(
                TagCloudConfigKeys.CFG_KEY_TERM_COL,
                    "");
    }

    /**
      * @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the column containing
     * the values, needed for the font size distribution.
      */
    public static final SettingsModelString getValueModel() {
        return  new SettingsModelString(
                TagCloudConfigKeys.CFG_KEY_VALUE_COL,
                    "");
    }

    /**
     * @return Creates and returns an instance of
     * <code>SettingsModelBoolean</code> specifying if the tags should be
     * ignored or not.
     */
    public static final SettingsModelBoolean getBooleanModel() {
        return new SettingsModelBoolean(TagCloudConfigKeys.CFGKEY_IGNORE_TAGS,
                true);
    }

    /**
     * @return Creates and returns an instance of
     * <code>SettingsModelInteger</code> specifying the number of rows
     */
    public static final SettingsModelIntegerBounded getNoofRowsModel() {
        return new SettingsModelIntegerBounded(
                TagCloudConfigKeys.CFGKEY_NO_OF_ROWS,
                TagCloudConfigKeys.DEFAULT_NO_OF_ROWS, 0,
                Integer.MAX_VALUE);
    }

    /**
     * @return Creates and returns an instance of
     * <code>SettingsModelBoolean</code> specifying if the table should be
     * filtered or not
     */
    public static final SettingsModelBoolean getUseallrowsBooleanModel() {
        return new SettingsModelBoolean(
                TagCloudConfigKeys.CFGKEY_ALL_ROWS, false);
    }

}

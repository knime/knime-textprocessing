/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   15.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud.outport;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.NodeViewExport;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermValue;


/**
 * The configuration dialog for the tagcloud.
 *
 * @author Iris Adae, University of Konstanz
 */
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
        m_allRows = getUseallrowsBooleanModel();
        m_noOfRows = getNoofRowsModel();
        m_term = getTermColumnModel();
        m_columnvalue = getValueModel();
        m_ignoretags = getBooleanModel();
        m_typeofcalctc = getTypeofTCcalculationModel();


        removeTab("Options");
        createNewTabAt("General", 1);

        createNewGroup("Row settings:");
        m_allRows.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_noOfRows.setEnabled(!m_allRows.getBooleanValue());
            }
        });
        addDialogComponent(new DialogComponentBoolean(m_allRows,
                TagCloudConfigKeys.ALL_ROWS_LABEL));
        addDialogComponent(new DialogComponentNumber(m_noOfRows,
                TagCloudConfigKeys.NO_OF_ROWS_LABEL, new Integer(1)));
        closeCurrentGroup();

        createNewGroup("Column settings:");
        addDialogComponent(new DialogComponentColumnNameSelection(m_term,
                "Term column", 0, TermValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(m_columnvalue,
                "Value column", 0, IntValue.class, DoubleValue.class));
        closeCurrentGroup();

        createNewGroup("Tag Cloud settings:");
        addDialogComponent(new DialogComponentBoolean(m_ignoretags,
                TagCloudConfigKeys.CFGKEY_IGNORE_TAGS));

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < TagCloudConfigKeys.CFG_TYPEOFTCCALCI.length; i++) {
            list.add(TagCloudConfigKeys.CFG_TYPEOFTCCALCI[i]);
        }
        addDialogComponent(new DialogComponentStringSelection(m_typeofcalctc,
                TagCloudConfigKeys.CFG_TYPEOFTCCALC, list));
        closeCurrentGroup();


        createNewTab("Image Export");
        final Set<String> exportTypes =
            NodeViewExport.getViewExportMap().keySet();
        addDialogComponent(new DialogComponentStringSelection(
                getImageTypeModel(), "Image type:", exportTypes));

        createNewGroup("Image size:");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumber(
                getWidthModel(), "Width:", Integer.valueOf(10)));
        addDialogComponent(new DialogComponentNumber(
                getHeightModel(), "Height:", Integer.valueOf(10)));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Colors and shape:");
        addDialogComponent(new DialogComponentColorChooser(
                getBackgroundColorModel(), "Background color:", true));

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumber(
                getAlphaModel(), "Alpha:", 1));

        addDialogComponent(new DialogComponentNumber(
                getBoldModel(), "Bold:", 1));

        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentBoolean(
                getAntiAliasingModel(), "Antialiasing"));
        closeCurrentGroup();
    }

    public static SettingsModelIntegerBounded getBoldModel() {
        return new SettingsModelIntegerBounded(
                TagCloudConfigKeys.CFGKEY_BOLD_VALUE,
                AbstractTagCloud.DEFAULT_BOLD, 0, 100);
    }

    public static SettingsModelIntegerBounded getAlphaModel() {
        return new SettingsModelIntegerBounded(
                TagCloudConfigKeys.CFGKEY_ALPHA_VALUE,
                AbstractTagCloud.DEFAULT_ALPHA, 0, 100);
    }

    public static SettingsModelColor getBackgroundColorModel() {
        return new SettingsModelColor(
                TagCloudConfigKeys.CFGKEY_BACKGROUND_COLOR,
                TagCloudNodeModel.DEFAULT_BACKGROUND_COLOR);
    }

    public static SettingsModelBoolean getAntiAliasingModel() {
        return new SettingsModelBoolean(TagCloudConfigKeys.CFGKEY_ANTIALIASING,
                TagCloudNodeModel.DEFAULT_ANTIALIASING);
    }

    public static SettingsModelString getImageTypeModel() {
        final Set<String> types = NodeViewExport.getViewExportMap().keySet();
        String preset;
        if (types != null && !types.isEmpty()) {
            preset = types.iterator().next();
        } else {
            preset = null;
        }
        return new SettingsModelString(TagCloudConfigKeys.CFGKEY_EXPORTTYPE,
                preset);
    }

    public static final SettingsModelIntegerBounded getWidthModel() {
        return new SettingsModelIntegerBounded(TagCloudConfigKeys.CFGKEY_WIDTH,
                TagCloudNodeModel.DEFAULT_WIDTH, 0, Integer.MAX_VALUE);
    }

    public static final SettingsModelIntegerBounded getHeightModel() {
        return new SettingsModelIntegerBounded(TagCloudConfigKeys.CFGKEY_HEIGHT,
                TagCloudNodeModel.DEFAULT_HEIGHT, 0, Integer.MAX_VALUE);
    }


    /**
     ** @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the type of
     * tagcloud which should be calculated.
     */
    public static final SettingsModelString getTypeofTCcalculationModel() {
        return new SettingsModelString(
                TagCloudConfigKeys.CFG_TYPEOFTCCALC,
                TagCloudConfigKeys.
                CFG_TYPEOFTCCALCI[TagCloudConfigKeys.DEFAULT_TAGCLOUD_TYPE]);
    }

    /**
     * * @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the column containing
     * the Term.
     */
    public static final SettingsModelString getTermColumnModel() {
        return new SettingsModelString(
                TagCloudConfigKeys.CFG_KEY_TERM_COL, "");
    }

    /**
      * @return Creates and returns an instance of
     * <code>SettingsModelString</code> specifying the column containing
     * the values, needed for the font size distribution.
      */
    public static final SettingsModelString getValueModel() {
        return  new SettingsModelString(
                TagCloudConfigKeys.CFG_KEY_VALUE_COL, "");
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
                TagCloudConfigKeys.DEFAULT_NO_OF_ROWS, 0, Integer.MAX_VALUE);
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

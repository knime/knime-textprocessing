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
 * -------------------------------------------------------------------
 *
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class SnowballStemmerNodeDialog extends PreprocessingNodeSettingsPane {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(SnowballStemmerNodeDialog.class);

    /**
     * @return Creates and returns new instance of
     * <code>SettingsModelString</code> containing the name of the snowball
     * stemmer.
     */
    public static SettingsModelString getStemmerNameModel() {
        return new SettingsModelString(
                SnowballStemmerConfigKeys.CFG_KEY_STEMMER_NAME,
                SnowballStemmerNodeModel.DEF_STEMMER_NAME);
    }

    /**
     * Creates new instance of <code>SnowballStemmerNodeDialog</code>.
     */
    public SnowballStemmerNodeDialog() {
        super();

        createNewTab("Stemmer options");
        setSelected("Stemmer options");

        List<String> names;
        try {
            SnowballStemmerFactory stemmerFactory =
                    new SnowballStemmerFactory();
            names = new ArrayList<String>(stemmerFactory.getStemmerNames());
            addDialogComponent(new DialogComponentStringSelection(
                    getStemmerNameModel(), "Snowball Stemmer", names));

        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        } catch (InstantiationException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
            //e.printStackTrace();
        }
    }
}

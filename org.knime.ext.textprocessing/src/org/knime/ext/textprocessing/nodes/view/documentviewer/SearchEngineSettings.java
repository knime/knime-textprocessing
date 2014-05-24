/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * Created on 09.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.util.ArrayList;
import java.util.List;



/**
 * Contains search engine name, link and default setting.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class SearchEngineSettings {

    /** Used for separating multiple search engine settings in the preferences.*/
    public static final String SETTINGS_SEPARATOR = "\n";

    private static final String FIELD_SEPARATOR = "::";

    private static final String DEFAULT_MARKER = "default";

    private static final String NON_DEFAULT_MARKER = "notdefault";

    private String m_name;

    private String m_link;

    private boolean m_default;

    /**
     * Creates new instance of {@code SearchEngineSettings} with given settings as setting string.
     * @param setting The settings, name and link as string
     */
    public SearchEngineSettings(final String setting) {
        parse(setting);
    }

    /**
     * Creates new instance of {@code SearchEngineSettings} with given name and link. Search engine is not set as
     * standard engine.
     * @param name the search engine name to set
     * @param link the link to the search engine to set
     */
    public SearchEngineSettings(final String name, final String link) {
        this(name, link, false);
    }

    /**
     * Creates new instance of {@code SearchEngineSettings} with given name, link and default flag.
     * @param name the search engine name to set
     * @param link the link to the search engine to set
     * @param def if {@code true} search engine is set as default engine
     */
    public SearchEngineSettings(final String name, final String link, final boolean def) {
        m_name = name;
        m_link = link;
        m_default = def;
    }

    private void parse(final String setting) {
        String[] splits = setting.split(FIELD_SEPARATOR);
//        if (3 != splits.length) {
//            throw new IllegalArgumentException(
//                    "Invalid settings string provided.");
//        }

        m_name = splits[0];
        if (splits.length >= 2 && splits[1] != null) {
            m_link = splits[1];
        }
        if (splits.length == 3 && splits[2] != null && splits[2].equals(DEFAULT_MARKER)) {
            m_default = true;
        } else {
            m_default = false;
        }
    }

    /**
     * @return the name of the search engine
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the diplay name of the search engine
     */
    public String getDisplayName() {
        if (m_default) {
            return m_name + " (default)";
        }
        return m_name;
    }

    /**
     * @return the link to the search engine
     */
    public String getLink() {
        return m_link;
    }

    /**
     * @return the default flag
     */
    public boolean isDefault() {
        return m_default;
    }

    /**
     * @return this search engine settings settings as preference string
     */
    public String getSettingsString() {
        String defMarker = NON_DEFAULT_MARKER;
        if (m_default) {
            defMarker = DEFAULT_MARKER;
        }
        return m_name + FIELD_SEPARATOR + m_link + FIELD_SEPARATOR + defMarker;
    }

    /**
     * Parses a settings string containing one or multiple settings separated
     * by {@link SearchEngineSettings#SETTINGS_SEPARATOR}.
     * @param settings the preference string to parse
     * @return the parsed list of search engine settings
     */
    public static List<SearchEngineSettings> parseSettings(final String settings) {
        List<SearchEngineSettings> ms = new ArrayList<SearchEngineSettings>();
        if (settings.isEmpty()) {
            return ms;
        }
        String[] split = settings.split(SETTINGS_SEPARATOR);
        for (String setting : split) {
            if (!settings.isEmpty()) {
                ms.add(new SearchEngineSettings(setting));
            }
        }
        return ms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getSettingsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SearchEngineSettings)) {
            return false;
        }
        return getSettingsString().equals(
                ((SearchEngineSettings)obj).getSettingsString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getSettingsString().hashCode();
    }
}

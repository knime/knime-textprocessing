/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import opennlp.tools.namefind.TokenNameFinderModel;

import org.knime.core.node.NodeLogger;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpenNlpModel {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(OpenNlpModel.class);

    private String m_name;

    private SoftReference<TokenNameFinderModel> m_model;

    private String m_fileName;

    private String m_tag;

    /**
     * Creates new instance of <code>OpenNlpModel</code> with given name, model
     * and tag to set.
     * @param name The model's name.
     * @param fileName The name of the file containing the maxent model.
     * @param tag The corresponding tag.
     */
    public OpenNlpModel(final String name, final String fileName,
            final String tag) {
        m_name = name;
        m_fileName = fileName;
        m_tag = tag;
        m_model = null;
    }

    /**
     * @return The name of the model.
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return The maxent model.
     * @since 2.7
     */
    public synchronized TokenNameFinderModel getModel() {
        TokenNameFinderModel m = m_model == null ? null : m_model.get();
        if (m == null) {
            File f = new File(m_fileName);
            if (!f.exists() || !f.isFile() || !f.canRead()) {
                LOGGER.warn("Maxent model file [" + m_fileName
                        + "] is not valid!");
            }

            try {
                LOGGER.info("Loading Maxent model ["
                        + f.getName() + "].");
                InputStream is = new FileInputStream(f);
                m = new TokenNameFinderModel(is);
                m_model = new SoftReference<TokenNameFinderModel>(m);
            } catch (IOException e) {
                LOGGER.warn("Maxent model could not be loeded from file ["
                        + m_fileName + "]!", e);
            }
        }
        return m;
    }

    /**
     * @return The corresponding tag.
     */
    public String getTag() {
        return m_tag;
    }

    /**
     * @return The name of the file containing the maxent model.
     */
    public String getFileName() {
        return m_fileName;
    }
}

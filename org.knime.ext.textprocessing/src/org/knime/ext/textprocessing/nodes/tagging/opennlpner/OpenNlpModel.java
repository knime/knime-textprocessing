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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

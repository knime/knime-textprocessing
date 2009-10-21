/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.SuffixSensitiveGISModelReader;

import org.knime.core.node.NodeLogger;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpenNlpModel {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(OpenNlpModel.class);

    private String m_name;

    private SoftReference<MaxentModel> m_model;

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
     */
    public synchronized MaxentModel getModel() {
        MaxentModel m = m_model == null ? null : m_model.get();
        if (m == null) {
            File f = new File(m_fileName);
            if (!f.exists() || !f.isFile() || !f.canRead()) {
                LOGGER.warn("Maxent model file [" + m_fileName
                        + "] is not valid!");
            }

            try {
                LOGGER.info("Loading Maxent model ["
                        + f.getName() + "].");
                m = new SuffixSensitiveGISModelReader(f).getModel();
                m_model = new SoftReference<MaxentModel>(m);
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

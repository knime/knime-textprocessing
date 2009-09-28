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

import opennlp.maxent.MaxentModel;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpenNlpModel {

    private String m_name;
    
    private MaxentModel m_model;
    
    private String m_tag;

    /**
     * Creates new instance of <code>OpenNlpModel</code> with given name, model 
     * and tag to set.
     * @param name The model's name.
     * @param model The maxent model.
     * @param tag The corresponding tag.
     */
    public OpenNlpModel(final String name, final MaxentModel model, 
            final String tag) {
        m_name = name;
        m_model = model;
        m_tag = tag;
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
    public MaxentModel getModel() {
        return m_model;
    }

    /**
     * @return The corresponding tag.
     */
    public String getTag() {
        return m_tag;
    }
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.ext.textprocessing.nodes.preprocessing.kuhlenstemmer.KuhlenStemmer;
import org.knime.ext.textprocessing.nodes.preprocessing.porterstemmer.PorterStemmer;

import java.util.ArrayList;
import java.util.List;


/**
 * The enum registers all provided stemming methods and enables a generic usage
 * of them.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public enum Stemming {

    /**
     * The Kuhlen Stemmer.
     */
    KUHLEN {
        /**
         * {@inheritDoc}
         */
        @Override
        public Preprocessing getPreprocessing() {
            return new KuhlenStemmer();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public StringPreprocessing getStringPreprocessing() {
            return new KuhlenStemmer();
        }
    },
    
    /**
     * The Porter Stemmer.
     */
    PORTER {
        /**
         * {@inheritDoc}
         */
        @Override
        public Preprocessing getPreprocessing() {
            return new PorterStemmer();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public StringPreprocessing getStringPreprocessing() {
            return new PorterStemmer();
        }
    };
    
    /**
     * @return a certain stemmer as <code>Preprocessing</code> instance, which
     * is used to preprocess <code>Term</code>s.
     */
    public abstract Preprocessing getPreprocessing();
    
    /**
     * @return a certain stemmer as <code>StringPreprocessing</code> instance, 
     * which is used to preprocess <code>String</code>s
     */
    public abstract StringPreprocessing getStringPreprocessing();
    
    /**
     * Returns the enum fields as a String list of their names.
     * 
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<Stemming>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }    
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Hashtable;
import java.util.List;

import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

/**
 * This class is a utility class providing different helper methods, i.e. to 
 * build up a word cache used by 
 * {@link org.knime.ext.textprocessing.data.Document}s to enable fast access
 * to {@link org.knime.ext.textprocessing.data.Term}s and underlying 
 * {@link org.knime.ext.textprocessing.data.Word}s. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class WordCacheHelper {

    private WordCacheHelper() { }
    
    
    public static Hashtable<Word, List<Term>> buildWordCache(
            final List<Section> sections) {
        Hashtable<Word, List<Term>> cache = new Hashtable<Word, List<Term>>();
        
        
        
        
        return cache;
    }
    
}

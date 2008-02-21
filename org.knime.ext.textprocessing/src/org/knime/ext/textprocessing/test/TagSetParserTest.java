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
 *   21.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.test;

import java.util.Set;

import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.TagFactory;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagSetParserTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        TagFactory tf = TagFactory.getInstance();
        Set<TagBuilder> ts = tf.getTagSet();
        for (TagBuilder tb : ts) {
            System.out.println(tb.toString());
        }
    }

}

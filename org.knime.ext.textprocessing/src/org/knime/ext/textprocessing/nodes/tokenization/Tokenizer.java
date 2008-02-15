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
package org.knime.ext.textprocessing.nodes.tokenization;

import java.util.List;

/**
 * Defines the default methods all tokenizer have to provide.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface Tokenizer {

    /**
     * @return A list of the string tokens created during the tokenization 
     * of the given text string.
     */
    public List<String> tokenize(final String text);
}

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
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import org.knime.ext.textprocessing.data.Document;

/**
 * Classes that restructure, retokenize or tag documents or terms respectively
 * should implement this interface to enable a proper use by tagger node models.
 * Restructuring of documents could be i.e. a noun phrase parser, detecting
 * noun phrases and combine words to terms which represent the phrases. The
 * document terms would be restructured, so the document self gets 
 * restructured too. What all the underlying classes have in common is that
 * they create a certain granularity of terms and they add tags to the terms
 * i.e. the Part-Of-Speech tagger adds POS tags, the noun phrase parser adds
 * nouns phrase tags and so on. Since they all adds tags, this interface is
 * called <code>DocumentTagger</code>.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentTagger {
    
    /**
     * Restructes, retokenizes and / or tags documents and returns a new
     * rebuild {@link org.knime.ext.textprocessing.data.Document} instance.
     * 
     * @param doc The <code>Document</code> to tag.
     * @return a new restructured, retokenized and tagged <code>Document</code> 
     * instance.
     */
    public Document tag(final Document doc);

}

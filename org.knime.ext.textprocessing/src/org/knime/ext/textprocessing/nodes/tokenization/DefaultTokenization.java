/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;



/**
 * Is a utility class which provides methods for the default tokenization of
 * {@link org.knime.ext.textprocessing.data.Document}s. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DefaultTokenization {
        
    private static final OpenNlpWordTokenizer WORD_TOKENIZER = 
        new OpenNlpWordTokenizer();
    
    private static final OpenNlpSentenceTokenizer SENTENCE_TOKENIZER = 
        new OpenNlpSentenceTokenizer();
    
    private DefaultTokenization() { }
    
    /**
     * @return The default sentence tokenizer.
     */
    public static final Tokenizer getSentenceTokenizer() {
        return SENTENCE_TOKENIZER;
    }

    /**
     * @return The default word tokenizer.
     */
    public static final Tokenizer getWordTokenizer() {
        return WORD_TOKENIZER; 
    }
}

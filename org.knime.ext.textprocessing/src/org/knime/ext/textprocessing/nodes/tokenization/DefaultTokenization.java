/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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

import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;



/**
 * Is a utility class which provides methods for the default tokenization of
 * {@link org.knime.ext.textprocessing.data.Document}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class DefaultTokenization {

    private static OpenNLPTokenizerPool tokenizerPool = new OpenNLPTokenizerPool(
        TextprocessingPreferenceInitializer.tokenizerPoolSize());

    private DefaultTokenization() { }

    /**
     * Creates new tokenizer pool with pool size set in preferences only if current pool size is different from
     * preference pool size.
     */
    public static final void createNewTokenizerPool() {
        final int newPoolSize = TextprocessingPreferenceInitializer.tokenizerPoolSize();
        if (newPoolSize != tokenizerPool.getPoolSize()) {
            tokenizerPool = new OpenNLPTokenizerPool(newPoolSize);
        }
    }

    /**
     * @return The default sentence tokenizer.
     */
    public static final Tokenizer getSentenceTokenizer() {
        return tokenizerPool.nextSentenceTokenizer();
    }

    /**
     * @return The default word tokenizer.
     */
    public static final Tokenizer getWordTokenizer() {
        return tokenizerPool.nextWordTokenizer();
    }
}

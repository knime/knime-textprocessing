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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.ArrayList;
import java.util.List;
import org.knime.ext.textprocessing.nodes.preprocessing.kuhlenstemmer.KuhlenStemmer;
import org.knime.ext.textprocessing.nodes.preprocessing.porterstemmer.PorterStemmer;


/**
 * The enum registers all provided stemming methods and enables a generic usage
 * of them.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated
 */
@Deprecated
public enum Stemming {

    /**
     * The Kuhlen Stemmer.
     */
    KUHLEN {
        /**
         * {@inheritDoc}
         */
        @Override
        public TermPreprocessing getPreprocessing() {
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
        public TermPreprocessing getPreprocessing() {
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
    public abstract TermPreprocessing getPreprocessing();

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

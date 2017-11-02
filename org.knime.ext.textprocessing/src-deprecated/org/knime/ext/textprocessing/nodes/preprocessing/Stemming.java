/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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

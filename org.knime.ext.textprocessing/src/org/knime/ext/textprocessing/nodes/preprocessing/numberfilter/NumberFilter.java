/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   11.05.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.numberfilter;

import java.util.regex.Pattern;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class NumberFilter implements TermPreprocessing, StringPreprocessing {
    /**
     * Constant for filtering mode that filters terms that represent numbers.
     */
    static final String FILTERINGMODE_TERM_REPRESENTS_NUMBER = "Filter terms representing numbers";

    /**
     * Constant for filtering mode that filters any terms that contain numbers.
     */
    static final String FILTERINGMODE_TERM_CONTAINS_NUMBER = "Filter terms containing numbers";

    /**
     * Constant for default filtering mode.
     */
    static final String DEF_FILTERINGMODE = FILTERINGMODE_TERM_REPRESENTS_NUMBER;

    // regex for terms that consist of numbers, decimal seperators and leading +-.
    private static final Pattern NUMBER_REGEX = Pattern.compile("^[-+]?(?:\\d*[.,]{1}\\d+|\\d)+");

    // regex for terms that contain at least one digit (e.g. "abc123")
    private static final Pattern NUMBER_REGEX_2 = Pattern.compile(".*\\d+.*");

    private static final String REPLACEMENT = "";

    private String m_filteringMode = DEF_FILTERINGMODE;

    private boolean m_filterTermsContainingNumbers = false;

    /**
     * Parameter-free constructor for NumberFilter. Creates an instance of NumberFilter that is using the default
     * filtering mode.
     * @since 3.4
     */
    public NumberFilter() {
        this(DEF_FILTERINGMODE);
    }

    /**
     * @param filterMode The name of the filtering mode.
     * @since 3.4
     */
    public NumberFilter(final String filterMode) {
        m_filteringMode = filterMode;
        checkFilteringMode();
    }

    /**
     * Filters strings depending on the used filtering mode. The default filtering mode filters terms that consists of
     * numbers, decimal separators and leading +- signs. The second filtering mode filters any terms that contains at
     * least on digit.
     * The filtered String is returned.
     *
     * @param str String to filter numbers from.
     * @return Filtered String.
     */
    private String numberFilter(final String str) {
        if (m_filterTermsContainingNumbers) {
            return NUMBER_REGEX_2.matcher(str).replaceAll(REPLACEMENT);
        } else {
            return NUMBER_REGEX.matcher(str).replaceAll(REPLACEMENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        String filtered = numberFilter(term.getText());
        if (filtered.length() <= 0) {
            return null;
        }
        return term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return numberFilter(str);
    }

    private void checkFilteringMode() {
        if (m_filteringMode.equals(FILTERINGMODE_TERM_CONTAINS_NUMBER)) {
            m_filterTermsContainingNumbers = true;
        } else {
            m_filterTermsContainingNumbers = false;
        }
    }
}

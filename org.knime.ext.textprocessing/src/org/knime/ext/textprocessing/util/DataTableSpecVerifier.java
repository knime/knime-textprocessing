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
 *   11.01.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.TermValue;

/**
 * Provides methods that verify various kinds of
 * {@link org.knime.core.data.DataTableSpec}s. These methods return
 * <code>false</code> or throw <code>InvalidSettingsException</code> if the
 * spec to verify is not valid accordant to a specified setting, i.e. it can
 * be verified if a spec contains at least one <code>DocumentCell</code>,
 * a <code>DocumentCell</code> and a <code>TermCell</code> and much more. With
 * these methods it can be verified conveniently, i.e. in a nodes configure
 * method, if a given spec fits the node needs.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DataTableSpecVerifier {

    private DataTableSpec m_spec;

    private int m_termCellIndex = -1;

    private int m_documentCellIndex = -1;

    private int m_stringCellIndex = -1;

    private int m_numberCellIndex = -1;

    private int m_numTermCells = 0;

    private int m_numDocumentCells = 0;

    private int m_numStringCells = 0;

    private int m_numNumberCells = 0;

    private String m_tokenizerWarningMsg = "";

    /**
     * Creates a new instance of <code>DataTableSpecVerifier</code> with given
     * <code>DataTableSpec</code> to verify.
     *
     * @param spec <code>DataTableSpec</code> to verify.
     */
    public DataTableSpecVerifier(final DataTableSpec spec) {
        m_spec = spec;
        readDataTable();
    }

    /**
     * Counts the number of specific cells and stores their indices.
     */
    private void readDataTable() {
        for (int i = 0; i < m_spec.getNumColumns(); i++) {

            if (m_spec.getColumnSpec(i).getType().isCompatible(
                    DocumentValue.class)) {
                m_numDocumentCells++;
                m_documentCellIndex = i;
            } else if (m_spec.getColumnSpec(i).getType().isCompatible(
                    TermValue.class)) {
                m_numTermCells++;
                m_termCellIndex = i;
            } else if (m_spec.getColumnSpec(i).getType().isCompatible(
                    StringValue.class)) {
                m_numStringCells++;
                m_stringCellIndex = i;
            } else if (m_spec.getColumnSpec(i).getType().isCompatible(
                    DoubleValue.class)) {
                m_numberCellIndex = i;
                m_numNumberCells++;
            }

        }
    }

    /**
     * @return the term cell index.
     */
    public int getTermCellIndex() {
        return m_termCellIndex;
    }

    /**
     * @return the document cell index.
     */
    public int getDocumentCellIndex() {
        return m_documentCellIndex;
    }

    /**
     * @return the string cell index.
     */
    public int getStringCellIndex() {
        return m_stringCellIndex;
    }

    /**
     * @return the number cell index.
     */
    public int getNumberCellIndex() {
        return m_numberCellIndex;
    }

    /**
     * @return the number of term cells.
     */
    public int getNumTermCells() {
        return m_numTermCells;
    }

    /**
     * @return the number of document cells.
     */
    public int getNumDocumentCells() {
        return m_numDocumentCells;
    }

    /**
     * @return the number of string cells.
     */
    public int getNumberStringCells() {
        return m_numStringCells;
    }

    /**
     * @return the number of number cells cells.
     */
    public int getNumberNumberCells() {
        return m_numNumberCells;
    }

    private void throwException(final boolean throwException, final String msg)
            throws InvalidSettingsException {
        if (throwException) {
            throw new InvalidSettingsException(msg);
        }
    }

    /**
     * Verifies tokenizer of first document column against the given tokenizer name. Returns {@code true}
     * if tokenization is equal, otherwise {@code false}.
     * @param tokenizerName Tokenizer name to test.
     * @return {@code true} if tokenization is equal, otherwise {@code false}.
     * @since 3.3
     */
    public boolean verifyTokenizer(final String tokenizerName) {
        return verifyTokenizer(getDocumentCellIndex(), tokenizerName);
    }

    /**
     * Verifies tokenization of document column of specified index against the given tokenizer name. Returns {@code true}
     * if tokenization is equal, otherwise {@code false}.
     * @param docColIndex Index of document column to test.
     * @param tokenizerName Tokenizer name to test.
     * @return {@code true} if tokenization is equal, otherwise {@code false}.
     * @since 3.3
     */
    public boolean verifyTokenizer(final int docColIndex, final String tokenizerName) {
        String tokenizer = getTokenizerFromInputDocCol(docColIndex);
        if (tokenizer != null) {
            if (!tokenizer.equals(tokenizerName)) {
                m_tokenizerWarningMsg = "Tokenization of input documents (" + tokenizer
                    + ") differs to selected tokenization (" + tokenizerName + ").";
                return false;
            }
        }
        m_tokenizerWarningMsg = "";
        return true;
    }

    /**
     * @return The warning message if tokenizer could not be verified.
     * @since 3.3
     */
    public String getTokenizerWarningMsg() {
        return m_tokenizerWarningMsg;
    }

    /**
     * @param docColIndex The document column index to get the tokenizer name from.
     * @return The tokenizer used in the input document column or null if index is invalid.
     * @since 3.3
     */
    public String getTokenizerFromInputDocCol(final int docColIndex) {
        if (docColIndex >= 0) {
            DataColumnSpec docColSpec = m_spec.getColumnSpec(docColIndex);
            return docColSpec.getProperties().getProperty(DocumentDataTableBuilder.WORD_TOKENIZER_KEY);
        }
        return null;
    }


    /**
     * Verifies the <code>DataTableSpec</code> and checks if it has exactly
     * one column containing <code>DocumentCell</code>s. If so, true is
     * returned otherwise false.
     *
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> has at least one
     * column containing <code>DocumentCell</code>.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> has
     * less or more than one column containing <code>DocumentCell</code>s.
     */
    public boolean verifyDocumentCell(final boolean throwException)
            throws InvalidSettingsException {
        boolean valid = true;

        if (m_numDocumentCells < 1) {
            valid = false;
            throwException(throwException,
                    "No column with DocumentCells found !");
        } else if (m_numDocumentCells > 1) {
            valid = false;
            throwException(throwException,
                    "Only one column containing DocumentCells allowed !");
        }

        return valid;
    }


    /**
     * Verifies the <code>DataTableSpec</code> and checks if it has exactly
     * one column containing <code>StringCell</code>s. If so, true is
     * returned otherwise false.
     *
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> has at least one
     * column containing <code>StringCell</code>.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> has
     * less or more than one column containing <code>StringCell</code>s.
     */
    public boolean verifyStringCell(final boolean throwException)
            throws InvalidSettingsException {
        boolean valid = true;

        if (m_numStringCells < 1) {
            valid = false;
            throwException(throwException,
                    "No column with StringCells found !");
        } else if (m_numStringCells > 1) {
            valid = false;
            throwException(throwException,
                    "Only one containing StringCells allowed !");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains a
     * certain number of column containing <code>StringCell</code>s specified
     * by the given parameter noStringCells. If so, true is returned, if not an
     * <code>InvalidSettingsException</code> is thrown.
     *
     * @param noStringCells The number of columns containing
     * <code>StringCell</code>s to check for.
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains the specified number
     * of column with <code>StringCell</code>s.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * more or less than the specified number of columns
     * with <code>StringCell</code>.
     */
    public boolean verifyStringCells(final int noStringCells,
            final boolean throwException) throws InvalidSettingsException {
        boolean valid = true;

        if (m_numStringCells < noStringCells) {
            valid = false;
            throwException(throwException, "No " + noStringCells
                    + " columns with StringCells found !");
        } else if (m_numStringCells > noStringCells) {
            valid = false;
            throwException(throwException, "Only " + noStringCells
                    + " columns containing StringCell allowed !");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains at
     * least the given number minimumStringCells of columns containing
     * <code>StringCell</code>s. If so, true is returned, if not an
     * <code>InvalidSettingsException</code> is thrown.
     *
     * @param minimumStringCells The number of minimum columns containing
     * <code>StringCell</code>s to check for.
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains at least
     * the given number of columns containing <code>StringCell</code>s.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * less than the specified number minimumStringCells of
     * columns containing <code>StringCell</code>s.
     */
    public boolean verifyMinimumStringCells(final int minimumStringCells,
            final boolean throwException) throws InvalidSettingsException {
        boolean valid = true;

        if (m_numStringCells < minimumStringCells) {
            valid = false;
            throwException(throwException, "There have to be at least "
                    + minimumStringCells + " columns containing StringCells !");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains at
     * least the given number minimumDocCells of columns containing
     * <code>DocumentCell</code>s. If so, true is returned, if not an
     * <code>InvalidSettingsException</code> is thrown.
     *
     * @param minimumDocCells The number of minimum columns containing
     * <code>DocumentCell</code>s to check for.
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains at least
     * the given number of columns containing <code>DocumentCell</code>s.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * less than the specified number minimumDocCells of
     * columns containing <code>DocumentCell</code>s.
     */
    public boolean verifyMinimumDocumentCells(final int minimumDocCells,
            final boolean throwException) throws InvalidSettingsException {
        boolean valid = true;

        if (m_numDocumentCells < minimumDocCells) {
            valid = false;
            throwException(throwException, "There have to be at least "
                    + minimumDocCells + " columns containing DocumentCells !");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains at
     * least the given number minimumTermCells of columns containing
     * <code>TermCell</code>s. If so, true is returned, if not an
     * <code>InvalidSettingsException</code> is thrown.
     *
     * @param minimumTermCells The number of minimum columns containing
     * <code>TermCell</code>s to check for.
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains at least
     * the given number of columns containing <code>TermCell</code>s.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * less than the specified number minimumTermCells of
     * columns containing <code>TermCell</code>s.
     */
    public boolean verifyMinimumTermCells(final int minimumTermCells,
            final boolean throwException) throws InvalidSettingsException {
        boolean valid = true;

        if (m_numTermCells < minimumTermCells) {
            valid = false;
            throwException(throwException, "There have to be at least "
                    + minimumTermCells + " columns containing TermCells !");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains at
     * least the given number minimumNumberCells of columns containing
     * number cells. If so, true is returned, if not an
     * <code>InvalidSettingsException</code> is thrown.
     *
     * @param minimumNumberCells The number of minimum columns containing
     * number cells to check for.
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains at least
     * the given number of columns containing number cells.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * less than the specified number minimumNumberCells of
     * columns containing number cells.
     */
    public boolean verifyMinimumNumberCells(final int minimumNumberCells,
            final boolean throwException) throws InvalidSettingsException {
        boolean valid = true;

        if (m_numNumberCells < minimumNumberCells) {
            valid = false;
            throwException(throwException, "There have to be at least "
                    + minimumNumberCells + " columns containing number cells!");
        }
        return valid;
    }

    /**
     * Verifies the <code>DataTableSpec</code> and checks if it contains
     * one columns with <code>TermCell</code>s only. If so,
     * true is returned, if not an <code>InvalidSettingsException</code> is
     * thrown.
     *
     * @param throwException If true an exception is throw in case of an error,
     * if false just false is returned.
     * @return true if <code>DataTableSpec</code> contains one column with
     * <code>TermCell</code>s only.
     * @throws InvalidSettingsException If <code>DataTableSpec</code> contains
     * more or less than one column containing <code>TermCell</code>s.
     */
    public boolean verifyTermCell(final boolean throwException)
            throws InvalidSettingsException {
        boolean valid = true;

        if (m_numTermCells < 1) {
            valid = false;
            throwException(throwException,
                    "No column containing TermCells found !");
        } else if (m_numTermCells > 1) {
            valid = false;
            throwException(throwException,
                    "Only one column conmtaining TermCells allowed !");
        }

        return valid;
    }
}

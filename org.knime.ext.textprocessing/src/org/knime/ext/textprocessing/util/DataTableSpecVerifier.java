/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   11.01.2007 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.TermValue;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DataTableSpecVerifier {

    private DataTableSpec m_spec;

    private int m_termCellIndex = -1;

    private int m_documentCellIndex = -1;

    private int m_stringCellIndex = -1;

    private int m_numTermCells = 0;

    private int m_numDocumentCells = 0;

    private int m_numStringCells = 0;

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
     * ounts the number of specific cells and stores their indices.
     */
    private void readDataTable() {
        for (int i = 0; i < m_spec.getNumColumns(); i++) {

            if (m_spec.getColumnSpec(i).getType().isCompatible(
                    DocumentValue.class)) {
                m_numDocumentCells++;
                m_documentCellIndex = i;

            } else if (m_spec.getColumnSpec(i).getType().isCompatible(
                    StringValue.class)) {
                m_numStringCells++;
                m_stringCellIndex = i;
            } else if (m_spec.getColumnSpec(i).getType().isCompatible(
                    TermValue.class)) {
                m_numTermCells++;
                m_termCellIndex = i;
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

    private void throwException(final boolean throwException, final String msg)
            throws InvalidSettingsException {
        if (throwException) {
            throw new InvalidSettingsException(msg);
        }
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
            throwException(throwException, "No column with StringCells found !");
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

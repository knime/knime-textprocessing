/*
 * ------------------------------------------------------------------------
 *
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
 *   24.10.2017 (Julian): created
 */
package org.knime.ext.textprocessing.util;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This class provides methods that can be used within configure() methods of nodes to streamline their behaviour in
 * terms of column.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.5
 */
public class ColumnSelectionVerifier {

    private String m_warningMessage;

    private boolean m_warningExists;

    /**
     * Creates and returns a new instance of a {@code ColumnSelectionVerifier}. The ColumnSelectionVerifier checks if
     * the {@link DataTableSpec} contains a column which name is contained within the {@code SettingsModelString}.
     * Additionally, it checks for the correct column type (e.g. DocumentValue.class) which should be passed as an
     * argument as well. If the SettingsModelString contains {@code null} or an empty string, the
     * ColumnSelectionVerifier automatically sets the first suitable column name. Empty strings or {@code null} should
     * only occur if the node is initialized for the first time and no column selection has been done within the node
     * dialog.
     *
     * @param columnSetting The {@code SettingsModelString} containing the name of the column to verify.
     * @param spec The {@code DataTableSpec} containing the column information.
     * @param columnType The specific {@code DataValue} implementation to verify the column.
     * @throws InvalidSettingsException Throws an InvalidSettingsException, if the column does not exist or if the
     *             column has a wrong type.
     */
    public ColumnSelectionVerifier(final SettingsModelString columnSetting, final DataTableSpec spec,
        final Class<? extends DataValue> columnType) throws InvalidSettingsException {
        verifyColumn(columnSetting, spec, columnType, null);
    }

    /**
     * Creates and returns a new instance of a {@code ColumnSelectionVerifier}. The ColumnSelectionVerifier checks if
     * the {@link DataTableSpec} contains a column which name is contained within the {@code SettingsModelString}.
     * Additionally, it checks for the correct column type (e.g. DocumentValue.class) which should be passed as an
     * argument as well. If the SettingsModelString contains {@code null} or an empty string, the
     * ColumnSelectionVerifier automatically sets the first suitable column name. Empty strings or {@code null} should
     * only occur if the node is initialized for the first time and no column selection has been done within the node
     * dialog.
     *
     * @param columnSetting The {@code SettingsModelString} containing the name of the column to verify.
     * @param spec The {@code DataTableSpec} containing the column information.
     * @param columnType The specific {@code DataValue} implementation to verify the column.
     * @param ignoreName If this value is not null, the Verifier will ignore any column with the same name during the
     *            search.
     * @throws InvalidSettingsException Throws an InvalidSettingsException, if the column does not exist or if the
     *             column has a wrong type.
     */
    public ColumnSelectionVerifier(final SettingsModelString columnSetting, final DataTableSpec spec,
        final Class<? extends DataValue> columnType, final String ignoreName) throws InvalidSettingsException {
        verifyColumn(columnSetting, spec, columnType, ignoreName);
    }

    private void verifyColumn(final SettingsModelString columnSetting, final DataTableSpec spec,
        final Class<? extends DataValue> columnType, final String ignoreName) throws InvalidSettingsException {
        String colType = StringUtils.remove(columnType.getSimpleName(), "Value");

        // if document column setting is empty take first feasible column from datatable
        if (columnSetting.getStringValue() == null || columnSetting.getStringValue().isEmpty()) {
            for (DataColumnSpec column : spec) {
                if ((column.getType().isCompatible(columnType)) &&
                        (ignoreName != null ? !column.getName().equals(ignoreName) : true)) {
                    columnSetting.setStringValue(column.getName());
                    m_warningExists = true;
                    m_warningMessage =
                        "Auto guessing: Using column '" + column.getName() + "' as " + colType + " column.";
                    break;
                }
            }
        } else if (spec.findColumnIndex(columnSetting.getStringValue()) < 0) {
            // if document column is set but the column does not exist in data table throw exception
            throw new InvalidSettingsException(
                colType + " column '" + columnSetting.getStringValue() + "' could not be found in data table.");
        } else if (!spec.getColumnSpec(columnSetting.getStringValue()).getType().isCompatible(columnType)) {
            // if document column is set and the column exists but is not of right type throw exception
            throw new InvalidSettingsException(
                "Column '" + columnSetting.getStringValue() + "' is not a " + colType + " column.");
        }
    }

    /**
     * Returns a String containing the warning message.
     *
     * @return Returns the warning message.
     */
    public String getWarningMessage() {
        return m_warningMessage;
    }

    /**
     * Returns true if a warning message exists and false if not.
     *
     * @return Returns true if a warning message exists.
     */
    public boolean hasWarningMessage() {
        return m_warningExists;
    }

}

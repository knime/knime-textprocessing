/*
 * ------------------------------------------------------------------------
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Feb 8, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

/**
 * Class to store results from relation extraction.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class ExtractionResult {

    /**
     * The subject.
     */
    private final String m_subject;

    /**
     * The relation/predicate.
     */
    private final String m_relation;

    /**
     * The object.
     */
    private final String m_object;

    /**
     * The confidence.
     */
    private final Double m_confidence;

    /**
     * Creates a new instance of {@code ExtractionResult} containing the results from relation extraction.
     *
     * @param subject The subject.
     * @param relation The predicate.
     * @param object The object.
     * @param confidence The confidence.
     */
    public ExtractionResult(final String subject, final String relation, final String object, final Double confidence) {
        m_subject = subject;
        m_relation = relation;
        m_object = object;
        m_confidence = confidence;
    }

    /**
     * True if all results are null.
     *
     * @return Returns true, if all results are null.
     */
    private final boolean isEmpty() {
        return (m_subject == null) && (m_relation == null) && (m_object == null) && (m_confidence == null);
    }

    /**
     * Creates and returns a list of {@link DataCell DataCells} based on extraction results.
     *
     * @return List of {@code DataCells}
     */
    List<DataCell> asDataCells() {
        final List<DataCell> dataCells = new ArrayList<>();
        if (!isEmpty()) {
            dataCells.add(new StringCell(m_subject));
            dataCells.add(new StringCell(m_relation));
            dataCells.add(new StringCell(m_object));
            dataCells.add(new DoubleCell(m_confidence));
        } else {
            dataCells.addAll(Arrays.asList(DataType.getMissingCell(), DataType.getMissingCell(),
                DataType.getMissingCell(), DataType.getMissingCell()));
        }
        return dataCells;
    }

    /**
     * Returns an {@code ExtractionResult} without any data.
     *
     * @return A new instance of {@code ExtractionResult} without any data.
     */
    public static final ExtractionResult getEmptyResult() {
        return new ExtractionResult(null, null, null, null);
    }
}

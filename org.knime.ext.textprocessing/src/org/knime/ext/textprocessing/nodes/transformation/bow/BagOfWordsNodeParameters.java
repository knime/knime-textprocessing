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
 *  prepare and propagate Nodes, in each case even if such Node is
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.transformation.bow;

import org.knime.core.data.DataColumnSpec;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.CommonColumnNames;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.persistence.legacy.LegacyColumnFilterPersistor;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.filter.ColumnFilter;
import org.knime.node.parameters.widget.choices.util.AllColumnsProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;

/**
 * Node parameters for Bag Of Words Creator.
 *
 * @author Robin Gerling, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class BagOfWordsNodeParameters implements NodeParameters {

    BagOfWordsNodeParameters() {
    }

    // This constructor currently has no effect as the default values for the documentColumn/columnFilter are set in the
    // model. When this node uses the default node API, it will have an effect.
    BagOfWordsNodeParameters(final NodeParametersInput input) {
        m_documentColumn = ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(input, DocumentValue.class)
            .map(DataColumnSpec::getName).orElse("");
        m_columnFilter =
            new ColumnFilter(ColumnSelectionUtil.getAllColumnsOfFirstPort(input)).withIncludeUnknownColumns();
    }

    @Persist(configKey = BagOfWordsConfigKeys2.CFG_KEY_DOCUMENT_COL)
    @Widget(title = "Document column",
        description = "Select the document column that is used for creating the bag of words.")
    @ChoicesProvider(DocumentColumnsProvider.class)
    String m_documentColumn = "";

    @Persist(configKey = BagOfWordsConfigKeys2.CFG_KEY_TERM_COL)
    @Widget(title = "Term column", description = "The name of the term column to be created.")
    String m_termColumn = CommonColumnNames.DEF_TERM_COLNAME;

    @Persistor(ColumnFilterLegacyPersistor.class)
    @Widget(title = "Column selection",
        description = "Selected columns will be copied to the output table."
            + " Columns that are not selected will not appear in the output table.")
    @ChoicesProvider(AllColumnsProvider.class)
    ColumnFilter m_columnFilter = new ColumnFilter().withIncludeUnknownColumns();

    static final class DocumentColumnsProvider extends CompatibleColumnsProvider {
        protected DocumentColumnsProvider() {
            super(DocumentValue.class);
        }
    }

    static final class ColumnFilterLegacyPersistor extends LegacyColumnFilterPersistor {
        ColumnFilterLegacyPersistor() {
            super(BagOfWordsConfigKeys2.CFG_KEY_COLUMN_FILTER);
        }
    }
}

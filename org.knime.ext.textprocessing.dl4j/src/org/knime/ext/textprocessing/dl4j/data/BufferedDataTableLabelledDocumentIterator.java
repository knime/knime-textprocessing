/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.data;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * {@link LabelAwareIterator} for a {@link BufferedDataTable}. Expects a column contained in the data table holding one
 * document and one label per row.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class BufferedDataTableLabelledDocumentIterator implements LabelAwareIterator {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(BufferedDataTableLabelledDocumentIterator.class);

    private final BufferedDataTable m_table;

    private CloseableRowIterator m_tableIterator;

    private final int m_documentColumnIndex;

    private final int m_labelColumnIndex;

    private final LabelsSource m_labelsSource;

    /**
     * Constructor for class BufferedDataTableLabelledDocumentIterator.
     *
     * @param table the table to iterate
     * @param documentColumnName the name of the document column
     * @param labelColumnName the name of the label column
     */
    public BufferedDataTableLabelledDocumentIterator(final BufferedDataTable table, final String documentColumnName,
        final String labelColumnName) {
        this.m_table = table;
        this.m_documentColumnIndex = table.getSpec().findColumnIndex(documentColumnName);
        this.m_labelColumnIndex = table.getSpec().findColumnIndex(labelColumnName);
        this.m_tableIterator = table.iterator();
        this.m_labelsSource = initLabelsSource();
        this.reset();
    }

    @Override
    public boolean hasNextDocument() {
        return m_tableIterator.hasNext();
    }

    /**
     * Returns the next {@link LabelledDocument} containing a document and a corresponding label from the
     * {@link BufferedDataTable}.
     *
     * @return
     */
    @Override
    public LabelledDocument nextDocument() {
        final DataRow row = m_tableIterator.next();
        final DataCell documentCell = row.getCell(m_documentColumnIndex);
        final DataCell labelCell = row.getCell(m_labelColumnIndex);

        String documentContent = null;
        String documentLabel = null;
        try {
            /*
             * Can't use converter for documents because the getStringValue()
             * method used for conversion to String returns the document title
             * and no the content
             */
            if (documentCell.getType().isCompatible(DocumentValue.class)) {
                final DocumentValue dCell = (DocumentValue)documentCell;
                documentContent = dCell.getDocument().getDocumentBodyText();
            } else if (documentCell.getType().isCompatible(StringValue.class)) {
                final StringCell sCell = (StringCell)documentCell;
                documentContent = sCell.getStringValue();
            }

            documentLabel = ConverterUtils.convertDataCellToJava(labelCell, String.class);
        } catch (final Exception e) {
            logger.coding("Problem with input conversion", e);
        }

        final LabelledDocument output = new LabelledDocument();
        output.setContent(documentContent);
        output.setLabel(documentLabel);

        return output;
    }

    @Override
    public void reset() {
        m_tableIterator.close();
        m_tableIterator = m_table.iterator();
    }

    @Override
    public LabelsSource getLabelsSource() {
        return m_labelsSource;
    }

    /**
     * Iterates over {@link BufferedDataTable} and collects all labels.
     *
     * @return {@link LabelsSource} containing the collected labels.
     */
    private LabelsSource initLabelsSource() {
        final List<String> labels = new ArrayList<>();
        while (m_tableIterator.hasNext()) {
            final DataCell labelCell = m_tableIterator.next().getCell(m_labelColumnIndex);

            try {
                labels.add(ConverterUtils.convertDataCellToJava(labelCell, String.class));
            } catch (final Exception e) {
                logger.coding("Problem with input conversion", e);
            }
        }
        return new LabelsSource(labels);
    }
}

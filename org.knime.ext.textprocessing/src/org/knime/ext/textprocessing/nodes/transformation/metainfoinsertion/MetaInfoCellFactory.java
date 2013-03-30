package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 */
public class MetaInfoCellFactory extends SingleCellFactory {

    private int m_docColIndx;
    private int m_keyColIndx;
    private int m_valueColIndx;
    private TextContainerDataCellFactory m_documentCellFac;

    /**
     * Constructor of {@link MetaInfoCellFactory} with given indices of document, key and value columns to set.
     * @param docColSpec The data column spec of the document column.
     * @param docColIndx The index of the column containing the documents.
     * @param keyColIndx The index of the column containing the keys.
     * @param valueColIndx The index of the column containing the values.
     */
    public MetaInfoCellFactory(final DataColumnSpec docColSpec, final int docColIndx, final int keyColIndx,
                               final int valueColIndx) {
        super(true, docColSpec);

        m_docColIndx = docColIndx;
        m_keyColIndx = keyColIndx;
        m_valueColIndx = valueColIndx;
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
    }

    /* (non-Javadoc)
     * @see org.knime.core.data.container.SingleCellFactory#getCell(org.knime.core.data.DataRow)
     */
    @Override
    public DataCell getCell(final DataRow row) {
        DataCell newCell = DataType.getMissingCell();
        if (!row.getCell(m_docColIndx).isMissing() && !row.getCell(m_keyColIndx).isMissing()
                && !row.getCell(m_valueColIndx).isMissing()) {
            Document d = ((DocumentValue)row.getCell(m_docColIndx)).getDocument();
            String key = ((StringValue)row.getCell(m_keyColIndx)).getStringValue();
            String value = ((StringValue)row.getCell(m_valueColIndx)).getStringValue();

            DocumentBuilder db = new DocumentBuilder(d);
            db.setSections(d.getSections());
            db.addMetaInformation(key, value);
            Document newDoc = db.createDocument();

            newCell = m_documentCellFac.createDataCell(newDoc);
        }
        return newCell;
    }
}

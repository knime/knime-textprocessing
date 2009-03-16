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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The model of the document vector node, creates a document feature vector
 * for each document. As features all term of the given bag of words are used.
 * As vector values, a column can be specified or bit vectors can be created.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentVectorNodeModel extends NodeModel {

    /**
     * The default setting of the creation of bit vectors. By default bit 
     * vectors are created (<code>true</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = true;
    
    /**
     * The default column to use.
     */
    public static final String DEFAULT_COL = "";
    
    /**
     * The default document column to use.
     */
    public static final String DEFAULT_DOCUMENT_COLNAME = 
        BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME;
    
    /**
     * The default value to ignore tags.
     */
    public static final boolean DEFAULT_IGNORE_TAGS = true;
    
    
    private final TextContainerDataCellFactory m_documentCellFac;
    
    private int m_documentColIndex = -1;
    
    private int m_termColIndex = -1;    
    
    private SettingsModelString m_colModel = 
        DocumentVectorNodeDialog.getColumnModel();
    
    private SettingsModelBoolean m_booleanModel = 
        DocumentVectorNodeDialog.getBooleanModel();
    
    private SettingsModelString m_docuColModel = 
        DocumentVectorNodeDialog.getDocumentColModel();
    
    private SettingsModelBoolean m_ignoreTags = 
        DocumentVectorNodeDialog.getIgnoreTagsModel();
    

    /**
     * Creates a new instance of <code>DocumentVectorNodeModel</code>. 
     */
    public DocumentVectorNodeModel() {
        super(1, 1);
        m_documentCellFac =
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();        
        m_booleanModel.addChangeListener(new InternalChangeListener());
        checkUncheck();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{null};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
        boolean ignoreTags = m_ignoreTags.getBooleanValue();
        
        // document column index.
        m_documentColIndex = inData[0].getSpec().findColumnIndex(
                m_docuColModel.getStringValue());
        
        int colIndex = -1;
        // Check if no valid column selected, the use of boolean values is
        // specified !
        if (!m_booleanModel.getBooleanValue()) {
            String colName = m_colModel.getStringValue();
            colIndex = inData[0].getDataTableSpec().findColumnIndex(colName);
            if (colIndex < 0) {
                throw new InvalidSettingsException("No valid column selected!");
            }
        }
        
        // Sort the data table first by documents
        List<String> colList = new ArrayList<String>();
        colList.add(m_docuColModel.getStringValue());
        boolean [] sortAsc = new boolean[]{true};
        BufferedDataTable sortedTable = new SortedTable(inData[0], colList, 
                sortAsc, exec).getBufferedDataTable();
        
        // hash table holding an index for each feature
        Hashtable<String, Integer> featureIndexTable = 
            new Hashtable<String, Integer>();
        
        // first go through data table to collect the features
        int currIndex = 0;
        RowIterator it = sortedTable.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Term t = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
            
            String key = null;
            // if tags have o be ignored
            if (ignoreTags) {
                key = t.getText();
            } else {
                key = t.toString();
            }
            if (key != null && !featureIndexTable.containsKey(key)) {
                featureIndexTable.put(key, currIndex);
                currIndex++;
            }
        }
        
        // second go through data table to create feature vectors
        BufferedDataContainer dc =
            exec.createDataContainer(createDataTableSpec(featureIndexTable));
        
        Document lastDoc = null;
        List<Double> featureVector = initFeatureVector(
                featureIndexTable.size());
        
        int numberOfRows = sortedTable.getRowCount();
        int currRow = 1;
        it = sortedTable.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Document currDoc = ((DocumentValue)row.getCell(m_documentColIndex))
                                .getDocument();
            Term currTerm = ((TermValue)row.getCell(m_termColIndex))
                                .getTermValue();
            double currValue = 1;
            if (colIndex > -1) {
                currValue = ((DoubleValue)row.getCell(colIndex))
                                .getDoubleValue();
            }
                        
            // if current doc is not equals last doc, create new feature vector 
            // for last doc
            if (lastDoc != null && !currDoc.equals(lastDoc)) {
                // add old feature vector to table
                dc.addRowToTable(createDataRow(lastDoc, featureVector));
                // create new feature vector
                featureVector = initFeatureVector(featureIndexTable.size());
            }
            
            
            // add new term at certain index to feature vector
            String key = "";
            // if tags have o be ignored
            if (ignoreTags) {
                key = currTerm.getText();
            } else {
                key = currTerm.toString();
            }
            int index = featureIndexTable.get(key);
            featureVector.set(index, currValue);
            
            // if last row, add feature vector to table
            if (currRow == numberOfRows) {
                dc.addRowToTable(createDataRow(lastDoc, featureVector));
            }
            
            lastDoc = currDoc;
            currRow++;
        }
        
        dc.close();
        featureIndexTable.clear();
        
        return new BufferedDataTable[]{dc.getTable()};
    }

    
    private int m_rowKeyNr = 1;
    
    private DataRow createDataRow(final Document doc, 
            final List<Double> featureVector) {
        DataCell[] cells = new DataCell[featureVector.size() + 1];
        cells[0] = m_documentCellFac.createDataCell(doc); 
        for (int i = 0; i < cells.length - 1; i++) {
            cells[i + 1] = new DoubleCell(featureVector.get(i)); 
        }
        
        RowKey rowKey = new RowKey(new Integer(m_rowKeyNr).toString());
        m_rowKeyNr++;
        DataRow newRow = new DefaultRow(rowKey, cells);      
        
        return newRow;
    }
    
    private DataTableSpec createDataTableSpec(
            final Hashtable<String, Integer> featureIndexTable) {
        int featureCount = featureIndexTable.size();
        DataColumnSpec[] columnSpecs = new DataColumnSpec[featureCount + 1];
        
        // add document column
        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(
                    DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME, 
                    m_documentCellFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();        
        
        // add feature vector columns
        Set<String> terms = featureIndexTable.keySet();
        for (String t : terms) {
            int index = featureIndexTable.get(t) + 1;
            columnSpecCreator = new DataColumnSpecCreator(t, DoubleCell.TYPE);
            columnSpecs[index] = columnSpecCreator.createSpec();
        }

        return new DataTableSpec(columnSpecs);
    }    
    
    private List<Double> initFeatureVector(int size) {
        List<Double> featureVector = new ArrayList<Double>(size);
        for (int i = 0; i < size; i++) {
            featureVector.add(i, 0.0);
        }
        return featureVector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_booleanModel.loadSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_docuColModel.loadSettingsFrom(settings);
        m_ignoreTags.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_booleanModel.saveSettingsTo(settings);
        m_docuColModel.saveSettingsTo(settings);
        m_ignoreTags.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_booleanModel.validateSettings(settings);
        m_docuColModel.validateSettings(settings);
        m_ignoreTags.validateSettings(settings);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }    
    
    
    private void checkUncheck() {
        if (m_booleanModel.getBooleanValue()) {
            m_colModel.setEnabled(false);
        } else {
            m_colModel.setEnabled(true);
        }
    }
    
    /**
     * 
     * @author Kilian Thiel, University of Konstanz
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        public void stateChanged(final ChangeEvent e) {
            checkUncheck();
        }
    }    
}

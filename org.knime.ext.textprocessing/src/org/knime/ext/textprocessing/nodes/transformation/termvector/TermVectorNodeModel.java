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
package org.knime.ext.textprocessing.nodes.transformation.termvector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The model of the document vector node, creates a document feature vector
 * for each document. As features all term of the given bag of words are used.
 * As vector values, a column can be specified or bit vectors can be created.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermVectorNodeModel extends NodeModel {

    /**
     * The default setting of the creation of bit vectors. By default bit 
     * vectors are created (<code>true</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = true;
    
    /**
     * The default column to use.
     */
    public static final String DEFAULT_COL = "";
    
    private int m_documentColIndex = -1;
    
    private int m_termColIndex = -1;    
    
    private SettingsModelString m_colModel = 
        TermVectorNodeDialog.getColumnModel();
    
    private SettingsModelBoolean m_booleanModel = 
        TermVectorNodeDialog.getBooleanModel();
    

    /**
     * Creates a new instance of <code>TermVectorNodeModel</code>. 
     */
    public TermVectorNodeModel() {
        super(1, 1);
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
        verifier.verifyDocumentCell(true);
        verifier.verifyTermCell(true);
        m_documentColIndex = verifier.getDocumentCellIndex();
        m_termColIndex = verifier.getTermCellIndex();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
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
        
        // Sort the data table first by term
        List<String> colList = new ArrayList<String>();
        colList.add(inData[0].getDataTableSpec().getColumnSpec(
                m_termColIndex).getName());
        boolean [] sortAsc = new boolean[colList.size()];
        sortAsc[0] = true;
        SortedTable sortedTable = new SortedTable(inData[0], colList, sortAsc, 
                exec);
        
        // hash table holding an index for each feature
        Hashtable<Document, Integer> featureIndexTable = 
            new Hashtable<Document, Integer>();
        
        // first go through data table to collect the features
        int currIndex = 0;
        RowIterator it = sortedTable.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Document d = ((DocumentValue)row.getCell(m_documentColIndex))
                        .getDocument();
            if (!featureIndexTable.containsKey(d)) {
                featureIndexTable.put(d, currIndex);
                currIndex++;
            }
        }
        
        // second go through data table to create feature vectors
        BufferedDataContainer dc =
            exec.createDataContainer(createDataTableSpec(featureIndexTable));
        
        Term lastTerm = null;
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
                        
            // if current term is not equals last term, create new feature 
            // vector for last term
            if (lastTerm != null && !currTerm.equals(lastTerm)) {
                // add old feature vector to table
                dc.addRowToTable(createDataRow(lastTerm, featureVector));
                // create new feature vector
                featureVector = initFeatureVector(featureIndexTable.size());
            }
            // add new document at certain index to feature vector
            int index = featureIndexTable.get(currDoc);
            featureVector.set(index, currValue);
            
            // if last row, add feature vector to table
            if (currRow == numberOfRows) {
                dc.addRowToTable(createDataRow(lastTerm, featureVector));
            }
            
            lastTerm = currTerm;
            currRow++;
        }
        
        dc.close();
        featureIndexTable.clear();
        
        return new BufferedDataTable[]{dc.getTable()};
    }

    
    private int m_rowKeyNr = 1;
    
    private DataRow createDataRow(final Term term, 
            final List<Double> featureVector) {
        DataCell[] cells = new DataCell[featureVector.size() + 1];
        cells[0] = new TermCell(term); 
        for (int i = 0; i < cells.length - 1; i++) {
            cells[i + 1] = new DoubleCell(featureVector.get(i)); 
        }
        
        RowKey rowKey = new RowKey(new Integer(m_rowKeyNr).toString());
        m_rowKeyNr++;
        DataRow newRow = new DefaultRow(rowKey, cells);      
        
        return newRow;
    }
    
    private DataTableSpec createDataTableSpec(
            final Hashtable<Document, Integer> featureIndexTable) {
        Hashtable<String, Integer> columnTitles = 
            new Hashtable<String, Integer>();
        
        int featureCount = featureIndexTable.size();
        DataColumnSpec[] columnSpecs = new DataColumnSpec[featureCount + 1];
        
        // add document column
        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator("Term", TermCell.TYPE);
        columnSpecs[0] = columnSpecCreator.createSpec();
        
        // add feature vector columns
        Set<Document> documents = featureIndexTable.keySet();
        for (Document d : documents) {
            int index = featureIndexTable.get(d) + 1;
            
            // avoid duplicate titles by adding numbers if titles are equal.
            String origTitle = d.getTitle();
            String title = origTitle;
            Integer count = columnTitles.get(origTitle);
            // if title is used the first time initialize the count value with 1
            if (count == null || count < 1) {
                count = 1;
                columnTitles.put(origTitle, count);
            }
            // if title occurres another time, add the count value
            if (count >= 1) {
                count++;
                title += " - #" + count;
                columnTitles.put(origTitle, count);
            }
            
            columnSpecCreator = new DataColumnSpecCreator(title, 
                    DoubleCell.TYPE);
            columnSpecs[index] = columnSpecCreator.createSpec();
        }

        columnTitles.clear();
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_booleanModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_booleanModel.validateSettings(settings);
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

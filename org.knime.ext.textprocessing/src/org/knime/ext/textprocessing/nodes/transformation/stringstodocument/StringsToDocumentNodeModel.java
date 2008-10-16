/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentNodeModel extends NodeModel {

    
    private final static int INPORT = 0; 
    
    private SettingsModelString m_titleColModel = 
        StringsToDocumentNodeDialog.getTitleStringModel();
    
    private SettingsModelString m_fulltextColModel = 
        StringsToDocumentNodeDialog.getTextStringModel();

    private SettingsModelString m_authorsColModel = 
        StringsToDocumentNodeDialog.getAuthorsStringModel();
    
    private SettingsModelString m_authorNameSeparator = 
        StringsToDocumentNodeDialog.getAuthorSplitStringModel();
    
    private SettingsModelString m_docSourceModel = 
        StringsToDocumentNodeDialog.getDocSourceModel();
    
    private SettingsModelString m_docCategoryModel = 
        StringsToDocumentNodeDialog.getDocCategoryModel();
    
    private SettingsModelString m_docTypeModel = 
        StringsToDocumentNodeDialog.getTypeModel();
    
    private SettingsModelString m_pubDateModel = 
        StringsToDocumentNodeDialog.getPubDatModel();
    
    /**
     * Creates new instance of <code>StringsToDocumentNodeModel</code>.
     */
    public StringsToDocumentNodeModel() {
        super(1, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        DataTableSpecVerifier verifier = 
            new DataTableSpecVerifier(inSpecs[INPORT]);
        verifier.verifyMinimumStringCells(1, true);
        
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INPORT])};
    }

    private DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        DataColumnSpec strCol = new DataColumnSpecCreator("Document", 
                DocumentCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(strCol));
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
        BufferedDataTable inDataTable = inData[INPORT];
        StringsToDocumentConfig conf = new StringsToDocumentConfig();
        
        // Title
        int titleIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_titleColModel.getStringValue());
        conf.setTitleStringIndex(titleIndex);
        
        // Fulltext
        int fulltextIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_fulltextColModel.getStringValue());
        conf.setFulltextStringIndex(fulltextIndex);
        
        // Author names
        int authorIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_authorsColModel.getStringValue());
        conf.setAuthorsStringIndex(authorIndex);
        
        // Author name separator
        String authorNameSeparator = m_authorNameSeparator.getStringValue();
        if (!authorNameSeparator.isEmpty() 
                && authorNameSeparator.length() > 0) {
            conf.setAuthorsSplitChar(authorNameSeparator);
        }
        
        // Document source
        String docSource = m_docSourceModel.getStringValue();
        if (!docSource.isEmpty() && docSource.length() > 0) {
            conf.setDocSource(docSource);
        }
        
        // Document category
        String docCat = m_docCategoryModel.getStringValue();
        if (!docCat.isEmpty() && docCat.length() > 0) {
            conf.setDocCat(docCat);
        }
        
        // Document type
        String docType = m_docTypeModel.getStringValue();
        if (!docType.isEmpty() && docType.length() > 0) {
            conf.setDocType(docType);
        }
        
        // Publication Date
        String pubDate = m_pubDateModel.getStringValue();
        if (!pubDate.isEmpty() && pubDate.length() > 0) {
            conf.setPublicationDate(pubDate);
        }
        
        // initializes the corresponding cell factory
        StringsToDocumentCellFactory cellFac = 
            new StringsToDocumentCellFactory(conf);
        
        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(
                inDataTable.getDataTableSpec());
        rearranger.append(cellFac);
        
        return new BufferedDataTable[] {
                exec.createColumnRearrangeTable(inDataTable, rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fulltextColModel.loadSettingsFrom(settings);
        m_authorsColModel.loadSettingsFrom(settings);
        m_titleColModel.loadSettingsFrom(settings);
        m_authorNameSeparator.loadSettingsFrom(settings);
        m_docSourceModel.loadSettingsFrom(settings);
        m_docCategoryModel.loadSettingsFrom(settings);
        m_docTypeModel.loadSettingsFrom(settings);
        m_pubDateModel.loadSettingsFrom(settings);
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
    protected void saveSettingsTo(NodeSettingsWO settings) {
        m_fulltextColModel.saveSettingsTo(settings);
        m_authorsColModel.saveSettingsTo(settings);
        m_titleColModel.saveSettingsTo(settings);
        m_authorNameSeparator.saveSettingsTo(settings);
        m_docSourceModel.saveSettingsTo(settings);
        m_docCategoryModel.saveSettingsTo(settings);
        m_docTypeModel.saveSettingsTo(settings);
        m_pubDateModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_fulltextColModel.validateSettings(settings);
        m_authorsColModel.validateSettings(settings);
        m_titleColModel.validateSettings(settings);
        m_authorNameSeparator.validateSettings(settings);
        m_docSourceModel.validateSettings(settings);
        m_docCategoryModel.validateSettings(settings);
        m_docTypeModel.validateSettings(settings);
        m_pubDateModel.validateSettings(settings);
        
        String pubDate = ((SettingsModelString)m_pubDateModel.
                createCloneWithValidatedValue(settings)).getStringValue();
        
        Pattern p = Pattern.compile("(\\d){2}-(\\d){2}-(\\d){4}");
        Matcher m = p.matcher(pubDate);
        if (!m.matches()) {
            throw new InvalidSettingsException(
                    "Publicationdate is not formatted properly (dd-mm-yyyy)!");
        }
        
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);
        try {
            df.parse(pubDate);
        } catch (ParseException e) {
            throw new InvalidSettingsException(
                     "Specified date is not valid!\n" 
                    + e.getMessage());
        }
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
}

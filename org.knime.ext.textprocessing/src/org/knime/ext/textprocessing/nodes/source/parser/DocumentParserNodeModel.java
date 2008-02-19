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
 *   15.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.knime.core.data.DataTableSpec;
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
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;


/**
 * The model for all {@link org.knime.ext.textprocessing.data.Document} parser 
 * nodes, no matter what format they parse. The factory provides them with the
 * right {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}
 * instance they use to parse the specified files.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentParserNodeModel extends NodeModel {

    /**
     * The default path of the directory containing the files to parse.
     */
    public static final String DEFAULT_PATH = System.getProperty("user.home");
    
    /**
     * The default value of the recursive flag (if set <code>true</code> the 
     * specified directory is search recursively).
     */
    public static final boolean DEFAULT_RECURSIVE = false;
    
    
    private SettingsModelString m_pathModel = 
        DocumentParserNodeDialog.getPathModel();
    
    private SettingsModelBoolean m_recursiveModel = 
        DocumentParserNodeDialog.getRecursiveModel();
    
    
    private DocumentParser m_parser;
    
    private List<String> m_validExtensions;
    
    
    /**
     * Creates a new instance of <code>DocumentParserNodeModel</code> with the
     * specified parser to use and the valid extensions of files to parse.
     * 
     * @param parser The parser to use.
     * @param validFileExtensions The valid extensions of files to parse.
     */
    public DocumentParserNodeModel(final DocumentParser parser, 
            final String... validFileExtensions) {
        super(0, 1);
        m_parser = parser;
        m_validExtensions = Arrays.asList(validFileExtensions);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{
                DocumentDataTableBuilder.createDocumentDataTableSpec()};
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {
        List<Document> docs = new ArrayList<Document>();
        
        File dir = new File(m_pathModel.getStringValue());        
        boolean recursive = m_recursiveModel.getBooleanValue();
        
        FileCollector fc = new FileCollector(dir, m_validExtensions, recursive);
        List<File> files = fc.getFiles();
        for (File f : files) {
            InputStream is;
            if (f.getName().toLowerCase().endsWith(".gz") 
                    || f.getName().toLowerCase().endsWith(".zip")) {
                is = new GZIPInputStream(new FileInputStream(f));
            } else {
                is = new FileInputStream(f);
            }
            docs.addAll(m_parser.parse(is));
        }
        
        return new BufferedDataTable[]{
                DocumentDataTableBuilder.createDocumentDataTable(exec, docs)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }


    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_pathModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
    }

}

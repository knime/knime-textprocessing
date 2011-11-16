/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 * 
 * History
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.inport;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
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
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;


/**
 * 
 * @author thiel, University of Konstanz
 */
public class DictionaryTaggerNodeModel extends NodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;
    
    /**
     * The default value of the case sensitive setting.
     */
    public static final boolean DEFAULT_CASE_SENSITIVE = true;
    
    /**
     * The default value of the default tag.
     */
    public static final String DEFAULT_TAG = 
        NamedEntityTag.UNKNOWN.getTag().getTagValue();
    
    /**
     * The default tag type.
     */
    public static final String DEFAULT_TAG_TYPE = "NE";
    
    public static final int DICT_TABLE_INDEX = 1;
    
    public static final int DATA_TABLE_INDEX = 0;
    
    
    private int m_docColIndex = -1;
    
    private SettingsModelBoolean m_setUnmodifiableModel = 
        DictionaryTaggerNodeDialog.createSetUnmodifiableModel();
    
    private SettingsModelString m_tagModel = 
        DictionaryTaggerNodeDialog.createTagModel();
    
    private SettingsModelString m_tagTypeModel = 
        DictionaryTaggerNodeDialog.createTagTypeModel();    
    
    private SettingsModelBoolean m_caseSensitiveModel = 
        DictionaryTaggerNodeDialog.createCaseSensitiveModel();
    
    private SettingsModelString m_columnModel = 
        DictionaryTaggerNodeDialog.createColumnModel();
    
    private DocumentDataTableBuilder m_dtBuilder;
    
    
    /**
     * Creates a new instance of <code>DictionaryTaggerNodeModel</code> with two
     * table in ports and one out port.
     */
    public DictionaryTaggerNodeModel() {
        super(2, 1);
        m_dtBuilder = new DocumentDataTableBuilder();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs);
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    private void checkDataTableSpec(final DataTableSpec[] specs) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(
                specs[DICT_TABLE_INDEX]);
        verfier.verifyStringCell(true);
        verfier = new DataTableSpecVerifier(specs[DATA_TABLE_INDEX]);
        verfier.verifyDocumentCell(true);
        m_docColIndex = verfier.getDocumentCellIndex();
    }      
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(new DataTableSpec[]{
                inData[0].getDataTableSpec(), inData[1].getDataTableSpec()
        });
        
        // Read table with dictionary
        int dictIndex = 
            inData[DICT_TABLE_INDEX].getDataTableSpec().findColumnIndex(
                m_columnModel.getStringValue());
        Set<String> namedEntities = new HashSet<String>();
        RowIterator it = inData[DICT_TABLE_INDEX].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            namedEntities.add(
                    ((StringValue)row.getCell(dictIndex)).getStringValue());
        }
        
        // tag documents
        String tagTypeStr = m_tagTypeModel.getStringValue();
        String tagStr = m_tagModel.getStringValue();
        Tag tag = TagFactory.getInstance().getTagSetByType(tagTypeStr)
                .buildTag(tagStr);
        DocumentTagger tagger = new DictionaryDocumentTagger(
                m_setUnmodifiableModel.getBooleanValue(), namedEntities, tag,
                m_caseSensitiveModel.getBooleanValue());
        
        it = inData[DATA_TABLE_INDEX].iterator();
        int rowCount = inData[DATA_TABLE_INDEX].getRowCount();
        int currDoc = 1;
        m_dtBuilder.openDataTable(exec);
        while (it.hasNext()) {
            
            double progress = (double)currDoc / (double)rowCount;
            exec.setProgress(progress, "Tagging document " + currDoc + " of " 
                    + rowCount);
            exec.checkCanceled();
            currDoc++;
            
            DataRow row = it.next();
            DocumentValue docVal = (DocumentValue)row.getCell(m_docColIndex);
            m_dtBuilder.addDocument(tagger.tag(docVal.getDocument()));
        }
        
        return new BufferedDataTable[]{m_dtBuilder.getAndCloseDataTable()};
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
        m_caseSensitiveModel.loadSettingsFrom(settings);
        m_tagModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_setUnmodifiableModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_caseSensitiveModel.saveSettingsTo(settings);
        m_tagModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_caseSensitiveModel.validateSettings(settings);
        m_tagModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);
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

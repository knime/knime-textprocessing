/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   27.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.nodes.frequencies.FrequenciesNodeSettingsPane;
import org.knime.ext.textprocessing.util.DataStructureUtil;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class DocumentViewer2NodeModel extends NodeModel implements BufferedDataTableHolder {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentViewer2NodeModel.class);

    private static final int INPUT_INDEX = 0;

    private int m_documentCellindex = -1;

    private List<Document> m_documents;

    private BufferedDataTable m_data;

    private static final String SETTINGS_FILE = "DocumentViewerNodeModelSettings.dat";

    private static final String INTERNAL_MODEL = "DocViewerModel";

    private static final String DOCUMENT_INDEX = "DocIndex";

    private SettingsModelString m_documentColModel = FrequenciesNodeSettingsPane.getDocumentColumnModel();

    private ExecutionContext m_exec;

    /**
     * Creates new instance of <code>DocumentViewerNodeModel</code>.
     */
    public DocumentViewer2NodeModel() {
        super(1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[INPUT_INDEX]);
        verifier.verifyMinimumDocumentCells(1, true);
        m_documentCellindex = verifier.getDocumentCellIndex();

        int documentColIndex = inSpecs[0].findColumnIndex(m_documentColModel.getStringValue());
        if (documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified document column is not valid! " + "Check your settings!");
        }

        return new DataTableSpec[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        m_exec = exec;
        m_documentCellindex = inData[0].getDataTableSpec().findColumnIndex(m_documentColModel.getStringValue());

        m_documents = new ArrayList<Document>();
        m_data = inData[INPUT_INDEX];
        m_documents = DataStructureUtil.buildDocumentList(m_data, m_documentCellindex, m_exec);
        Collections.sort(m_documents, new Comparator<Document>() {
            @Override
            public int compare(final Document o1, final Document o2) {
                String title1 = o1.getTitle();
                String title2 = o2.getTitle();
                return title1.compareTo(title2);
            }
        });

        return new BufferedDataTable[]{};
    }

    /**
     * @return the set of documents to display.
     */
        List<Document> getDocumentList() {
        return m_documents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable[] getInternalTables() {
        return new BufferedDataTable[]{m_data};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInternalTables(final BufferedDataTable[] tables) {
        if (tables.length != 1) {
            throw new IllegalArgumentException();
        }
        m_data = tables[0];
        try {
            m_documents = DataStructureUtil.buildDocumentList(m_data, m_documentCellindex, m_exec);
        } catch (CanceledExecutionException e) {
            LOGGER.warn("Could not load internal table, execution was canceled!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        File file = new File(nodeInternDir, SETTINGS_FILE);
        FileInputStream fis = new FileInputStream(file);
        ModelContentRO modelContent = ModelContent.loadFromXML(fis);

        // Load settings
        try {
            m_documentCellindex = modelContent.getInt(DOCUMENT_INDEX);
        } catch (InvalidSettingsException e1) {
            IOException ioe = new IOException("Could not load internals!");
            ioe.initCause(e1);
            fis.close();
            throw ioe;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        if (m_documents != null) {
            m_documents.clear();
            m_documents = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Save tree
        ModelContent modelContent = new ModelContent(INTERNAL_MODEL);

        // Save settings
        modelContent.addInt(DOCUMENT_INDEX, m_documentCellindex);

        File file = new File(nodeInternDir, SETTINGS_FILE);
        FileOutputStream fos = new FileOutputStream(file);
        modelContent.saveToXML(fos);
        fos.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
    }
}

/*
========================================================================
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   20.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;


/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
/**
 *
 * @author Kilian Thiel, KNIME.com AG, Zurich
 */
public abstract class AbstractDocumentGrabber implements DocumentGrabber {
    private ExecutionMonitor m_exec;

    private boolean m_delete;

    private DocumentCategory m_documentCategory;

    private DocumentType m_documentType;

    private boolean m_extractMetaInfo = false;

    // initialize the tokenizer with the old standard tokenizer for backwards compatibility
    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * List of listeners.
     */
    protected List<DocumentParsedEventListener> m_listener;

    /**
     * Constructor of <code>AbstractDocumentGrabber</code> with flag
     * deleteFiles to set, which specifies if downloaded files will be deleted
     * after parsing, the given document category and type to set
     * and <code>ExecutionMonitor</code> to handle the progress.
     *
     * @param deleteFiles If set true files will be deleted after parsing.
     * @param documentCategory The category of the documents to set.
     * @param documentType The tpe of the document to set.
     * @param exec <code>ExecutionMonitor</code> to display the progress of
     * the process.
     */
    public AbstractDocumentGrabber(final boolean deleteFiles,
            final DocumentCategory documentCategory,
            final DocumentType documentType,
            final ExecutionMonitor exec) {
        m_exec = exec;
        m_delete = deleteFiles;
        m_documentCategory = documentCategory;
        m_documentType = documentType;
        m_listener = new ArrayList<DocumentParsedEventListener>();
    }

    /**
     * Constructor of <code>AbstractDocumentGrabber</code> with flag
     * deleteFiles to set, which specifies if downloaded files will be deleted
     * after parsing.
     *
     * @param deleteFiles If set true files will be deleted after parsing.
     * @param documentCategory The category of the documents to set.
     */
    public AbstractDocumentGrabber(final boolean deleteFiles,
            final DocumentCategory documentCategory) {
        this(deleteFiles, documentCategory, null, null);
    }

    /**
     * Constructor of <code>AbstractDocumentGrabber</code>. Files will
     * not be deleted after parsing, deleteFiles is set false by default.
     */
    public AbstractDocumentGrabber() {
        this(false, null, null, null);
    }

    /**
     * @return the delete flag. If set true files will be deleted after parsing.
     */
    public boolean getDeleteFiles() {
        return m_delete;
    }

    /**
     * @param delete If set <code>true</code> files will be deleted after
     * parsing.
     */
    public void setDeleteFiles(final boolean delete) {
        m_delete = delete;
    }

    /**
     * @return the <code>ExecutionMonitor</code>.
     */
    public ExecutionMonitor getExec() {
        return m_exec;
    }

    /**
     * @param exec the <code>ExecutionMonitor</code> to set,
     */
    public void setExec(final ExecutionMonitor exec) {
        m_exec = exec;
    }

    /**
     * @see org.knime.core.node.NodeProgressMonitor#checkCanceled()
     * @throws CanceledExecutionException which indicated the execution will be
     *             canceled by this call.
     */
    protected void checkCanceled() throws CanceledExecutionException {
        if (m_exec != null) {
            m_exec.checkCanceled();
        }
    }

    /**
     * Sets the given progress and message if execution context is available.
     *
     * @param progress The progress to set.
     * @param message The message to set.
     */
    protected void setProgress(final double progress, final String message) {
        if (m_exec != null) {
            m_exec.setProgress(progress, message);
        }
    }

    /**
     * @return the document category
     */
    public DocumentCategory getDocumentCategory() {
        return m_documentCategory;
    }

    /**
     * @param documentCategory the category of the document to set
     */
    public void setDocumentCategory(final DocumentCategory documentCategory) {
        m_documentCategory = documentCategory;
    }

    /**
     * @return the documentType
     */
    public DocumentType getDocumentType() {
        return m_documentType;
    }

    /**
     * @param documentType the documentType to set
     */
    public void setDocumentType(final DocumentType documentType) {
        m_documentType = documentType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDocumentParsedListener(
            final DocumentParsedEventListener listener) {
        m_listener.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDocumentParsedListener(
            final DocumentParsedEventListener listener) {
        m_listener.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllDocumentParsedListener() {
        m_listener.clear();
    }

    /**
     * Notifies all registered listeners with given event.
     * @param event Event to notify listener with
     */
    public void notifyAllListener(final DocumentParsedEvent event) {
        for (DocumentParsedEventListener l : m_listener) {
            l.documentParsed(event);
        }
    }

    /**
     * @return the extractMetaInfo
     * @since 2.7
     */
    public boolean getExtractMetaInfo() {
        return m_extractMetaInfo;
    }

    /**
     * @param extractMetaInfo the extractMetaInfo to set
     * @since 2.7
     */
    public void setExtractMetaInfo(final boolean extractMetaInfo) {
        m_extractMetaInfo = extractMetaInfo;
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    @Override
    public void setTokenizerName(final String tokenizerName) {
        m_tokenizerName = tokenizerName;
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    @Override
    public String getTokenizerName() {
        return m_tokenizerName;
    }
}

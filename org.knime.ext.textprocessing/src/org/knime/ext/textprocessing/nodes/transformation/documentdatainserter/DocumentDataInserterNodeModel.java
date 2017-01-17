/*
 * ------------------------------------------------------------------------
 *
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
 *   17.01.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentdatainserter;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Julian Bunzel, KNIME.com Berlin
 */
public class DocumentDataInserterNodeModel extends SimpleStreamableFunctionNodeModel {

    /** The default number of threads. */
    static final int DEF_THREADS = Math.max(1, Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
        (int)Math.ceil(Runtime.getRuntime().availableProcessors())));

    /** The min number of threads. */
    static final int MIN_THREADS = 1;

    /** The max number of threads. */
    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    private SettingsModelString m_docColumnModel = DocumentDataInserterNodeDialog.getDocumentColumnModel();

    private SettingsModelString m_authorsColModel = DocumentDataInserterNodeDialog.getAuthorsColumnModel();

    private SettingsModelString m_sourceColModel = DocumentDataInserterNodeDialog.getSourceColumnModel();

    private SettingsModelString m_categoryColModel = DocumentDataInserterNodeDialog.getCategoryColumnModel();

    private SettingsModelString m_pubDateColModel = DocumentDataInserterNodeDialog.getPubDateColumnModel();

    private SettingsModelBoolean m_useAuthorsColModel = DocumentDataInserterNodeDialog.getUseAuthorsColumnModel();

    private SettingsModelBoolean m_useSourceColModel = DocumentDataInserterNodeDialog.getUseSourceColumnModel();

    private SettingsModelBoolean m_useCategoryColModel = DocumentDataInserterNodeDialog.getUseCategoryColumnModel();

    private SettingsModelBoolean m_usePubDateColModel = DocumentDataInserterNodeDialog.getUsePubDateColumnModel();

    private SettingsModelString m_authorsFirstNameModel = DocumentDataInserterNodeDialog.getAuthorsFirstNameModel();

    private SettingsModelString m_authorsLastNameModel = DocumentDataInserterNodeDialog.getAuthorsLastNameModel();

    private SettingsModelString m_authorsSplitStr = DocumentDataInserterNodeDialog.getAuthorsSplitStringModel();

    private SettingsModelString m_sourceModel = DocumentDataInserterNodeDialog.getSourceModel();

    private SettingsModelString m_categoryModel = DocumentDataInserterNodeDialog.getCategoryModel();

    private SettingsModelString m_pubDateModel = DocumentDataInserterNodeDialog.getPubDateModel();


    /**
     * {@inheritDoc}
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        // author names
        String authorColName = m_authorsColModel.getStringValue();


        ColumnRearranger rearranger = new ColumnRearranger(spec);
        return rearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Nov 30, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The node dialog for the StanfordNLP Extractor nodes.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class ParallelExtractorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * The {@link SettingsModelString} storing the column name of the document column.
     */
    private final SettingsModelString m_documentColModel = ParallelExtractorNodeModel.getDocumentColumnModel();

    /**
     * The {@link SettingsModelString} storing the column name of the lemmatized document column.
     */
    private final SettingsModelString m_lemmatizedDocumentColModel =
        ParallelExtractorNodeModel.getLemmatizedDocumentColumnModel();

    /**
     * The {@link SettingsModelBoolean} storing the boolean value of the option to apply the required preprocessing.
     */
    private final SettingsModelBoolean m_applyReqPreprocModel = ParallelExtractorNodeModel.getApplyReqPreprocModel();

    /**
     * Constructor for class {@link ParallelExtractorNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public ParallelExtractorNodeDialog() {

        setHorizontalPlacement(true);
        // document col
        final DialogComponentColumnNameSelection docColSelectionComp =
            new DialogComponentColumnNameSelection(m_documentColModel, "Document column", 0, DocumentValue.class);
        docColSelectionComp.setToolTipText("Column containing the documents to extract relations from.");
        addDialogComponent(docColSelectionComp);

        // lemma document col
        final DialogComponentColumnNameSelection lemmaDocColSelectionComp = new DialogComponentColumnNameSelection(
            m_lemmatizedDocumentColModel, "Lemmatized document column", 0, DocumentValue.class);
        lemmaDocColSelectionComp.setToolTipText("Column containing the lemmatized documents.");
        addDialogComponent(lemmaDocColSelectionComp);
        setHorizontalPlacement(false);

        setHorizontalPlacement(true);
        m_applyReqPreprocModel.addChangeListener(e -> update());
        final DialogComponentBoolean applyReqPreprocComp =
            new DialogComponentBoolean(m_applyReqPreprocModel, "Apply preprocessing");
        addDialogComponent(applyReqPreprocComp);

        setHorizontalPlacement(false);
        addDialogComponent(
            new DialogComponentNumber(ParallelExtractorNodeModel.getNumberOfThreadsModel(), "Number of threads", 1));

        update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        if (!m_applyReqPreprocModel.getBooleanValue()
            && (m_documentColModel.getStringValue().equals(m_lemmatizedDocumentColModel.getStringValue()))) {
            throw new InvalidSettingsException("Document column and lemmatized document column cannot be the same!");
        }
    }

    /**
     * Enable/disable components.
     */
    private final void update() {
        m_lemmatizedDocumentColModel.setEnabled(!m_applyReqPreprocModel.getBooleanValue());
    }
}

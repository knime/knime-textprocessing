/*
 * ------------------------------------------------------------------------
 *
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
 *   25.08.2016 (David Kolb): created
 */
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.learn.d2v.dialog;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.NominalValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.nodes.dialog.AbstractGridBagDialogComponentGroup;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorLearnerParameter;
import org.knime.ext.textprocessing.dl4j.settings.impl.WordVectorParameterSettingsModels2;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing data parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class Doc2VecDataParametersComponentGroup extends AbstractGridBagDialogComponentGroup {

    private SettingsModelString m_documentColumnSettings;

    private SettingsModelString m_labelColumnSettings;

    private JLabel m_indicatorLabel;

    /**
     * @param wvSettings
     */
    @SuppressWarnings("unchecked")
    public Doc2VecDataParametersComponentGroup(final WordVectorParameterSettingsModels2 wvSettings) {
        m_documentColumnSettings =
            (SettingsModelString)wvSettings.createParameter(WordVectorLearnerParameter.DOCUMENT_COLUMN);
        m_labelColumnSettings =
            (SettingsModelString)wvSettings.createParameter(WordVectorLearnerParameter.LABEL_COLUMN);

        m_documentColumnSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateIndicatorLabel();
            }
        });

        m_labelColumnSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateIndicatorLabel();
            }
        });

        addWhitespaceRow(2);
        addColumnNameSelectionRowComponent(m_documentColumnSettings, "Document Column", 0, StringValue.class,
            DocumentValue.class);
        addColumnNameSelectionRowComponent(m_labelColumnSettings, "Label Column", 0, NominalValue.class);

        m_indicatorLabel = new JLabel("");
        addWhitespaceRow(1);
        addComponent(m_indicatorLabel);
        addWhitespaceRow(2);
    }

    /**
     * Updates label in dialog indicating if the column selection is valid. Label and document column selection must not
     * be the same.
     */
    private void updateIndicatorLabel() {
        String docCol = m_documentColumnSettings.getStringValue();
        String labelCol = m_labelColumnSettings.getStringValue();

        try {
            checkDoc2VecColumnSelection(docCol, labelCol);
            m_indicatorLabel.setText("Column selection OK!");
            m_indicatorLabel.setForeground(Color.BLACK);
        } catch (InvalidSettingsException e) {
            m_indicatorLabel.setText(e.getMessage());
            m_indicatorLabel.setForeground(Color.RED);
        }
    }

    /**
     * Check if the specified column names are valid in the context of Doc2Vec.
     *
     * @param col1 name of the fist column
     * @param col2 name of the second column
     * @throws InvalidSettingsException if the column names are the same
     */
    public static void checkDoc2VecColumnSelection(final String col1, final String col2)
        throws InvalidSettingsException {
        if (col1.equals(col2)) {
            throw new InvalidSettingsException("Document and Label column selection must not be the same!");
        }
    }
}

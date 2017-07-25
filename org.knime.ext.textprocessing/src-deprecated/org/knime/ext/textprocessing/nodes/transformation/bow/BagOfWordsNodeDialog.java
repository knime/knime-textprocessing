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
 * Created on 23.10.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.transformation.bow;

import javax.swing.event.ChangeListener;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.preprocessing.DefaultSwitchEventListener;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public final class BagOfWordsNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns the boolean settings model which contains the flag if the original documents will be
     * be appended in an extra column or not.
     */
    public static SettingsModelBoolean getAppendIncomingDocument() {
        return new SettingsModelBoolean(
                BagOfWordsConfigKeys.CFG_KEY_APPEND_ORIGDOCUMENTS,
                BagOfWordsNodeModel.DEF_APPEND_ORIGDOCUMENT);
    }

    /**
     * @return Creates and returns the string settings model containing the name of the column with the documents to
     * create the bag of words from.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                BagOfWordsConfigKeys.CFG_KEY_DOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }

    /**
     * @return Creates and returns the string settings model containing the name of the column with the original
     * documents to append unchanged.
     */
    public static SettingsModelString getOrigDocumentColumnModel() {
        return new SettingsModelString(
                BagOfWordsConfigKeys.CFG_KEY_ORIGDOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME);
    }

    /**
     * Constructor for class {@link BagOfWordsNodeDialog}.
     */
    public BagOfWordsNodeDialog() {

        // document col to create bow from
        final SettingsModelString docColModel = getDocumentColumnModel();
        @SuppressWarnings("unchecked")
        final DialogComponentColumnNameSelection comp1 = new DialogComponentColumnNameSelection(docColModel,
                    "Document column", 0, DocumentValue.class);
        comp1.setToolTipText("Column containing the documents to create bow from.");
        addDialogComponent(comp1);

        // original document to append and append setting
        createNewGroup("Appending");

        final SettingsModelBoolean appendOrigDocModel = getAppendIncomingDocument();
        final DialogComponentBoolean comp2 = new DialogComponentBoolean(
            appendOrigDocModel, "Append original documents");
        comp2.setToolTipText("The original documents will be appended!");
        addDialogComponent(comp2);

        final SettingsModelString origDocColModel = getOrigDocumentColumnModel();
        @SuppressWarnings("unchecked")
        final DialogComponentColumnNameSelection comp3 = new DialogComponentColumnNameSelection(origDocColModel,
                    "Original Document column", 0, DocumentValue.class);
        comp3.setToolTipText("Column containing the original documents to append!");
        addDialogComponent(comp3);

        final ChangeListener cl1 = new DefaultSwitchEventListener(origDocColModel, appendOrigDocModel);
        appendOrigDocModel.addChangeListener(cl1);
        cl1.stateChanged(null);

        closeCurrentGroup();
    }
}

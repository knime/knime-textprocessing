/*
 * ------------------------------------------------------------------------
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
 *   01.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 * A {@link org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane}
 * which provides additionally a tab that contains a checkbox to specify
 * if deep preprocessing have to be applied or not.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated use {@link PreprocessingNodeSettingsPane2} instead.
 */
@Deprecated
public class PreprocessingNodeSettingsPane extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if deep preprocessing have to be applied or not.
     */
    public static SettingsModelBoolean getDeepPrepressingModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_DEEP_PREPRO,
                PreprocessingNodeModel.DEF_DEEP_PREPRO);
    }

    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if the incoming original documents have to be applied in an
     * extra column.
     */
    public static SettingsModelBoolean getAppendIncomingDocument() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_APPEND_INCOMING,
                PreprocessingNodeModel.DEF_APPEND_INCOMING);
    }

    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the documents to preprocess.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                PreprocessingConfigKeys.CFG_KEY_DOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }

    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the original documents to append unchanged.
     */
    public static SettingsModelString getOrigDocumentColumnModel() {
        return new SettingsModelString(
                PreprocessingConfigKeys.CFG_KEY_ORIGDOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME);
    }

    /**
     * @return Creates and returns a <code>SettingsModelBoolean</code> which
     * stores the flag specifying whether unmodifiable terms will be
     * preprocessed was well or not.
     */
    public static SettingsModelBoolean getPreprocessUnmodifiableModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_PREPRO_UNMODIFIABLE,
                PreprocessingNodeModel.DEF_PREPRO_UNMODIFIABLE);
    }

    /**
     * Creates new instance of <code>PreprocessingNodeSettingsPane</code>.
     */
    @SuppressWarnings("unchecked")
    public PreprocessingNodeSettingsPane() {
        removeTab("Options");
        createNewTabAt("Preprocessing", 1);

        //
        // document to preprocess and deep preprocessing
        //
        createNewGroup("Deep preprocessing");

        SettingsModelBoolean deepPreproModel = getDeepPrepressingModel();
        DialogComponentBoolean comp1 = new DialogComponentBoolean(
                deepPreproModel, "Deep preprocessing");
        comp1.setToolTipText(
                "Be aware that deep preprocessing is more time consuming!");
        addDialogComponent(comp1);

        SettingsModelString documentColModel = getDocumentColumnModel();
        DialogComponentColumnNameSelection comp3 =
            new DialogComponentColumnNameSelection(documentColModel,
                    "Document column", 0, DocumentValue.class);
        comp3.setToolTipText(
                "Column has to contain documents to preprocess!");
        addDialogComponent(comp3);

        ChangeListener cl1 = new DefaultSwitchEventListener(
                documentColModel, deepPreproModel);
        deepPreproModel.addChangeListener(cl1);

        closeCurrentGroup();

        //
        // original document to append and append setting
        //
        createNewGroup("Appending");

        SettingsModelBoolean appendOrigDocModel = getAppendIncomingDocument();
        DialogComponentBoolean comp2 = new DialogComponentBoolean(
                appendOrigDocModel, "Append unchanged documents");
        comp2.setToolTipText(
                "The unchanged incoming documents will be appended!");
        addDialogComponent(comp2);

        SettingsModelString origDocColModel = getOrigDocumentColumnModel();
        DialogComponentColumnNameSelection comp4 =
            new DialogComponentColumnNameSelection(origDocColModel,
                    "Original Document column", 0, DocumentValue.class);
        comp4.setToolTipText("Column has to contain the original documents "
                + "to append unchanged!");
        addDialogComponent(comp4);

        ChangeListener cl2 = new DefaultSwitchEventListener(
                origDocColModel, appendOrigDocModel);
        appendOrigDocModel.addChangeListener(cl2);

        cl1.stateChanged(null);
        cl2.stateChanged(null);

        closeCurrentGroup();

        //
        // chunk size
        //
//        createNewGroup("Chunking");
//
//        addDialogComponent(new DialogComponentNumber(
//                getChunkSizeModel(), "Chunk size", 1000));
//
//        closeCurrentGroup();

        //
        // preprocess unmodifiable terms
        //
        createNewGroup("Unmodifiable policy");

        addDialogComponent(new DialogComponentBoolean(
                getPreprocessUnmodifiableModel(),
                "Ignore unmodifiable flag"));

        closeCurrentGroup();
    }
}

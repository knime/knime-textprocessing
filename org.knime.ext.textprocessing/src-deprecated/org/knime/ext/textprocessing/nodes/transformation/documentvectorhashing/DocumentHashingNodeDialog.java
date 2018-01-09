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
 *   10.08.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectorhashing;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 *
 * @author Tobias Koetter and Andisa Dewi, KNIME.com, Berlin, Germany
 * @since 3.3
 * @deprecated Use {@link DocumentHashingNodeDialog2} instead
 */
@Deprecated
public class DocumentHashingNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return the document column
     */
    static final SettingsModelString getDocumentColModel() {
        return new SettingsModelString(DocumentHashingConfigKeys.CFGKEY_DOC_COL,
            DocumentHashingNodeModel.DEFAULT_DOCUMENT_COLNAME);
    }

    /**
     * @return the number of buckets for the hashing function
     */
    static SettingsModelIntegerBounded getDimModel() {
        return new SettingsModelIntegerBounded(DocumentHashingConfigKeys.CFGKEY_DIM, 5000, 1, Integer.MAX_VALUE);
    }

    /**
     * @return the seed for the hashing function
     */
    static SettingsModelInteger getSeedModel() {
        return new SettingsModelInteger(DocumentHashingConfigKeys.CFGKEY_SEED, DocumentHashingNodeModel.DEFAULT_SEED);
    }

    /**
     * @return the hashing function
     */
    static SettingsModelString getHashingMethod() {
        return new SettingsModelString(DocumentHashingConfigKeys.CFGKEY_HASHING_FUNC, "murmur3_32bit");
    }

    /**
     * @return the vector value type
     */
    static SettingsModelString getVectorValueModel() {
        return new SettingsModelString(DocumentHashingConfigKeys.CFGKEY_VEC_VAL, "binary");
    }

    /**
     * @return a flag to specify whether the vector should be put in columns or as collection
     */
    public static final SettingsModelBoolean getAsCollectionModel() {
        return new SettingsModelBoolean(DocumentHashingConfigKeys.CFGKEY_ASCOLLECTION,
            DocumentHashingNodeModel.DEFAULT_ASCOLLECTION);
    }

    /**
     * Creates a new instance of <code>DocumentHashingNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public DocumentHashingNodeDialog() {
        createNewGroup("Document column setting");
        addDialogComponent(
            new DialogComponentColumnNameSelection(getDocumentColModel(), "Document column: ", 0, DocumentValue.class));
        closeCurrentGroup();

        createNewGroup("Hashing function setting");
        addDialogComponent(new DialogComponentNumber(getDimModel(), "Dimension: ", 100));

        addDialogComponent(new DialogComponentNumberEdit(getSeedModel(), "Seed: "));

        List<String> namesAsList = new ArrayList<String>(HashingFunctionFactory.getInstance().getHashNames());

        addDialogComponent(new DialogComponentStringSelection(getHashingMethod(), "Hashing function: ", namesAsList));

        addDialogComponent(new DialogComponentStringSelection(getVectorValueModel(), "Vector type: ", "Binary",
            "TF-Relative", "TF-Absolute"));
        closeCurrentGroup();

        createNewGroup("Output column setting");
        addDialogComponent(new DialogComponentBoolean(getAsCollectionModel(), "As collection cell"));
        closeCurrentGroup();
    }

}

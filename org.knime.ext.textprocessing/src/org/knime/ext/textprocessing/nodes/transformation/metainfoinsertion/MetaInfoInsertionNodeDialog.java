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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 */
public final class MetaInfoInsertionNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return The settings model containing the document column name.
     */
    public static SettingsModelString createDocumentColumnModel() {
        return new SettingsModelString(MetaInfoInsertionConfigKeys.CFGKEY_DOCCOL,
                                       BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }

    /**
     * @return The settings model containing the key column name.
     */
    public static SettingsModelString createKeyColumnModel() {
        return new SettingsModelString(MetaInfoInsertionConfigKeys.CFGKEY_KEYCOL,
                                       MetaInfoInsertionNodeModel.DEF_KEYCOL);
    }

    /**
     * @return The settings model containing the value column name.
     */
    public static SettingsModelString createValueColumnModel() {
        return new SettingsModelString(MetaInfoInsertionConfigKeys.CFGKEY_VALUECOL,
                                       MetaInfoInsertionNodeModel.DEF_VALUECOL);
    }

    /**
     * @return The settings model containing the settings whether the key and value cols are kept.
     */
    public static SettingsModelBoolean createKeepKeyValColsModel() {
        return new SettingsModelBoolean(MetaInfoInsertionConfigKeys.CFGKEY_KEEPKEYVALCOLS,
                                        MetaInfoInsertionNodeModel.DEF_KEEPKEYVALCOLS);
    }

    /**
     * Constructor of {@link MetaInfoInsertionNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public MetaInfoInsertionNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(createDocumentColumnModel(), "Document column", 0,
                                                                  StringValue.class));

        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentColumnNameSelection(createKeyColumnModel(), "Key column", 0,
                                                                  StringValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(createValueColumnModel(), "Value column", 0,
                                                                  StringValue.class));

        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentBoolean(createKeepKeyValColsModel(), "Keep key and value columns"));
    }
}

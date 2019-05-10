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
 *   Apr 25, 2019 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The {@code NodeDialog} for the Meta Info Inserter node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MetaInfoInsertionNodeDialog2 extends DefaultNodeSettingsPane {

    /** Creates a new instance of {@link MetaInfoInsertionNodeDialog2}. */
    @SuppressWarnings("unchecked")
    MetaInfoInsertionNodeDialog2() {
        setHorizontalPlacement(true);
        // document col to add meta information to
        final DialogComponentColumnNameSelection docColSelectionComp = new DialogComponentColumnNameSelection(
            MetaInfoInsertionNodeModel2.getDocumentColumnModel(), "Document column", 0, DocumentValue.class);
        docColSelectionComp.setToolTipText("Column containing the documents to add meta information to.");
        addDialogComponent(docColSelectionComp);

        // check box to select whether to remove meta information columns after insertion or not
        final DialogComponentBoolean removeMetaInfColsComp = new DialogComponentBoolean(
            MetaInfoInsertionNodeModel2.getRemoveMetaInfoColsModel(), "Remove inserted meta info columns");
        addDialogComponent(removeMetaInfColsComp);
        setHorizontalPlacement(false);

        // column filter component to select output columns
        createNewGroup("Meta Info Column Selection");
        addDialogComponent(new DialogComponentColumnFilter2(MetaInfoInsertionNodeModel2.getColumnSelectionModel(), 0));
    }
}

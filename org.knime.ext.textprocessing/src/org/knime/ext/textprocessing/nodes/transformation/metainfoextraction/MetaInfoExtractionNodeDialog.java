/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 30.03.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoextraction;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class MetaInfoExtractionNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return The settings model containing the column name of the document column.
     */
    public static SettingsModelString createDocColModel() {
        return new SettingsModelString(MetaInfoExtractionConfigKeys.CFGKEY_DOCCOL,
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }

    /**
     * @return The settings model containing the flag whether documents will be appended or not.
     */
    public static SettingsModelBoolean createAppendDocsModel() {
        return new SettingsModelBoolean(MetaInfoExtractionConfigKeys.CFGKEY_APPENDDOCS,
                MetaInfoExtractionNodeModel.DEF_APPENDDOCS);
    }

    /**
     * @return The settings model containing the flag whether meta info of distinct docs only is extracted.
     */
    public static SettingsModelBoolean createDistinctDocsModel() {
        return new SettingsModelBoolean(MetaInfoExtractionConfigKeys.CFGKEY_DISTINCTDOCS,
                MetaInfoExtractionNodeModel.DEF_DISTINCTDOCS);
    }

    /**
     * @return The settings model containing the flag whether meta info with a certain key is extracted.
     */
    public static SettingsModelBoolean createKeysOnlyModel() {
        return new SettingsModelBoolean(MetaInfoExtractionConfigKeys.CFGKEY_KEYSONLY,
                MetaInfoExtractionNodeModel.DEF_ONLYMETAKEYS);
    }

    /**
     * @return The settings model containing the keys for which meta info is extracted.
     */
    public static SettingsModelString createKeysModel() {
        return new SettingsModelString(MetaInfoExtractionConfigKeys.CFGKEY_KEYS, "key1,key2");
    }

    private SettingsModelBoolean m_keysOnlyModel = createKeysOnlyModel();

    private SettingsModelString m_keysModel = createKeysModel();

    /**
     * Constructor of {@link MetaInfoExtractionNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public MetaInfoExtractionNodeDialog() {

        addDialogComponent(new DialogComponentColumnNameSelection(createDocColModel(), "Document column", 0,
                                                                  DocumentValue.class));

        addDialogComponent(new DialogComponentBoolean(createAppendDocsModel(), "Append documents"));

        addDialogComponent(new DialogComponentBoolean(createDistinctDocsModel(), "For distinct documents only"));

        m_keysOnlyModel.addChangeListener(new MetaInfoDialogChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_keysOnlyModel, "Meta info for keys"));

        addDialogComponent(new DialogComponentMultiLineString(m_keysModel, "Meta keys, comma separated", false, 4, 2));

        enableDialogs();
    }

    private void enableDialogs() {
        m_keysModel.setEnabled(m_keysOnlyModel.getBooleanValue());
    }

    /**
     * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
     */
    class MetaInfoDialogChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            enableDialogs();
        }
    }
}

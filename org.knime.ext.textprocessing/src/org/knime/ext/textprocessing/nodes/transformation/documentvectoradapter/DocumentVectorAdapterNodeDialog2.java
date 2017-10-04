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
 *   15.09.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.DocumentVectorPortObjectSpec;

/**
 * Provides the dialog of the document vector adapter node.
 *
 * @author Andisa Dewi & Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
class DocumentVectorAdapterNodeDialog2 extends DefaultNodeSettingsPane {

    static final SettingsModelBoolean getBooleanModel() {
        return new SettingsModelBoolean(DocumentVectorAdapterConfigKeys2.CFGKEY_BOOLEAN,
            DocumentVectorAdapterNodeModel2.DEFAULT_BOOLEAN);
    }

    static final SettingsModelBoolean getAsCollectionModel() {
        return new SettingsModelBoolean(DocumentVectorAdapterConfigKeys2.CFGKEY_ASCOLLECTION,
            DocumentVectorAdapterNodeModel2.DEFAULT_ASCOLLECTION);
    }

    static final SettingsModelString getColumnModel() {
        return new SettingsModelString(DocumentVectorAdapterConfigKeys2.CFGKEY_VALUE_COL,
            DocumentVectorAdapterNodeModel2.DEFAULT_COL);
    }

    static final SettingsModelString getDocumentColModel() {
        return new SettingsModelString(DocumentVectorAdapterConfigKeys2.CFGKEY_DOC_COL, "");
    }

    static SettingsModelFilterString getVectorColumnsModel() {
        return new SettingsModelFilterString(DocumentVectorAdapterConfigKeys2.CFGKEY_VECTOR_COLUMNS);
    }

    static SettingsModelBoolean getUseModelSettings() {
        return new SettingsModelBoolean(DocumentVectorAdapterConfigKeys2.CFGKEY_USE_MODEL_SETTINGS, true);
    }

    /** Config key for filter configuration. */
    private static final String CFG_CONFIGROOTNAME = "filter config";

    private final SettingsModelString m_columnModel = getColumnModel();

    private final SettingsModelBoolean m_booleanModel = getBooleanModel();

    private final SettingsModelBoolean m_useModelPortSettingsModel = getUseModelSettings();

    private final SettingsModelBoolean m_asCollectionCell = getAsCollectionModel();

    private final SettingsModelFilterString m_vectorColsModel = getVectorColumnsModel();

    private final DialogComponentStringFilter m_stringFilterComponent;

    /**
     * Creates a new instance of {@code DocumentVectorAdapterNodeDialog2}.
     */
    @SuppressWarnings("unchecked")
    DocumentVectorAdapterNodeDialog2() {

        createNewGroup("General settings");
        addDialogComponent(
            new DialogComponentColumnNameSelection(getDocumentColModel(), "Document column", 0, DocumentValue.class));

        m_useModelPortSettingsModel.addChangeListener(e -> checkUncheck());

        addDialogComponent(new DialogComponentBoolean(m_useModelPortSettingsModel, "Use settings from model"));
        closeCurrentGroup();

        createNewGroup("Document vector settings");
        m_booleanModel.addChangeListener(e -> checkUncheck());

        addDialogComponent(new DialogComponentBoolean(m_booleanModel, "Bitvector"));

        addDialogComponent(new DialogComponentColumnNameSelection(m_columnModel, "Vector value", 0, DoubleValue.class));

        addDialogComponent(new DialogComponentBoolean(m_asCollectionCell, "As collection cell"));
        closeCurrentGroup();

        createNewGroup("Feature Column Selection");
        m_stringFilterComponent = new DialogComponentStringFilter(m_vectorColsModel, new String[]{}, false);

        addDialogComponent(m_stringFilterComponent);
        closeCurrentGroup();
        checkUncheck();
    }

    private void checkUncheck() {
        m_booleanModel.setEnabled(!m_useModelPortSettingsModel.getBooleanValue());
        m_asCollectionCell.setEnabled(!m_useModelPortSettingsModel.getBooleanValue());
        m_vectorColsModel.setEnabled(!m_useModelPortSettingsModel.getBooleanValue());
        m_stringFilterComponent.setEnabledComponents(!m_useModelPortSettingsModel.getBooleanValue());

        if (m_useModelPortSettingsModel.getBooleanValue()
            || (m_booleanModel.isEnabled() && m_booleanModel.getBooleanValue())) {
            m_columnModel.setEnabled(false);
        } else {
            m_columnModel.setEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);
        StringFilterConfiguration config = new StringFilterConfiguration(CFG_CONFIGROOTNAME);
        m_stringFilterComponent.saveConfiguration(config);
        config.saveConfiguration(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        DocumentVectorPortObjectSpec modelSpec = (DocumentVectorPortObjectSpec)specs[1];
        // throw exception if no or a wrong model is connected to the model port
        if (!(specs[1] instanceof DocumentVectorPortObjectSpec) || specs[1] == null) {
            throw new NotConfigurableException("No model or wrong model connected to model port!");
        }
        StringFilterConfiguration config = new StringFilterConfiguration(CFG_CONFIGROOTNAME);
        if (!settings.containsKey(CFG_CONFIGROOTNAME)) {
            // set existing columns and set includes if they haven't been set before
            config.loadDefaults(modelSpec.getFeatureSpaceColumns(), true);
        } else {
            config.loadConfigurationForDialog(settings, modelSpec.getFeatureSpaceColumns());
        }
        m_stringFilterComponent.loadConfiguration(config, modelSpec.getFeatureSpaceColumns());
    }
}

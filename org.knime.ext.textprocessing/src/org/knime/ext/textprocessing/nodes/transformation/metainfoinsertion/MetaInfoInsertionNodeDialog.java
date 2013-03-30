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

        addDialogComponent(new DialogComponentBoolean(createKeepKeyValColsModel(), "Keep key, value cols"));
    }
}

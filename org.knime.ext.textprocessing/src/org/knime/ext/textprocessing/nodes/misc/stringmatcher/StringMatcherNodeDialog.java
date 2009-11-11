package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**The configuration dialog for the string matcher.
 *
 * @author adae
 *
 */
public class StringMatcherNodeDialog extends DefaultNodeSettingsPane {



    /** String for first column. */
     private final SettingsModelString m_col1;
     /** String for second column.*/
    private final SettingsModelString m_col2;
    /** boolean for to be sorted in memory or not. */
    private final SettingsModelBoolean m_sortInMemory;
    /** boolean for showing the found minimal distance. */
    private final SettingsModelBoolean m_showdist;
    /** the Panel for the cost. */
    private CostPanel m_costPanel;
    /** Integer for the maximal number of related words. */
    private final SettingsModelInteger m_numberofrelatedwords;


    /**
     * Constructor for StringMatcherNodeDialog.
     */
    @SuppressWarnings("unchecked")
    public StringMatcherNodeDialog() {
          super();
        m_col1 = new SettingsModelString(StringMatcherNodeModel.CFG_COL1, null);
        m_col2 = new SettingsModelString(StringMatcherNodeModel.CFG_COL2, null);
        m_numberofrelatedwords =
               new SettingsModelIntegerBounded(
                        StringMatcherNodeModel.CFG_NUMBER,
                        3 ,
                        1 ,
                        Integer.MAX_VALUE);
        m_sortInMemory = new SettingsModelBoolean(
                StringMatcherNodeModel.CFG_SORT_IN_MEMORY, false);
        m_showdist = new SettingsModelBoolean(
                StringMatcherNodeModel.CFG_SHOW_DISTANCE, true);
        
        // adding the tab to configure the costs.
        m_costPanel = new CostPanel();
        addTab("Cost Panel", m_costPanel);

        // adding the column selection
        addDialogComponent(new DialogComponentColumnNameSelection(
                m_col1, "Search string column:",
                0, StringValue.class));
        addDialogComponent(new DialogComponentColumnNameSelection(
                m_col2, "Ditionary column:",
                1, StringValue.class));
        addDialogComponent(new DialogComponentNumber(m_numberofrelatedwords,
                StringMatcherNodeModel.CFG_NUMBER, 1, 5));

        // adding the buttons.
        setHorizontalPlacement(true);
        
        addDialogComponent(new DialogComponentBoolean(m_sortInMemory, 
                "Process in memory"));
        addDialogComponent(new DialogComponentBoolean(m_showdist,
                "Display the found distance"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
    throws InvalidSettingsException {
          assert settings != null;
          m_costPanel.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        assert settings != null;
        assert specs != null;
        try {
                  m_costPanel.loadSettings(settings);
            } catch (InvalidSettingsException e) {
                  e.printStackTrace();
            }
    }
}

package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import java.awt.GridLayout;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * Panel for the costs.
 *
 * @author Iris Adae, University of Konstanz
 */
public class CostPanel extends JPanel {

  /** */
  private static final long serialVersionUID = 0;
  private final SettingsModelInteger m_wd;
  private final SettingsModelInteger m_wi;
  private final SettingsModelInteger m_wc;
  private final SettingsModelInteger m_ws;

      /**
       * Constructor for the cost panel.
       */
  public CostPanel() {
          super();
          this.setLayout(new GridLayout(2, 2));

          m_wd = new SettingsModelIntegerBounded(StringMatcherNodeModel.CFG_WD, 1, 0, Integer.MAX_VALUE);
          m_wi = new SettingsModelIntegerBounded(StringMatcherNodeModel.CFG_WI, 1, 0, Integer.MAX_VALUE);
          m_wc = new SettingsModelIntegerBounded(StringMatcherNodeModel.CFG_WC, 1, 0, Integer.MAX_VALUE);
          m_ws = new SettingsModelIntegerBounded(StringMatcherNodeModel.CFG_WS, 1, 0, Integer.MAX_VALUE);

        final DialogComponentNumber wd = new DialogComponentNumber(m_wd, StringMatcherNodeModel.CFG_WD, 1);
        final DialogComponentNumber wi = new DialogComponentNumber(m_wi, StringMatcherNodeModel.CFG_WI, 1);
        final DialogComponentNumber wc = new DialogComponentNumber(m_wc, StringMatcherNodeModel.CFG_WC, 1);
        final DialogComponentNumber ws = new DialogComponentNumber(m_ws, StringMatcherNodeModel.CFG_WS, 1);

        add(wd.getComponentPanel());
        add(wi.getComponentPanel());
        add(wc.getComponentPanel());
        add(ws.getComponentPanel());
     }

  /**
   * save Settings.
   * @param settings the settings object to write the settings.
   */
  public void saveSettings(final NodeSettingsWO settings) {
          m_wd.saveSettingsTo(settings);
          m_wi.saveSettingsTo(settings);
          m_wc.saveSettingsTo(settings);
          m_ws.saveSettingsTo(settings);
     }

  /**
   * Load settings.
   * @param settings the settings object to load from.
   * @throws InvalidSettingsException if the setting are not valid
   */
  public void loadSettings(final NodeSettingsRO settings)
     throws InvalidSettingsException {
          m_wd.loadSettingsFrom(settings);
          m_wi.loadSettingsFrom(settings);
          m_wc.loadSettingsFrom(settings);
          m_ws.loadSettingsFrom(settings);
     }
}

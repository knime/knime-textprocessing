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

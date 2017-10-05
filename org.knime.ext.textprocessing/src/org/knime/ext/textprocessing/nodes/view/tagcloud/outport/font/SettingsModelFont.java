/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * -------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud.outport.font;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.config.Config;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

import java.awt.Color;
import java.awt.Font;

import javax.swing.event.ChangeListener;


/**
 * Settings model that holds font information.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class SettingsModelFont extends SettingsModel {

    private static final String CFG_FAMILY = "fontFamily";
    private static final String CFG_TYPE = "fontType";
    private static final String CFG_SIZE = "fontSize";
    private static final String CFG_COLOR = "fontColor";

    private final String m_configName;

    private Font m_font;

    private Color m_color;


    /**
     * Creates a new object holding a {@link Font} and {@link Color}.
     *
     * @param configName the identifier the value is stored with in the
     *            {@link org.knime.core.node.NodeSettings} object
     * @param defaultFont the initial {@link Font}
     */
    public SettingsModelFont(final String configName,
            final Font defaultFont) {
        this(configName, defaultFont, Color.BLACK);
    }

    /**
     * Creates a new object holding a {@link Font} and {@link Color}.
     *
     * @param configName the identifier the value is stored with in the
     *            {@link org.knime.core.node.NodeSettings} object
     * @param defaultFont the initial {@link Font}
     * @param defaultColor the initial {@link Color}
     */
    public SettingsModelFont(final String configName,
            final Font defaultFont, final Color defaultColor) {
        if ((configName == null) || (configName == "")) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
        if (defaultFont == null) {
            throw new NullPointerException("defaultFont must not be null");
        }
        if (defaultColor == null) {
            throw new NullPointerException("defaultColor must not be null");
        }
        m_configName = configName;
        m_font = defaultFont;
        m_color = defaultColor;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected SettingsModelFont createClone() {
        return new SettingsModelFont(getConfigName(), getFont(), getColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModelTypeID() {
        return "SMID_font";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigName() {
        return m_configName;
    }

    /**
     * @return the
     */
    public Font getFont() {
        return m_font;
    }

    /**
     * @param font the font to set
     */
    public void setFont(final Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font must not be null");
        }
        final boolean sameValue = font.equals(m_font);
        m_font = font;

        if (!sameValue) {
            notifyChangeListeners();
        }
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return m_color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(final Color color) {
        if (color == null) {
            throw new IllegalArgumentException("colot must not be null");
        }
        final boolean sameValue = color.equals(m_color);
        m_color = color;

        if (!sameValue) {
            notifyChangeListeners();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prependChangeListener(final ChangeListener l) {
        //TK_TODO: This has to be removed when moved to core
        super.prependChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForDialog(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        // use the current value, if no value is stored in the settings
        if (settings.containsKey(m_configName)) {
            try {
                final Config config = settings.getConfig(m_configName);
                final String family = config.getString(CFG_FAMILY);
                final int type = config.getInt(CFG_TYPE);
                final int size = config.getInt(CFG_SIZE);
                setFont(new Font(family, type, size));
                final int color = config.getInt(CFG_COLOR);
                m_color = new Color(color, true);
            } catch (final InvalidSettingsException e) {
                throw new NotConfigurableException(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForDialog(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        //try to get all values
        final Config config = settings.getConfig(m_configName);
        config.getString(CFG_FAMILY);
        config.getInt(CFG_TYPE);
        config.getInt(CFG_SIZE);
        config.getInt(CFG_COLOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // no default value, throw an exception instead
        final Config config = settings.getConfig(m_configName);
        final String family = config.getString(CFG_FAMILY);
        final int type = config.getInt(CFG_TYPE);
        final int size = config.getInt(CFG_SIZE);
        setFont(new Font(family, type, size));
        final int color = config.getInt(CFG_COLOR);
        setColor(new Color(color, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForModel(final NodeSettingsWO settings) {
        final Config config = settings.addConfig(m_configName);
        config.addString(CFG_FAMILY, getFont().getFamily());
        config.addInt(CFG_TYPE, getFont().getStyle());
        config.addInt(CFG_SIZE, getFont().getSize());
        config.addInt(CFG_COLOR, getColor().getRGB());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " ('" + m_configName + "')";
    }
}

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
 * History
 *   23.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud.outport.font;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides a component for color choosing. This component for the default
 * dialog allows to select a color using the the {@link JColorChooser} dialog.
 * The component is either painted as {@link JButton}
 * (colorPreview flag equals false) or as {@link JLabel}
 * (colorPreview flag equals false) with color preview box next to the label.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DialogComponentFontChooser extends DialogComponent {

    private final JButton m_button;
    private final boolean m_showPreview;
    private JTextField m_preview = null;

    /**
     * Creates new instance of <code>DialogComponentButton</code> with given
     * text to set as button label.
     * @param model an already created settings model
     * @param label The button label.
     * @param showPreview <code>true</code> if a color preview box should
     * be painted next to the label otherwise a button is displayed with
     * the given label
     * @param previewDimension the size of the preview field
     */
    public DialogComponentFontChooser(final SettingsModelFont model,
            final String label, final boolean showPreview,
            final Dimension previewDimension) {
        super(model);
        m_showPreview = showPreview;
        final MouseAdapter mouseListener = new MouseAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!getModel().isEnabled()) {
                    return;
                }
                final JFontChooser fontChooser = new JFontChooser(getFont());
                final int result = fontChooser.showDialog(
                        DialogComponentFontChooser.this.getComponentPanel());
              //check if the user has pressed ok
                if (result == JFontChooser.OK_OPTION) {
                    setFont(fontChooser.getSelectedFont());
                }
            }
        };
        getComponentPanel().setLayout(new FlowLayout());
        m_button = new JButton(label);
        m_button.addMouseListener(mouseListener);
        getComponentPanel().add(m_button);
        if (showPreview) {
            final JPanel samplePanel = new JPanel();
            final Border lowered = BorderFactory.createLoweredBevelBorder();
            m_preview = new JTextField("Preview Font");
            m_preview.setBorder(lowered);
            m_preview.setPreferredSize(previewDimension);
            samplePanel.add(m_preview, BorderLayout.CENTER);
            getComponentPanel().add(samplePanel);
        }
        // update the button, whenever the model changes - make sure we get
        // notified first.
        // TK_TODO: This has to be changed when moved to core
        ((SettingsModelFont)getModel()).prependChangeListener(
                new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateComponent();
            }
        });
        //call this method to be in sync with the settings model
        updateComponent();
    }

    /**
     * Updates the color of the button if desired.
     */
    private void updatePreview() {
        if (m_showPreview && m_preview != null) {
            m_preview.setFont(getFont());
        }
    }

    /**
     * @return the current color value
     */
    public Color getColor() {
        return ((SettingsModelFont)getModel()).getColor();
    }

    /**
     * @return the current font
     */
    public Font getFont() {
        return ((SettingsModelFont)getModel()).getFont();
    }

    /**
     * @param newValue the new color value
     */
    public void setColor(final Color newValue) {
        ((SettingsModelFont)getModel()).setColor(newValue);
    }

    /**
     * @param newValue the new font value
     */
    public void setFont(final Font newValue) {
        ((SettingsModelFont)getModel()).setFont(newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
            throws NotConfigurableException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_button.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        m_button.setToolTipText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateComponent() {
        updatePreview();
        setEnabledComponents(getModel().isEnabled());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsBeforeSave()
            throws InvalidSettingsException {
        // Nothing to do ...
    }
}

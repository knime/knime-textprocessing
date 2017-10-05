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
 * ---------------------------------------------------------------------
 *
 * History
 *   19.11.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.viz.plotter.props.PropertiesTab;

/**
 * This tab is used to improve the look of the tag cloud.
 *
 * There will be two sliders for the minimal and maximal font size. Each slider
 * is supported with a spinner. On the one hand, to show the user the currently
 * selected value as a number. And on the other hand, to give the possibility
 * for an exact adjustment of the values.
 *
 * There will also be a selection box containing the three different
 * possibilities of font size distribution (linear, logarithm or exponential)
 *
 * The alpha slider can be used to change the color intensity of the terms. The
 * lower the value the more labels will loose intensity.
 *
 * The bold slider changes the bold distribution. The higher the slider, the
 * more terms with the highest values will be painted in bold.
 *
 * All values can be received and set with the typical get - and set methods.
 *
 * If any of the above elements is changed or activated the listener, given
 * through addChangeListener will be invoked.
 *
 * The default button restores all default value.
 *
 * When the default button is hit, the listener given through
 * addReverseChangeListener will be invoked.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
public class TagCloudFontStyleTab extends PropertiesTab {

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 1784319750725018865L;

    private static final int CFG_SLIDER_MAX_FONTSIZE = 150;

    private static final int CFG_SLIDER_MIN_FONTSIZE = 0;

    private static final int CFG_SPINNER_STEPSIZE = 3;

    private static final int CFG_SLIDER_STEPSIZE = 20;

    private static final int CFG_SPINNER_MAX_FONTSIZE = 999;

    private static final int CFG_SPINNER_MIN_FONTSIZE = 0;

    private ChangeListener m_listener;

    private ChangeListener m_reverselistener;

    private JSlider m_minSlider;

    private JSpinner m_minSpinner;

    private JSlider m_maxSlider;

    private JSpinner m_maxSpinner;

    private JComboBox m_fontCombo;

    private JComboBox m_calcType;

    private JSlider m_alphaSlider;

    private JSlider m_boldSlider;

    private JButton m_reverse;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultName() {
        return "Font Style";
    }

    /**
     * Updates the complete fontstyle with the given parameters.
     *
     * @param min minimal fontsize
     * @param max maximal fontsize
     * @param fontname Name of the font
     * @param calcID the calctype or 0, then linear is used
     * @param bold new bold value
     * @param alpha new alpha value
     */
    public void setAll(final int min, final int max, final String fontname,
            final int calcID, final int bold, final int alpha) {
        setMinMaxFonsize(min, max);
        setFontname(fontname);
        setcalcID(calcID);
        setBold(bold);
        setAlpha(alpha);
    }

    /**
     * Updates the font style with the sizes and the font name and paints the
     * panel.
     *
     * @param min minimal font size
     * @param max maximal font size
     * @param fontname Name of the font
     * @param calcID the calc type or 0, then linear is used
     * @param bold the bold value
     * @param alpha the alpha value
     */
    public void update(final int min, final int max, final String fontname,
            final int calcID, final int bold, final int alpha) {
        removeAll();

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();

        String[] fontnames = ge.getAvailableFontFamilyNames();

        m_fontCombo = new JComboBox();
        int curentpos = 0;
        for (int i = 0; i < fontnames.length; i++) {
            if (fontnames[i].equals(fontname)) {
                curentpos = i;
            }
            m_fontCombo.addItem(fontnames[i]);
        }
        m_fontCombo.setSelectedIndex(curentpos);
        m_fontCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (m_listener != null) {
                    m_listener.stateChanged(new ChangeEvent(this));
                }

            }
        });
        m_calcType = new JComboBox();
        for (int i = 0; i < TagCloudConfigKeys.CFG_TYPEOFFSCALCI.length; i++) {
            m_calcType.addItem(TagCloudConfigKeys.CFG_TYPEOFFSCALCI[i]);
        }
        m_calcType.setSelectedIndex(calcID);
        m_calcType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (m_listener != null) {
                    m_listener.stateChanged(new ChangeEvent(this));
                }

            }
        });

        m_minSlider = createMinMaxSlider(min);
        m_minSpinner = createMinMaxSpinner(min);
        m_maxSlider = createMinMaxSlider(max);
        m_maxSpinner = createMinMaxSpinner(max);
        m_alphaSlider = createAlphaSlider(alpha);
        m_boldSlider = createAlphaSlider(bold);
        m_minSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                if (m_minSlider.getValueIsAdjusting()) {
                    m_minSpinner.setValue(new Integer(m_minSlider.getValue()));
                }
                if (m_minSlider.getValue() > m_maxSlider.getValue()) {
                    m_maxSlider.setValue(m_minSlider.getValue());
                    m_maxSpinner.setValue(new Integer(m_minSlider.getValue()));

                }
                if (!m_minSlider.getValueIsAdjusting()) {
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });
        m_maxSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                if (m_maxSlider.getValueIsAdjusting()) {
                    m_maxSpinner.setValue(new Integer(m_maxSlider.getValue()));
                }

                if (m_minSlider.getValue() > m_maxSlider.getValue()) {
                    m_minSlider.setValue(m_maxSlider.getValue());
                    m_minSpinner.setValue(new Integer(m_maxSlider.getValue()));
                }

                if (!m_maxSlider.getValueIsAdjusting()) {
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });
        m_minSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent name) {
                if (getMinFontsize() < CFG_SLIDER_MAX_FONTSIZE) {
                    m_minSlider.setValue((Integer)m_minSpinner.getValue());
                } else {
                    m_minSlider.setValue(CFG_SLIDER_MAX_FONTSIZE);
                    if (getMaxFontsize() < getMinFontsize()) {
                        m_maxSpinner.setValue(m_minSpinner.getValue());
                    }
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });
        m_maxSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent name) {
                if (getMaxFontsize() <= CFG_SLIDER_MAX_FONTSIZE) {
                    m_maxSlider.setValue((Integer)m_maxSpinner.getValue());
                } else {
                    m_maxSlider.setValue(CFG_SLIDER_MAX_FONTSIZE);
                    if (getMaxFontsize() < getMinFontsize()) {
                        m_minSpinner.setValue(m_maxSpinner.getValue());
                    }
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });
        m_alphaSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                if (!m_alphaSlider.getValueIsAdjusting()) {
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });

        m_boldSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {

                if (!m_boldSlider.getValueIsAdjusting()) {
                    if (m_listener != null) {
                        m_listener.stateChanged(new ChangeEvent(this));
                    }
                }
            }
        });
        m_reverse = new JButton("Default");
        m_reverse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (m_reverselistener != null) {
                    m_reverselistener.stateChanged(new ChangeEvent(this));
                }
            }
        });
        JPanel newTab = generateFontstyleTab();
        add(newTab);
    }

    /**
     * @return a JPanel containing the style of the Tab.
     */
    private JPanel generateFontstyleTab() {
        JPanel newTab = new JPanel();
        newTab.setLayout(new BoxLayout(newTab, BoxLayout.PAGE_AXIS));
        newTab.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(4, 2, 3, 3);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        c.gridy = 0; // first line

        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Font"), c);

        c.gridx++;
        c.insets = new Insets(4, 10, 2, 2);
        c.anchor = GridBagConstraints.WEST;
        newTab.add(m_fontCombo, c);

        c.gridx++;
        c.insets = new Insets(4, 2, 2, 2);
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Distribution"), c);

        c.gridx++;
        c.insets = new Insets(4, 10, 2, 2);
        c.anchor = GridBagConstraints.WEST;
        newTab.add(m_calcType, c);

        c.gridx++;

        newTab.add(m_reverse, c);

        c.gridy++; // second line
        c.insets = new Insets(4, 2, 2, 2);

        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Minimum fontsize"), c);

        c.gridx++;
        c.anchor = GridBagConstraints.WEST;
        newTab.add(m_minSlider, c);
        c.gridx++;
        newTab.add(m_minSpinner, c);

        c.gridy++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Maximum fontsize"), c);

        c.gridx++;
        c.anchor = GridBagConstraints.WEST;

        newTab.add(m_maxSlider, c);
        c.gridx++;
        newTab.add(m_maxSpinner, c);

        c.gridy++; // fourth line
        c.gridx = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Color intensity"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        newTab.add(m_alphaSlider, c);

        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        newTab.add(new JLabel("Bold level"), c);

        c.gridx++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        newTab.add(m_boldSlider, c);

        return newTab;
    }

    /**
     * creates a {@link JSlider}.
     *
     * @param value the preselected value
     * @return a new {@link JSlider}
     */
    private JSlider createMinMaxSlider(final int value) {
        JSlider t = new JSlider(CFG_SLIDER_MIN_FONTSIZE,
                CFG_SLIDER_MAX_FONTSIZE, value);
        t.setLabelTable(t.createStandardLabels(CFG_SLIDER_STEPSIZE));
        t.setPaintLabels(true);
        t.setPaintTicks(true);
        t.setMinorTickSpacing(CFG_SLIDER_STEPSIZE);
        return t;
    }

    /**
     * creates a {@link JSpinner}.
     *
     * @param value the preselected value
     * @return a new {@link JSpinner}
     */
    private JSpinner createMinMaxSpinner(final int value) {
        JSpinner t = new JSpinner(new SpinnerNumberModel(value,
                CFG_SPINNER_MIN_FONTSIZE, CFG_SPINNER_MAX_FONTSIZE,
                CFG_SPINNER_STEPSIZE));
        return t;
    }

    /**
     * creates a {@link JSlider} for selecting the alphavalue.
     *
     * @return a new {@link JSlider}
     */
    private JSlider createAlphaSlider(final int init) {
        JSlider t = new JSlider(0, 100, init);
        t.setPaintTicks(true);
        t.setMinorTickSpacing(10);
        return t;
    }

    /**
     * Adds the listener to the sliders.
     *
     * @param a ChangeListener
     */
    public void addChangeListener(final ChangeListener a) {
        m_listener = a;
    }

    /**
     * Adds the listener to the reverse button.
     *
     * @param a ChangeListener
     */
    public void addReverseChangeListener(final ChangeListener a) {
        m_reverselistener = a;
    }

    /**
     * @return font name selected by the user
     */
    public String getFontName() {
        return (String)m_fontCombo.getSelectedItem();
    }

    /**
     * @return min font size selected by the user
     */
    public int getMinFontsize() {
        return ((Integer)m_minSpinner.getValue()).intValue();
    }

    /**
     * @return max font size selected by the user
     */
    public int getMaxFontsize() {
        return ((Integer)m_maxSpinner.getValue()).intValue();
    }

    /**
     * @return the selected calculation type
     */
    public int getCalcType() {
        return m_calcType.getSelectedIndex();
    }

    /**
     * Sets the value of the minimal and maximal font size sliders and the
     * associated spinners.
     *
     * @param min new chosen minimal value
     * @param max new chosen maximal value
     */
    public void setMinMaxFonsize(final int min, final int max) {
        m_maxSlider.setValue(max);
        m_minSlider.setValue(min);
        m_maxSpinner.setValue(max);
        m_minSpinner.setValue(min);
    }

    /**
     * @param alpha the current value of the alpha slider.
     */
    public void setAlpha(final int alpha) {
        m_alphaSlider.setValue(alpha);
    }

    /**
     * @return the value for the alpha/color intensity calculation.
     */
    public int getAlpha() {
        return m_alphaSlider.getValue();
    }

    /**
     * @return the current value of the bold slider.
     */
    public int getbold() {
        return m_boldSlider.getValue();
    }

    /**
     * @param fontname
     */
    private void setFontname(final String fontname) {
        m_fontCombo.setSelectedItem(fontname);
    }

    /**
     * @param calcID
     */
    private void setcalcID(final int calcID) {
        m_calcType.setSelectedIndex(calcID);
    }

    /**
     * @param bold
     */
    private void setBold(final int bold) {
        m_boldSlider.setValue(bold);
    }
}

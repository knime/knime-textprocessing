/*
 * ------------------------------------------------------------------------
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
 *   27.08.2008 (Hermann Azong): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2.slider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
abstract class Animation implements ActionListener {

    /** start value (typically in pixels) */
    protected int startValue = 0;

    /** end value (typically in pixels) */
    protected int endValue = 0;

    /** duration over which the animation takes place */
    protected long durationMillis = 0;

    /** A value (difference of start and end values) that corresponds to value per millisecond */
    protected double valuePerMilli = 0.0;

    /** The ctm of the last performed animation operation */
    protected long startMillis;

    /**
     *
     */
    protected Timer timer;

    /**
     *
     */
    protected double value = 0;

    /**
     * Constructor where you specify <i>time</i> between the two pixel values.
     *
     * @param sV
     * @param eV
     * @param dM
     */
    public Animation(final int sV, final int eV, final int dM) {
        startValue = sV;
        this.endValue = eV;
        this.durationMillis = dM;

        // create the value per millis.
        this.valuePerMilli = ((double)(endValue - startValue)) / ((double)durationMillis);
    }

    /**
     * Constructor where you specify <i>value/ms</i> between the two pixel values.
     *
     * @param sV
     * @param eV
     * @param vP
     */
    public Animation(final int sV, final int eV, final double vP) {

        this.startValue = sV;
        this.endValue = eV;
        this.valuePerMilli = vP;
    }

    /**
     * @param durationMs
     */
    public Animation(final int durationMs) {
        this.durationMillis = durationMs;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        //get ctm
        long ctm = System.currentTimeMillis();

        //get difference of this ctm with the last ctm
        double millisPassed = ctm - startMillis;

        // This may be 0 if millisPassed is small enough
        double i = millisPassed * valuePerMilli;
        if (i == 0) {
            System.err.println("WARNING: Animation is incrementing by zero... potential infinite loop");
            i++;
        }

        value += i;

        //replace old ctm with new one.
        this.startMillis = ctm;

        if (startValue < endValue && value >= endValue) {
            value = Math.min(value, endValue);
            render((int)value);
            stop();
        } else if (startValue > endValue && value <= endValue) {
            value = Math.max(value, endValue);
            render((int)value);
            stop();
        } else {
            render((int)value);
        }
    }

    /**
     *
     */
    public void start() {
        this.startMillis = System.currentTimeMillis();
        this.value = startValue;
        timer = new Timer(50, this);
        starting();
        timer.restart();
    }

    /**
     *
     */
    public void stop() {
        timer.stop();
        stopped();
    }

    //

    /**
     * @param value1
     */
    protected abstract void render(int value1);

    /**
     * Optional starting method for an animation. Ie. before animation commences code to initialise the animation can be
     * placed here.
     */
    protected void starting() {
    }

    /**
     * Optional stop method for an animation. Ie. clean up code, or rendering code that happens at the end of the
     * animation
     */
    protected void stopped() {
    }

    /**
     * @return endValue
     */
    public int getEndValue() {
        return endValue;
    }

    /**
     * @param eV
     */
    public void setEndValue(final int eV) {
        this.endValue = eV;
        if (durationMillis > 0) {
            this.valuePerMilli = ((double)(endValue - startValue)) / ((double)durationMillis);
        }
    }

    /**
     * @return startValue
     */
    public int getStartValue() {
        return startValue;
    }

    /**
     * @param sV
     */
    public void setStartValue(final int sV) {
        this.startValue = sV;
        if (durationMillis > 0) {
            this.valuePerMilli = ((double)(endValue - startValue)) / ((double)durationMillis);
        }
    }

}

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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
*
* @author Hermann Azong, KNIME.com, Berlin, Germany
*/
public class ArrowPanel extends JPanel implements SwingConstants {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    protected int direction;

    private final String arrowName;

    private Color shadow;

    private Color darkShadow;

    private Color highlight;

    /**
     * @param dir
     * @param name
     */
    public ArrowPanel(final int dir, final String name) {
        this(dir, UIManager.getColor("control"), UIManager.getColor("controlShadow"),
            UIManager.getColor("controlDkShadow"), UIManager.getColor("controlLtHighlight"), name);
    }

    /**
     * @param dir
     * @param background
     * @param colorShadow
     * @param colorDarkShadow
     * @param colorHighlight
     * @param name
     */
    public ArrowPanel(final int dir, final Color background, final Color colorShadow, final Color colorDarkShadow,
        final Color colorHighlight, final String name) {
        super();
        setRequestFocusEnabled(false);
        setDirection(dir);
        setBackground(background);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.shadow = colorShadow;
        this.darkShadow = colorDarkShadow;
        this.highlight = colorHighlight;
        this.arrowName = name;
    }

    /**
     * Returns the direction of the arrow.
     *
     * @return direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @return arrowName
     */
    public String getArrowName() {
        return arrowName;
    }

    /**
     * Sets the direction of the arrow.
     *
     * @param dir the direction of the arrow; one of of {@code
     *            SwingConstants.NORTH}, {@code SwingConstants.SOUTH}, {@code
     *            SwingConstants.EAST} or {@code SwingConstants.WEST}
     */
    public void setDirection(final int dir) {
        direction = dir;
    }

    @Override
    public void paint(final Graphics g) {
        Color origColor;
        int w, h, size;

        w = getSize().width;
        h = getSize().height;
        origColor = g.getColor();

        g.setColor(getBackground());
        g.fillRect(1, 1, w - 2, h - 2);

        // If there's no room to draw arrow, bail
        if (h < 5 || w < 5) {
            g.setColor(origColor);
            return;
        }

        // Draw the arrow
        size = Math.min((h - 4) / 3, (w - 4) / 3);
        size = Math.max(size, 2);
        paintTriangle(g, (w - size) / 2, (h - size) / 2, size, direction, false);

        g.setColor(origColor);
    }

    /**
     * Paints a triangle.
     *
     * @param g
     * @param x
     * @param y
     * @param size
     * @param dir
     * @param isEnabled
     */
    public void paintTriangle(final Graphics g, final int x, final int y, int size, final int dir,
        final boolean isEnabled) {
        Color oldColor = g.getColor();
        int mid, i, j;

        j = 0;
        size = Math.max(size, 2);
        mid = (size / 2) - 1;

        g.translate(x, y);
        if (isEnabled) {
            g.setColor(darkShadow);
        } else {
            g.setColor(shadow);
        }

        switch (dir) {
            case NORTH:
                for (i = 0; i < size; i++) {
                    g.drawLine(mid - i, i, mid + i, i);
                }
                if (!isEnabled) {
                    g.setColor(highlight);
                    g.drawLine(mid - i + 2, i, mid + i, i);
                }
                break;
            case SOUTH:
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(highlight);
                    for (i = size - 1; i >= 0; i--) {
                        g.drawLine(mid - i, j, mid + i, j);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(shadow);
                }

                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                break;
            case WEST:
                for (i = 0; i < size; i++) {
                    g.drawLine(i, mid - i, i, mid + i);
                }
                if (!isEnabled) {
                    g.setColor(highlight);
                    g.drawLine(i, mid - i + 2, i, mid + i);
                }
                break;
            case EAST:
                if (!isEnabled) {
                    g.translate(1, 1);
                    g.setColor(highlight);
                    for (i = size - 1; i >= 0; i--) {
                        g.drawLine(j, mid - i, j, mid + i);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(shadow);
                }

                j = 0;
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(j, mid - i, j, mid + i);
                    j++;
                }
                break;
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     * @param d
     */
    public void changeDirection(final int d) {
        setDirection(d);
    }

}

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
 *   27.08.2008 (Hermann Azong): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2.slider;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class SideBar extends JPanel {

    private static final long serialVersionUID = 1L;

    /** box layout to contain side bar sections arranged vertically */
    private BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);

    /** the currently expanded section */
    private SidebarSection currentSection = null;

    SideBarMode thisMode;

    private boolean showArrow;

    private boolean animate = false;

    /**
     * @param mode
     * @param arrow
     * @param preferredWidth
     * @param animation
     */
    public SideBar(final SideBarMode mode, final boolean arrow, final int preferredWidth, final boolean animation) {

        this.setShowArrow(arrow);
        this.thisMode = mode;
        this.setAnimate(animation);

        setLayout(boxLayout);

        setFocusable(false);

        revalidate();
    }

    /**
     * @param newSection
     */
    public void addSection(final SidebarSection newSection) {
        add(newSection);

        newSection.collapse(false);
    }

    /**
     * @param section
     * @return boolean
     */
    public boolean isCurrentExpandedSection(final SidebarSection section) {
        return (section != null) && (currentSection != null) && section.equals(currentSection);
    }

    /**
     * @return thisMode
     */
    public SideBarMode getMode() {
        return thisMode;
    }

    /**
     * @return currentSection
     */
    public SidebarSection getCurrentSection() {
        return currentSection;
    }

    /**
     * @param section
     */
    public void setCurrentSection(final SidebarSection section) {
        currentSection = section;
    }

    /**
     * @return the showArrow
     */
    public boolean isShowArrow() {
        return showArrow;
    }

    /**
     * @param showArrow the showArrow to set
     */
    public void setShowArrow(@SuppressWarnings("hiding") final boolean showArrow) {
        this.showArrow = showArrow;
    }

    /**
     * @return the animate
     */
    public boolean isAnimate() {
        return animate;
    }

    /**
     * @param animate the animate to set
     */
    public void setAnimate(@SuppressWarnings("hiding") final boolean animate) {
        this.animate = animate;
    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     *
     */
    public enum SideBarMode {

        /**
         * Top Level
         */
        TOP_LEVEL,

        /**
         * Inner Level
         */
        INNER_LEVEL;
    }

}

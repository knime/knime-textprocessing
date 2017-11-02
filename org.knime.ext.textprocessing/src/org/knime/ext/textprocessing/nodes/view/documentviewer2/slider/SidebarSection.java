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
 * ---------------------------------------------------------------------
 *
 * History
 *   27.08.2008 (Hermann Azong): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2.slider;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

import org.knime.ext.textprocessing.nodes.view.documentviewer2.slider.SideBar.SideBarMode;

/**
 * Panel that contains both the title/header part and the content part.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 *
 */

public class SidebarSection extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private int minComponentHeight = 40;

    /**
     *
     */
    private int minComponentWidth = 250;

    /**
     *
     */
    private JComponent titlePanel;

    private SideBar sideBarOwner;

    /**
     *
     */
    public JComponent contentPane; //sidebar section's content

    private ArrowPanel arrowPanel;

    private int calculatedHeight;

    /**
     * @param owner
     * @param text
     * @param component
     * @param icon
     */
    public SidebarSection(final SideBar owner, final String text, final JComponent component, final Icon icon) {
        this(owner, new JLabel(text), component, icon);
    }

    /**
     * Construct a new sidebar section with the specified owner and model.
     *
     * @param owner - SideBar
     * @param titleComponent
     * @param component
     * @param icon
     */
    public SidebarSection(final SideBar owner, final JComponent titleComponent, final JComponent component,
        final Icon icon) {

        if (owner.thisMode == SideBar.SideBarMode.INNER_LEVEL) {
            minComponentHeight = 30;
        } else {
            minComponentHeight = 40;
        }

        this.contentPane = component;

        sideBarOwner = owner;

        titlePanel = new JPanel();
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {

                if (SidebarSection.this != sideBarOwner.getCurrentSection()) {
                    if (sideBarOwner.getCurrentSection() != null) {
                        sideBarOwner.getCurrentSection().collapse(true);
                    }

                    expand(); //expand this!
                } else {
                    collapse(true);
                }
            }
        });

        //absolute layout
        setLayout(new BorderLayout());

        add(titlePanel, BorderLayout.NORTH);

        titlePanel.setLayout(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(this.getPreferredSize().width, minComponentHeight));
        titlePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        arrowPanel = new ArrowPanel(SwingConstants.EAST, "Name");
        arrowPanel.setPreferredSize(new Dimension(40, 40));

        if (sideBarOwner.isShowArrow()) {
            //add into tab panel the arrow and labels.
            titlePanel.add(arrowPanel, BorderLayout.EAST);
        }

        titlePanel.add(new JLabel(icon), BorderLayout.WEST);

        titleComponent
            .setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2), titleComponent.getBorder()));
        titlePanel.add(titleComponent);

        add(component, BorderLayout.CENTER);

        setCursor(new Cursor(Cursor.HAND_CURSOR));

        revalidate();
    }

    /**
     *
     */
    public void expand() {
        sideBarOwner.setCurrentSection(this);

        arrowPanel.changeDirection(SwingConstants.SOUTH);
        arrowPanel.updateUI();

        calculatedHeight = -1;
        calculatedHeight = sideBarOwner.getSize().height;

        if (this.sideBarOwner.isAnimate()) {
            SidebarAnimation anim = new SidebarAnimation(this, 200); // ANIMATION BIT

            anim.setStartValue(minComponentHeight);
            anim.setEndValue(calculatedHeight);
            anim.start();
        } else {
            if (sideBarOwner.thisMode == SideBarMode.INNER_LEVEL) {
                calculatedHeight = 1000;
                Dimension d = new Dimension(Integer.MAX_VALUE, calculatedHeight);
                setMaximumSize(d);
                sideBarOwner.setPreferredSize(d);
                contentPane.setVisible(true);
                revalidate();
            } else {
                setMaximumSize(new Dimension(Integer.MAX_VALUE, calculatedHeight));
                contentPane.setVisible(true);
                revalidate();
            }
        }
    }

    /**
     * @param animate
     */
    public void collapse(final boolean animate) {
        // remove reference
        if (sideBarOwner.getCurrentSection() == SidebarSection.this) {
            sideBarOwner.setCurrentSection(null);
        }

        arrowPanel.changeDirection(SwingConstants.EAST);
        arrowPanel.updateUI();

        if (animate && this.sideBarOwner.isAnimate()) {
            SidebarAnimation anim = new SidebarAnimation(this, 200); // ANIMATION BIT
            anim.setStartValue(calculatedHeight);
            anim.setEndValue(minComponentHeight);
            anim.start();
        } else {
            if (sideBarOwner.thisMode == SideBarMode.INNER_LEVEL) {
                setMaximumSize(new Dimension(Integer.MAX_VALUE, titlePanel.getPreferredSize().height));
                contentPane.setVisible(false);
                revalidate();
            } else {
                setMaximumSize(new Dimension(Integer.MAX_VALUE, titlePanel.getPreferredSize().height));
                contentPane.setVisible(false);
                revalidate();
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(minComponentWidth, minComponentHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(minComponentWidth, minComponentHeight);
    }

    /**
     *
     */
    public void printDimensions() {
        System.out.println("-- DIMENSIONS -- ");

        System.out.println("sideBar height                     " + this.sideBarOwner.getSize().height);

        System.out.println("sideBarSection height              " + getSize().height);
        System.out.println("sideBarSection titlePanel height   " + titlePanel.getSize().height);
        System.out.println("sideBarSection.contentPane height  " + contentPane.getSize().height);
    }

}
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
 * Created on 08.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


/**
 * A panel containing a table with document (meta) information to show. Which kind of information is shown is based
 * on the specified table mode.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
public class MetaInfoPanel extends JPanel {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = 1964785387859143491L;

    /**
     * Default background color of a selected row.
     */
    public static final Color DEF_SELECTEDROW_BACKGROUND = Color.DARK_GRAY;

    /**
     * Default foreground color of a selected row.
     */
    public static final Color DEF_SELECTEDROW_FOREGROUND = Color.WHITE;

    /**
     * Default background color of a mouse over row.
     */
    public static final Color DEF_MOUSEOVERROW_BACKGROUND = Color.LIGHT_GRAY;

    /**
     * Default foreground color of a mouse over row.
     */
    public static final Color DEF_MOUSEOVERDROW_FOREGROUND = Color.BLACK;

    /**
     * Default background color of a default row.
     */
    public static final Color DEF_ROW_BACKGROUND = Color.WHITE;

    /**
     * Default foreground color of a default row.
     */
    public static final Color DEF_ROW_FOREGROUND = Color.BLACK;


    private final String m_title;

    private final JTable m_metaInfoTable;

    private int m_infoRow = 0;

    private Color m_selectedRowBackgroud = DEF_SELECTEDROW_BACKGROUND;

    private Color m_selectedRowForeground = DEF_SELECTEDROW_FOREGROUND;

    private Color m_mouseOverRowBackgroud = DEF_MOUSEOVERROW_BACKGROUND;

    private Color m_mouseOverRowForeground = DEF_MOUSEOVERDROW_FOREGROUND;

    private Color m_defaultRowBackgroud = DEF_ROW_BACKGROUND;

    private Color m_defaultRowForeground = DEF_ROW_FOREGROUND;

    /**
     * Creates a new instance of {@code MetaInfoPanel} with given title to and table model, as well as the
     * preferred width and height to set.
     *
     * @param title The title to set and display.
     * @param tableModel The table model containing the information to show.
     * @param preferredWidth The preferred width to set.
     * @param preferredHeight The preferred height to set.
     */
    public MetaInfoPanel(final String title, final TableModel tableModel,
            final int preferredWidth, final int preferredHeight) {
        super(new BorderLayout());

        m_title = title;

        JLabel heading = new JLabel();
        Font f = new Font("Verdana", Font.BOLD, 13);
        heading.setFont(f);
        heading.setText(m_title);
        add(heading, BorderLayout.NORTH);

        m_metaInfoTable = new JTable(tableModel);
        m_metaInfoTable.addMouseMotionListener(new MetaInfoTableListener());
        m_metaInfoTable.setOpaque(false);
        m_metaInfoTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer());

        JScrollPane jsp = new JScrollPane(m_metaInfoTable);
        jsp.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        add(jsp, BorderLayout.CENTER);
        setBorder(new EtchedBorder());
    }

    /**
     * @return the selectedRowBackgroud
     */
    public Color getSelectedRowBackgroud() {
        return m_selectedRowBackgroud;
    }


    /**
     * @param selectedRowBackgroud the selectedRowBackgroud to set
     */
    public void setSelectedRowBackgroud(final Color selectedRowBackgroud) {
        this.m_selectedRowBackgroud = selectedRowBackgroud;
    }


    /**
     * @return the selectedRowForeground
     */
    public Color getSelectedRowForeground() {
        return m_selectedRowForeground;
    }


    /**
     * @param selectedRowForeground the selectedRowForeground to set
     */
    public void setSelectedRowForeground(final Color selectedRowForeground) {
        this.m_selectedRowForeground = selectedRowForeground;
    }


    /**
     * @return the mouseOverRowBackgroud
     */
    public Color getMouseOverRowBackgroud() {
        return m_mouseOverRowBackgroud;
    }


    /**
     * @param mouseOverRowBackgroud the mouseOverRowBackgroud to set
     */
    public void setMouseOverRowBackgroud(final Color mouseOverRowBackgroud) {
        this.m_mouseOverRowBackgroud = mouseOverRowBackgroud;
    }


    /**
     * @return the mouseOverRowForeground
     */
    public Color getMouseOverRowForeground() {
        return m_mouseOverRowForeground;
    }


    /**
     * @param mouseOverRowForeground the mouseOverRowForeground to set
     */
    public void setMouseOverRowForeground(final Color mouseOverRowForeground) {
        this.m_mouseOverRowForeground = mouseOverRowForeground;
    }


    /**
     * @return the defaultRowBackgroud
     */
    public Color getDefaultRowBackgroud() {
        return m_defaultRowBackgroud;
    }


    /**
     * @param defaultRowBackgroud the defaultRowBackgroud to set
     */
    public void setDefaultRowBackgroud(final Color defaultRowBackgroud) {
        this.m_defaultRowBackgroud = defaultRowBackgroud;
    }


    /**
     * @return the defaultRowForeground
     */
    public Color getDefaultRowForeground() {
        return m_defaultRowForeground;
    }


    /**
     * @param defaultRowForeground the defaultRowForeground to set
     */
    public void setDefaultRowForeground(final Color defaultRowForeground) {
        this.m_defaultRowForeground = defaultRowForeground;
    }


    /**
     * @return the m_title
     */
    public String getTitle() {
        return m_title;
    }


    /**
    *
    * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
    */
   private class MetaInfoTableListener extends MouseAdapter {

       /**
        * {@inheritDoc}
        */
       @Override
       public void mouseMoved(final MouseEvent e) {
           JTable aTable = (JTable)e.getSource();
           m_infoRow = aTable.rowAtPoint(e.getPoint());
           aTable.repaint();
       }
   }

   /**
    *
    * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
    */
   @SuppressWarnings("serial")
   private class AttributiveCellRenderer extends JLabel
   implements TableCellRenderer {

       /**
        * Constructor.
        */
       public AttributiveCellRenderer() {
           setOpaque(true);
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public Component getTableCellRendererComponent(final JTable table,
               final Object value, final boolean isSelected,
               final boolean hasFocus, final int row, final int column) {

           if (isSelected) {
               this.setBackground(m_selectedRowBackgroud);
               this.setForeground(m_selectedRowForeground);
           } else if (row == m_infoRow) {
               this.setBackground(m_mouseOverRowBackgroud);
               this.setForeground(m_mouseOverRowForeground);
           } else {
               this.setBackground(m_defaultRowBackgroud);
               this.setForeground(m_defaultRowForeground);
           }

           this.setText(value.toString());
           return this;
       }
   }
}

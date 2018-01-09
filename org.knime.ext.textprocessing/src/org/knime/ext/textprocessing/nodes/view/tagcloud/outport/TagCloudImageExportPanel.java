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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   15.11.2011 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud.outport;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;

import javax.swing.JPanel;

/**
 * The panel for the image.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TagCloudImageExportPanel extends JPanel {

    private static final long serialVersionUID = 2856757620090895488L;

    private TagCloud m_tagcloud = null;

    private boolean m_antialiasing = false;

    /**
     * Constructor.
     * @param tagCloud The tagcloud to visualize.
     */
    public TagCloudImageExportPanel(final TagCloud tagCloud) {
        m_tagcloud = tagCloud;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;

        if (m_antialiasing) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (m_tagcloud != null) {
            Font myfont = new Font(m_tagcloud.getfontName(), Font.PLAIN, m_tagcloud.getmaxFontsize());
            TagCloudData[] points = m_tagcloud.getDataArray();

            // through all points
            for (TagCloudData tcd : points) {
                myfont = myfont.deriveFont(Font.PLAIN, tcd.getFontsize());

                g2.setColor(tcd.getTextcolor());
                g2.setFont(myfont);

                if (tcd.isSelected()) { // is the term is currently selec
                    // ted it will be in italic
                    myfont = myfont.deriveFont(Font.ITALIC);
                }
                if (tcd.isBold()) {
                    myfont = myfont.deriveFont(Font.BOLD);
                }
                TextLayout tl = new TextLayout(tcd.getTerm().getText(), myfont, g2.getFontRenderContext());
                tl.draw(g2, (float)tcd.getX(), (float)(tcd.getY() + tcd.getHeight() * 0.8));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        if (m_tagcloud != null) {
            return m_tagcloud.getPreferredSize();
        }
        return super.getPreferredSize();
    }

    /**
     * @param antialiasing the antialiasing to set
     */
    public void setAntialiasing(final boolean antialiasing) {
        m_antialiasing = antialiasing;
    }
}

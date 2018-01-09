/*
 * ------------------------------------------------------------------------
 *
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
 *   21.01.2016 (hermann): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.knime.ext.textprocessing.nodes.view.documentviewer2.DocumentViewModel;
import org.knime.ext.textprocessing.nodes.view.documentviewer2.slider.ArrowPanel;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
class DocumentTopPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final DocumentViewModel m_docViewModel;

    private final JLabel m_docTitel;

    private final JPanel m_nextButton;

    private final JPanel m_previousButton;

    private final JPanel m_navigationContainer;

    /**
     * @param docViewModel
     */
    public DocumentTopPanel(final DocumentViewModel docViewModel) {
        super(new FlowLayout(FlowLayout.CENTER));

        if (docViewModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewModel = docViewModel;

        // Add the Panel that will contain the navigation and titel
        m_navigationContainer = new JPanel();

        // Enable navigationn throught documents
        m_previousButton = new ArrowPanel(SwingConstants.WEST, "Name1");
        m_previousButton.setPreferredSize(new Dimension(40, 40));
        m_previousButton.addMouseListener(new ArrowListener());
        m_previousButton.setToolTipText("show previous document");
        m_navigationContainer.add(m_previousButton);
        m_docTitel = new JLabel(m_docViewModel.getDocument().getTitle(), SwingConstants.CENTER);
        m_docTitel.setForeground(Color.BLACK);
        m_docTitel.setFont(new Font("sans-serif", Font.BOLD, 16));
        m_docTitel.setPreferredSize(new Dimension(800, 50));
        m_navigationContainer.add(m_docTitel);
        m_nextButton = new ArrowPanel(SwingConstants.EAST, "Name2");
        m_nextButton.setPreferredSize(new Dimension(40, 40));
        m_nextButton.addMouseListener(new ArrowListener());
        m_nextButton.setToolTipText("show next document");
        m_navigationContainer.add(m_nextButton);

        add(m_navigationContainer);
    }

    private void updateDocumentViewModel() {
        m_docViewModel.modelChanged();
    }

    private class ArrowListener implements MouseListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getSource().equals(m_previousButton)) {
                m_docViewModel.previousDocument();
            } else if (e.getSource().equals(m_nextButton)) {
                m_docViewModel.nextDocument();
            }

            updateDocumentViewModel();
            m_docTitel.setText(m_docViewModel.getDocument().getTitle());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(final MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(final MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseEntered(final MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseExited(final MouseEvent e) {
            // TODO Auto-generated method stub

        }

    }

}

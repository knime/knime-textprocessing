/*
 * ------------------------------------------------------------------------
 *
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
 *   21.01.2016 (hermann): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.knime.ext.textprocessing.nodes.view.documentviewer2.slider.SideBar;
import org.knime.ext.textprocessing.nodes.view.documentviewer2.slider.SideBar.SideBarMode;
import org.knime.ext.textprocessing.nodes.view.documentviewer2.slider.SidebarSection;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
class DocumentActionPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final DocumentViewModel m_docViewModel;

    /**
     * @param docViewModel
     */
    public DocumentActionPanel(final DocumentViewModel docViewModel) {
        if (docViewModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewModel = docViewModel;

        Icon iconCal24 = null;

        setLayout(new BorderLayout());

        JPanel listPanel = new JPanel(new BorderLayout());

        SideBar sideBar = new SideBar(SideBarMode.TOP_LEVEL, true, -1, true);

        // build the menus
        SidebarSection ss1 =
            new SidebarSection(sideBar, "Search", new DocumentControlPanel2(m_docViewModel), iconCal24);
        sideBar.addSection(ss1);

        SidebarSection ss2 =
            new SidebarSection(sideBar, "Document information", createDocumentInfoViewPanel(), iconCal24);
        sideBar.addSection(ss2);

        SidebarSection ss3 = new SidebarSection(sideBar, "Authors", createAuthorViewPanel(), iconCal24);
        sideBar.addSection(ss3);

        SidebarSection ss4 =
            new SidebarSection(sideBar, "Meta information", createDocumentMetaInfoViewPanel(), iconCal24);
        sideBar.addSection(ss4);

        listPanel.add(sideBar, BorderLayout.CENTER);
        add(listPanel);

    }

    // generate a panel displaying the Document Info
    private JPanel createDocumentInfoViewPanel() {
        DocumentInfoTableModel tableModel = new DocumentInfoTableModel(m_docViewModel);
        JTable documentMetaInfoTable = new JTable(tableModel);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        m_docViewModel.addObserver(tableModel);
        JScrollPane pane = new JScrollPane(documentMetaInfoTable);
        panel.add(pane, BorderLayout.CENTER);
        return panel;
    }

    // generate a panel that displays the Document Author Info
    private JPanel createAuthorViewPanel() {
        AuthorTableModel tableModel = new AuthorTableModel(m_docViewModel);
        JTable authorTable = new JTable(tableModel);
        JPanel authorPanel = new JPanel();
        authorPanel.setLayout(new BorderLayout());
        m_docViewModel.addObserver(tableModel);
        JScrollPane pane = new JScrollPane(authorTable);
        authorPanel.add(pane, BorderLayout.CENTER);
        return authorPanel;
    }

    // generate a panel that displays the Document Meta Info
    private JPanel createDocumentMetaInfoViewPanel() {
        DocumentMetaInfoTableModel tableModel = new DocumentMetaInfoTableModel(m_docViewModel);
        JTable docMetaInfoTable = new JTable(tableModel);
        JPanel metaInfoPanel = new JPanel();
        metaInfoPanel.setLayout(new BorderLayout());
        m_docViewModel.addObserver(tableModel);
        JScrollPane pane = new JScrollPane(docMetaInfoTable);
        metaInfoPanel.add(pane, BorderLayout.CENTER);
        return metaInfoPanel;

    }

}

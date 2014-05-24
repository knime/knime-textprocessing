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
 * Created on 08.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.util.ImgLoaderUtil;


/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class DocumentControlPanel extends JPanel {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;


    private final DocumentViewModel m_docViewModel;

    private final JToggleButton m_hiliteTagsButton;

    private final JComboBox m_tagTypes;

    private final JComboBox m_linkSourcesBox;

    private final JButton m_colorChooserButton;

    private final JTextField m_searchField;

    private final JToggleButton m_searchButton;

    private final JButton m_nextButton;

    private final JButton m_previousButton;


    /**
     * Creates new instance of {@code DocumentControlPanel} with given document view model.
     *
     * @param docViewModel The document view model to use to set gui input to.
     */
    public DocumentControlPanel(final DocumentViewModel docViewModel) {
        super(new FlowLayout(FlowLayout.LEFT));

        if (docViewModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewModel = docViewModel;

        JToolBar hiliteToolbar = new JToolBar();
        // hilite button
        m_hiliteTagsButton = new JToggleButton();
        m_hiliteTagsButton.setSelected(DocumentViewPanel.HILITE_TAGS);
        m_hiliteTagsButton.addActionListener(new HiliteActionListener());
        ImageIcon icon = ImgLoaderUtil.loadImageIcon("marker.png",
                                                     "Hilite tags");
        m_hiliteTagsButton.setIcon(icon);
        m_hiliteTagsButton.setToolTipText("click to hilite tagged terms");
        hiliteToolbar.add(m_hiliteTagsButton);

        // color chooser
        m_colorChooserButton = new JButton();
        icon = ImgLoaderUtil.loadImageIcon("color.png", "Color");
        m_colorChooserButton.setIcon(icon);
        m_colorChooserButton.setToolTipText("Choose Color");
        m_colorChooserButton.setBorder(BorderFactory.createLineBorder(DocumentViewPanel.DEFAULT_ENTITY_COLOR));
        m_colorChooserButton.setBorderPainted(true);
        m_colorChooserButton.addActionListener(new ColorButtonListener());
        m_colorChooserButton.setOpaque(true);
        hiliteToolbar.add(m_colorChooserButton);

        hiliteToolbar.addSeparator();

        // tag combo box
        m_tagTypes = new JComboBox();
        Set<String> tagTypes = TagFactory.getInstance().getTagTypes();
        for (String tagType : tagTypes) {
            m_tagTypes.addItem(tagType);
        }
        m_tagTypes.addActionListener(new HiliteActionListener());
        hiliteToolbar.add(m_tagTypes);

        hiliteToolbar.addSeparator();

        // links sources
        hiliteToolbar.add(new JLabel("Link to:"));
        m_linkSourcesBox = new JComboBox();
        for (String source : SearchEngines.getInstance().getSearchEngineNames()) {
            m_linkSourcesBox.addItem(source);
        }
        m_linkSourcesBox.setSelectedItem(SearchEngines.getInstance().getDefaultSource());
        m_linkSourcesBox.setEnabled(m_hiliteTagsButton.isSelected());
        m_linkSourcesBox.addActionListener(new LinkSourceListener());
        hiliteToolbar.add(m_linkSourcesBox);

        JToolBar searchToolbar = new JToolBar();
        // search field and button
        m_searchButton = new JToggleButton();
        m_searchButton.addActionListener(new SearchListener());
        icon = ImgLoaderUtil.loadImageIcon("search.png", "Search");
        m_searchButton.setIcon(icon);
        m_searchButton.setToolTipText("click to hilite search results");
        searchToolbar.add(m_searchButton);
        m_searchField = new JTextField("[a-z]+");
        m_searchField.setColumns(10);
        m_searchField.getDocument().addDocumentListener(new SearchListener());
        m_searchField.setToolTipText("enter regular expression");
        searchToolbar.add(m_searchField);

        // next and previous buttons
        JToolBar navigationToolbar = new JToolBar();
        m_previousButton = new JButton(ImgLoaderUtil.loadImageIcon("previous.png", "previous"));
        m_previousButton.addActionListener(new NewDocumentListener());
        m_previousButton.setToolTipText("show previous document");
        navigationToolbar.add(m_previousButton);
        m_nextButton = new JButton(ImgLoaderUtil.loadImageIcon("next.png", "next"));
        m_nextButton.addActionListener(new NewDocumentListener());
        m_nextButton.setToolTipText("show next document");
        navigationToolbar.add(m_nextButton);

        add(searchToolbar);
        add(hiliteToolbar);
        add(navigationToolbar);
    }

    private void updateDocumentViewModel() {
        m_docViewModel.setHiliteTags(m_hiliteTagsButton.isSelected());
        m_docViewModel.setTagType(m_tagTypes.getSelectedItem().toString());
        m_docViewModel.setHiliteSearch(m_searchButton.isSelected());
        m_docViewModel.setSearchString(m_searchField.getText());
        if (m_linkSourcesBox.getSelectedItem() != null) {
            m_docViewModel.setLinkSourceName(m_linkSourcesBox.getSelectedItem().toString());
        }

        m_docViewModel.modelChanged();
    }

    /**
     * Sets (un-)hilited text when action was performed.
     *
     * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
     */
    private class HiliteActionListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent arg0) {
            updateDocumentViewModel();
            m_linkSourcesBox.setEnabled(m_hiliteTagsButton.isSelected());
        }
    }

    /**
    *
    * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
    */
   private class SearchListener implements ActionListener, DocumentListener {

       /**
        * {@inheritDoc}
        */
       @Override
       public void actionPerformed(final ActionEvent e) {
           updateDocumentViewModel();
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public void insertUpdate(final DocumentEvent e) {
           updateDocumentViewModel();
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public void removeUpdate(final DocumentEvent e) {
           updateDocumentViewModel();
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public void changedUpdate(final DocumentEvent e) {
           updateDocumentViewModel();
       }
   }

   /**
   *
   * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
   */
   private class NewDocumentListener implements ActionListener {

       /**
        * {@inheritDoc}
        */
       @Override
       public void actionPerformed(final ActionEvent e) {
           if (e.getSource().equals(m_nextButton)) {
               m_docViewModel.nextDocument();
           } else if (e.getSource().equals(m_previousButton)) {
               m_docViewModel.previousDocument();
           }

           updateDocumentViewModel();
       }
   }

   /**
   *
   * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
   */
   private class LinkSourceListener implements ActionListener {

       /**
        * {@inheritDoc}
        */
       @Override
       public void actionPerformed(final ActionEvent e) {
           updateDocumentViewModel();
       }
   }

   /**
   *
   * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
   */
   private class ColorButtonListener implements ActionListener {

       /**
        * {@inheritDoc}
        */
       @Override
       public void actionPerformed(final ActionEvent e) {
           Color taggedEntityColor = JColorChooser.showDialog(null, "Choose hilite color",
                                                              m_docViewModel.getTaggedEntityColor());
           if (taggedEntityColor == null) {
               taggedEntityColor = Color.RED;
           }
           m_colorChooserButton.setOpaque(true);
           m_colorChooserButton.setBorder(BorderFactory.createLineBorder(taggedEntityColor));
           repaint();
           m_docViewModel.setTaggedEntityColor(taggedEntityColor);
           updateDocumentViewModel();
       }
   }
}

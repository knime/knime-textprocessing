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
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.knime.core.util.DesktopUtil;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngines;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 * @since 2.8
 */
final class DocumentPanel2 extends JPanel implements Observer {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;

    private final DocumentViewModel m_docViewModel;

    private final JEditorPane m_fulltextPane;

    private JPopupMenu m_rightClickMenue = null;

    /**
     * Creates new instance of {@code DocumentPanel} with given document view model to show, and the preferred width and
     * height to set.
     *
     * @param docViewModel The document view model.
     * @param preferredWidth The preferred width to set.
     * @param preferredHeight The preferred height to set.
     */
    public DocumentPanel2(final DocumentViewModel docViewModel, final int preferredWidth, final int preferredHeight) {
        super(new BorderLayout());

        if (docViewModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewModel = docViewModel;

        m_fulltextPane = new JEditorPane();
        m_fulltextPane.setContentType("text/html");
        m_fulltextPane.setEditable(false);
        m_fulltextPane.setCaretPosition(0);
        m_fulltextPane.addHyperlinkListener(new LinkListener());
        m_fulltextPane.setToolTipText("Select text and right click.");

        HTMLEditorKit editorKit = new HTMLEditorKit();

        // Adding stylesheet to the document
        StyleSheet sheet = editorKit.getStyleSheet();
        sheet.addRule(
            "html, body {margin: 0; padding: 4px; color: #000000; font-family: 'Roboto', sans-serif; background: #ffffff; font-size: 12px;}");
        sheet.addRule("table { border-collapse: collapse; }");
        sheet.addRule("th, td { border: 1px solid #000000; padding: 10px 15px; }");
        sheet.addRule(
            ".docHeading {display: block; font-size: 15px; font-weight: bold; margin: 5px 0 30px 0; width: 100%;}");
        sheet.addRule(
            ".sep {  width:100%; border-bottom: 1px dashed #8c8b8b ; top: 10%; bottom: 20%; position: absolute; margin: 10px 0 10px 0;}");
        sheet.addRule(".subHeading { font-weight: 500; font-style: italic; text-transform: lowercase; }");

        editorKit.setStyleSheet(sheet);
        m_fulltextPane.setEditorKit(editorKit);

        Document doc = editorKit.createDefaultDocument();
        m_fulltextPane.setDocument(doc);
        m_fulltextPane.setText(getPreparedText());

        m_rightClickMenue = new JPopupMenu();
        JMenuItem item;
        for (String source : SearchEngines.getInstance().getSearchEngineNames()) {
            item = new JMenuItem(source);
            item.addActionListener(new RightClickMenueListener());
            m_rightClickMenue.add(item);
        }
        m_fulltextPane.setComponentPopupMenu(m_rightClickMenue);

        JScrollPane jsp = new JScrollPane(m_fulltextPane);
        jsp.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        add(jsp, BorderLayout.CENTER);
    }

    private static final String whitespacePlaceHolder = "@#P@H#@";

    private final String replaceWhitespacesWithPlaceholder(final String str) {
        return str.replaceAll("\\s", whitespacePlaceHolder);
    }

    private final String cleanWhitespaces(final String str) {
        return str.replaceAll(whitespacePlaceHolder, "&ensp;");
    }

    private final String replaceWhitespacesWithHtml(final String str) {
        return str.replaceAll("\\s", "&ensp;");
    }

    private String getPreparedText() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<h2 class='docHeading'>" + m_docViewModel.getDocument().getTitle() + "</h2>");
        List<Section> sections = m_docViewModel.getDocument().getSections();
        // disable the document title section
        for (Section s : sections) {
            if (s.getAnnotation().equals(SectionAnnotation.TITLE)) {
                continue;
            }

            buffer.append("<font face=\"Verdana\" size=\"3\"><b>");
            String ann = s.getAnnotation().toString();
            buffer.append("<h4 class='subHeading'>" + ann + "</h4>");
            buffer.append("</b></font>");

            List<Paragraph> paras = s.getParagraphs();
            for (Paragraph p : paras) {
                buffer.append("<font face=\"Verdana\" size=\"3\">");
                buffer.append("<p>" + getParagraphText(p) + "</p>");
                buffer.append("</font>");
                buffer.append("<div class='sep'></div>");
            }
        }

        return buffer.toString();
    }

    private String getParagraphText(final Paragraph p) {
        if (!m_docViewModel.isHiliteTags() && !m_docViewModel.isHiliteSearch() && !m_docViewModel.isDisplayTags()
            && !m_docViewModel.isDisableHtmlTags()) {
            return escapeHTMLIfSelected(p.getText());
        }

        // selected color to hex str
        String hexColorStr = Integer.toHexString(m_docViewModel.getTaggedEntityColor().getRGB() & 0x00ffffff);

        StringBuffer paramStr = new StringBuffer();
        for (Sentence sen : p.getSentences()) {
            for (Term t : sen.getTerms()) {
                boolean marked = false;
                // search hiliting
                if (m_docViewModel.isHiliteSearch() && m_docViewModel.getSearchString() != null) {
                    if (searchMatch(t, m_docViewModel.getSearchString())) {
                        paramStr.append("<font style=\"BACKGROUND-COLOR: green; COLOR: white\">"
                            + escapeHTMLIfSelected(t.getText()) + "</font>");
                        paramStr.append(
                            escapeHTMLIfSelected(t.getTextWithWsSuffix().substring(t.getText().length())));
                        marked = true;
                        continue;
                    }
                }

                if (m_docViewModel.isHiliteTags() && m_docViewModel.getTagType() != null) {
                    if (t.getTags().size() > 0) {
                        List<Tag> tags = t.getTags();
                        for (Tag tag : tags) {
                            // tag hiliting
                            if (tag.getTagType().equals(m_docViewModel.getTagType())) {

                                if (SearchEngines.getInstance().getSearchEngineSetting().size() > 0) {
                                    String link = SearchEngines.getInstance()
                                        .getUrlString(m_docViewModel.getLinkSourceName(), t.getText());
                                    paramStr.append("<a href=\"" + link + "\">");
                                }
                                paramStr.append("<font color=\"#" + hexColorStr + "\">"
                                    + escapeHTMLIfSelected(t.getText()) + "</font>");
                                if (SearchEngines.getInstance().getSearchEngineSetting().size() > 0) {
                                    paramStr.append("</a>");
                                }

                                paramStr.append(escapeHTMLIfSelected(
                                    t.getTextWithWsSuffix().substring(t.getText().length())));
                                marked = true;
                                break;
                            }
                        }
                    }
                }

                // when display tags button is selected, show tags
                if (m_docViewModel.isDisplayTags() && m_docViewModel.getTagType() != null
                    && !m_docViewModel.isDisableHtmlTags()) {
                    if (t.getTags().size() > 0) {
                        List<Tag> termTags = t.getTags();
                        for (Tag tag : termTags) {
                            if (tag.getTagType().equals(m_docViewModel.getTagType())) {
                                if (!m_docViewModel.isHiliteTags()) {
                                    paramStr.append(escapeHTMLIfSelected(t.getTextWithWsSuffix()));
                                }
                                paramStr.append("<font size=\"2.4\">" + "<b>" + "<i>" + "[" + tag.getTagType() + "("
                                    + tag.getTagValue() + ")" + "] " + "</i>" + "</b>" + "</font>");
                                marked = true;
                                break;
                            }
                        }
                    }
                } else if (m_docViewModel.isDisplayTags() && m_docViewModel.getTagType() != null
                    && m_docViewModel.isDisableHtmlTags()) {
                    if (t.getTags().size() > 0) {
                        List<Tag> termTags = t.getTags();
                        for (Tag tag : termTags) {
                            if (tag.getTagType().equals(m_docViewModel.getTagType())) {
                                if (!m_docViewModel.isHiliteTags()) {
                                    paramStr.append(escapeHTMLIfSelected(t.getTextWithWsSuffix()));
                                }
                                paramStr.append("<font size=\"2.4\">" + "<b>" + "<i>" + "[" + tag.getTagType() + "("
                                    + tag.getTagValue() + ")" + "] " + "</i>" + "</b>" + "</font>");
                                marked = true;
                                break;
                            }
                        }
                    }

                }

                if (!marked) {
                    paramStr.append(escapeHTMLIfSelected(t.getTextWithWsSuffix()));
                }
            }

        }

        return paramStr.toString();
    }

    // Escape HTML Character when clicked
    private String escapeHTMLIfSelected(String str) {
        str = replaceWhitespacesWithPlaceholder(str);
        str = replaceWhitespacesWithHtml(str);
        if (!m_docViewModel.isDisableHtmlTags()) {
            str = StringEscapeUtils.escapeHtml4(str);
            }
        str = cleanWhitespaces(str);
        return str;
    }

    private static final boolean searchMatch(final Term term, final String search) {
        try {
            return term.getText().matches(search);
        } catch (Exception e) {
            // do nothing
        }
        return false;
    }

    private static final void openUrlInBrowser(final URL u) {
        if (u != null) {
            try {
                DesktopUtil.browse(u);
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class LinkListener implements HyperlinkListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                URL u = e.getURL();
                openUrlInBrowser(u);
            }
        }
    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class RightClickMenueListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            String selectedText = m_fulltextPane.getSelectedText();
            if (selectedText != null) {
                if (selectedText.length() > 0) {
                    if (e.getSource() instanceof JMenuItem) {
                        String source = ((JMenuItem)e.getSource()).getText();
                        String urlStr = SearchEngines.getInstance().getUrlString(source, selectedText);

                        try {
                            URL u = new URL(urlStr);
                            openUrlInBrowser(u);
                        } catch (MalformedURLException e1) {
                            // No msg here
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable o, final Object arg) {
        m_fulltextPane.setText(getPreparedText());
        repaint();
    }



}

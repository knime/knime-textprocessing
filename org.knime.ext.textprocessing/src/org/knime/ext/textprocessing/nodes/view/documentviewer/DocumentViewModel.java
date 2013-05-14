/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
import java.util.Observable;

import org.knime.ext.textprocessing.data.Document;


/**
 * Model, containing document and view data, such as hiliting, searching, etc.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class DocumentViewModel extends Observable {

    private Document m_doc;

    private final DocumentProvider m_docProvider;

    private boolean m_hiliteTags = DocumentViewPanel.HILITE_TAGS;

    private String m_tagType = null;

    private Color m_taggedEntityColor = DocumentViewPanel.DEFAULT_ENTITY_COLOR;

    private boolean m_hiliteSearch = DocumentViewPanel.HILITE_SEARCH;

    private String m_searchString = null;

    private String m_linkSourceName = SearchEngines.getInstance().getDefaultSource();

    /**
     * Creates a new instance of {@code DocumentViewModel} with given document to display and document provider to get
     * next and previous documents.
     *
     * @param document Document to show.
     * @param documentProvider Document provider to get next and previous documents.
     */
    public DocumentViewModel(final Document document, final DocumentProvider documentProvider) {
        if (document == null) {
            throw new IllegalArgumentException("Document may not be null!");
        }
        m_doc = document;
        m_docProvider = documentProvider;
    }

    /**
     * Notifies all observers.
     */
    public void modelChanged() {
        setChanged();
        notifyObservers();
    }

    /**
     * Retrieves and shows next document.
     */
    public void nextDocument() {
        if (m_docProvider != null && m_docProvider.hasNext()) {
            m_doc = m_docProvider.next();
        }
    }

    /**
     * Retrieves and shows previous document.
     */
    public void previousDocument() {
        if (m_docProvider != null && m_docProvider.hasPrevious()) {
            m_doc = m_docProvider.previous();
        }
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return m_doc;
    }

    /**
     * @param document the document to set
     */
    public void setDoc(final Document document) {
        this.m_doc = document;
    }

    /**
     * @return the hiliteTags flag
     */
    public boolean isHiliteTags() {
        return m_hiliteTags;
    }

    /**
     * @param hiliteTags the hiliteTags flag to set
     */
    public void setHiliteTags(final boolean hiliteTags) {
        this.m_hiliteTags = hiliteTags;
    }

    /**
     * @return the type of the tags to hilite
     */
    public String getTagType() {
        return m_tagType;
    }

    /**
     * @param tagType the type of tags to hilite to set. If set {@code true} tagged entities results are hilited.
     */
    public void setTagType(final String tagType) {
        this.m_tagType = tagType;
    }

    /**
     * @return the tagged entity color
     */
    public Color getTaggedEntityColor() {
        return m_taggedEntityColor;
    }

    /**
     * @param taggedEntityColor the tagged entity color to set
     */
    public void setTaggedEntityColor(final Color taggedEntityColor) {
        this.m_taggedEntityColor = taggedEntityColor;
    }

    /**
     * @return the hiliteSearch flag
     */
    public boolean isHiliteSearch() {
        return m_hiliteSearch;
    }

    /**
     * @param hiliteSearch the hiliteSearch flag to set. If set {@code true} search results are hilited.
     */
    public void setHiliteSearch(final boolean hiliteSearch) {
        this.m_hiliteSearch = hiliteSearch;
    }

    /**
     * @return the search string
     */
    public String getSearchString() {
        return m_searchString;
    }

    /**
     * @param searchString the search string to set
     */
    public void setSearchString(final String searchString) {
        this.m_searchString = searchString;
    }

    /**
     * @return the name of the source to link to
     */
    public String getLinkSourceName() {
        return m_linkSourceName;
    }

    /**
     * @param linkSourceName the name of the source to link to, to set
     */
    public void setLinkSourceName(final String linkSourceName) {
        this.m_linkSourceName = linkSourceName;
    }
}

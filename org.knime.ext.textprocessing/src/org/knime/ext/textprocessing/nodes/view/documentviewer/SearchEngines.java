/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2012
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 * 
 * History
 *   03.07.2012 (kilian): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Provides links to various search engines for given terms used as query terms.
 * 
 * @author Kilian Thiel, KNIME.com AG, Zurich
 * @since 2.6
 */
public final class SearchEngines {
    
    /**
     * The singelton instance.
     */
    static final SearchEngines INSTANCE = new SearchEngines();
    
    /**
     * Default search engine.
     */
    static final String DEFAULT_SOURCE = "Google";
    
    private Hashtable<String, String> m_sources;
    
    private SearchEngines() { 
        m_sources = new Hashtable<String, String>();
        
        // GENERAL
        m_sources.put("Google", 
                "https://www.google.de/search?hl=en&noj=1&site=webhp&q=");
        m_sources.put("Wikipedia", 
                "http://en.wikipedia.org/w/index.php?search=");
        m_sources.put("Leo (dict)",
                "http://dict.leo.org/ende?lp=ende&lang=de&searchLoc=0&cmpType=relaxed&sectHdr=on&spellToler=&search=");
        
        // BIO
        m_sources.put("PubMed", 
                "http://www.ncbi.nlm.nih.gov/pubmed?term=");
        m_sources.put("PubGene",
                "http://www.pubgene.org/tools/Network/Subset.cgi?mode=simple&organism=hs&terms=");
        m_sources.put("UniProtKB", 
                "http://www.uniprot.org/uniprot/?sort=score&query=");
        
        // CHEM
        m_sources.put("PubChem",
                "http://www.ncbi.nlm.nih.gov/pccompound?term=");
        m_sources.put("ZINC", "http://zinc.docking.org/results/query?term=");
        m_sources.put("DrugBank", 
                "http://www.drugbank.ca/search?commit=Search&query=");
    }
    
    /**
     * @return The singelton instance.
     */
    public static SearchEngines getInstance() {
        return INSTANCE;
    }
    
    /**
     * @return A set containing the names of all available search engines.
     */
    public List<String> getSearchEngineNames() {
        List<String> keys = new ArrayList<String>(m_sources.keySet());
        Collections.sort(keys);
        return keys;
    }
    
    /**
     * Returns the url to query the specified search engine with the given term.
     * @param source The search engine to query.
     * @param query The query term
     * @return The url as string by which the search engine can be queried.
     */
    public String getUrlString(final String source, final String query) {
        String encodedQuery = encode(query);
        String link = m_sources.get(source);
        if (link != null) {
            link += encodedQuery;
            return link;
        }
        return m_sources.get(DEFAULT_SOURCE) + encodedQuery;
    }
    
    /**
     * @return The default search engine
     */
    public String getDefaultSource() {
        return DEFAULT_SOURCE;
    }
    

    private static String encode(final String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(final int ch) {
        return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(final char ch) {
        if (ch > 128 || ch < 0) {
            return true;
        }
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
}

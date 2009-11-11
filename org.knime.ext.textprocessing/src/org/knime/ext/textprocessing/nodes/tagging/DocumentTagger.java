/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 * 
 * History
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import org.knime.ext.textprocessing.data.Document;

/**
 * Classes that restructure, retokenize or tag documents or terms respectively
 * should implement this interface to enable a proper use by tagger node models.
 * Restructuring of documents could be i.e. a noun phrase parser, detecting
 * noun phrases and combine words to terms which represent the phrases. The
 * document terms would be restructured, so the document self gets 
 * restructured too. What all the underlying classes have in common is that
 * they create a certain granularity of terms and they add tags to the terms
 * i.e. the Part-Of-Speech tagger adds POS tags, the noun phrase parser adds
 * nouns phrase tags and so on. Since they all adds tags, this interface is
 * called <code>DocumentTagger</code>.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentTagger {
    
    /**
     * Restructes, retokenizes and / or tags documents and returns a new
     * rebuild {@link org.knime.ext.textprocessing.data.Document} instance.
     * 
     * @param doc The <code>Document</code> to tag.
     * @return a new restructured, retokenized and tagged <code>Document</code> 
     * instance.
     */
    public Document tag(final Document doc);

}

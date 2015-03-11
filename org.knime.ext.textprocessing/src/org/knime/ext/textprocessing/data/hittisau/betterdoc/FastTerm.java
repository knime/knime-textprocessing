/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   10.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.betterdoc;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.data.hittisau.Term;

/**
 *
 * @author Alexander
 */
public class FastTerm implements Term {

    private int m_termIndex;

    private int m_whiteSpaceIndex;

    private int[][] m_tags;

    private boolean m_immutable;

    private TermLookupTable m_tlt;

    private TagBuilderLookupTable m_tblt;

    public FastTerm(final int termIdx, final int wsIdx, final int[][] tags, final boolean immutable,
        final TermLookupTable tlt, final TagBuilderLookupTable tblt) {
        m_termIndex = termIdx;
        m_whiteSpaceIndex = wsIdx;
        m_tags = tags;
        m_immutable = immutable;
        m_tlt = tlt;
        m_tblt = tblt;
    }



    public int[][] getTagArray() {
        return m_tags;
    }

    /**
     * @return the m_tlt
     */
    public TermLookupTable getTermLookupTable() {
        return m_tlt;
    }

    /**
     * @param m_tlt the m_tlt to set
     */
    public void setTermLookupTable(final TermLookupTable m_tlt) {
        this.m_tlt = m_tlt;
    }

    /**
     * @return the m_tblt
     */
    public TagBuilderLookupTable getTagBuilderLookupTable() {
        return m_tblt;
    }

    /**
     * @param m_tblt the m_tblt to set
     */
    public void setTagBuilderLookupTable(final TagBuilderLookupTable m_tblt) {
        this.m_tblt = m_tblt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return m_tlt.getTermAt(m_termIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextWithWsSuffix() {
        String s = m_tlt.getTermAt(m_termIndex);
        if (m_whiteSpaceIndex != 0) {
            s += m_tlt.getTermAt(m_whiteSpaceIndex);
        }
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Word> getWords() {
        Word w;
        if (m_whiteSpaceIndex == 0) {
            w = new Word(m_tlt.getTermAt(m_termIndex), "");
        } else {
            w = new Word(m_tlt.getTermAt(m_termIndex), m_tlt.getTermAt(m_whiteSpaceIndex));
        }
        return new ArrayList<Word>(1) {
            {
                add(w);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tag> getTags() {
        ArrayList<Tag> tags = new ArrayList<Tag>();
        for (int i = 0; i < m_tags.length; i++) {
            TagBuilder tb = m_tblt.getTagBuilderAt(i);
            for (int j = 0; j < m_tags[i].length; j++) {
                tags.add(tb.buildTag(m_tags[i][j]));
            }
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnmodifiable() {
        return m_immutable;
    }
    /**
     * @return the m_termIndex
     */
    public int getTermIndex() {
        return m_termIndex;
    }

    /**
     * @param m_termIndex the m_termIndex to set
     */
    public void setTermIndex(final int termIndex) {
        this.m_termIndex = termIndex;
    }

    /**
     * @param m_tags the m_tags to set
     */
    public void setTags(final int[][] tags) {
        this.m_tags = tags;
    }

    /**
     * @return the m_whiteSpaceIndex
     */
    public int getWhiteSpaceIndex() {
        return m_whiteSpaceIndex;
    }

    /**
     * @param m_whiteSpaceIndex the m_whiteSpaceIndex to set
     */
    public void setWhiteSpaceIndex(final int whiteSpaceIndex) {
        this.m_whiteSpaceIndex = whiteSpaceIndex;
    }

    /**
     * @return the m_immutable
     */
    public boolean isImmutable() {
        return m_immutable;
    }

    /**
     * @param m_immutable the m_immutable to set
     */
    public void setImmutable(final boolean immutable) {
        this.m_immutable = immutable;
    }
}

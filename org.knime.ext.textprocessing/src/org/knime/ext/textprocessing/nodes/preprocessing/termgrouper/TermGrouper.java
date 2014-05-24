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
 * -------------------------------------------------------------------
 *
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.termgrouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.ChunkPreprocessing;
import org.knime.ext.textprocessing.util.DocumentChunk;

/**
 * Groups the terms of a document by the term text and combines or deletes
 * their tags.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermGrouper implements ChunkPreprocessing {

    /**
     * The tag grouping policy, specifying that all tags are deleted.
     */
    public static final String DELETE_ALL = "Delete all";

    /**
     * The tag grouping policy, specifying that only conflicting tags are
     * deleted.
     */
    public static final String DELETE_CONFLICTING = "Delete conflicting only";

    /**
     * The tag grouping policy, specifying that all tags are kept.
     */
    public static final String KEEP_ALL = "Keep all";

    private final String m_policy;

    /**
     * Creates new instance of <code>TermGrouper</code> with given tag
     * grouping policy to set.
     *
     * @param tagGroupingPolicy The tag grouping policy to set.
     */
    public TermGrouper(final String tagGroupingPolicy) {
        m_policy = tagGroupingPolicy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hashtable<Term, Term> preprocessChunk(final DocumentChunk chunk) {
        final Hashtable<Term, Term> mapping = new Hashtable<Term, Term>();
        final Map<String, Term> newTerms = new HashMap<String, Term>(chunk.getTerms().size());
        final Map<String, Integer> tagTypeCount = new HashMap<String, Integer>();

        for (final Term t : chunk.getTerms()) {
            if (m_policy.equals(DELETE_ALL)) {
                // DELETE ALL TAGS
                Term mappedTerm = newTerms.get(t.getText());
                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), new ArrayList<Tag>(), t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);

            } else if (m_policy.equals(KEEP_ALL)) {
                // KEEP ALL TAGS
                Term mappedTerm = newTerms.get(t.getText());
                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), t.getTags(), t.isUnmodifiable());
                } else {
                    // add all tags
                    final List<Tag> newTags = new ArrayList<Tag>();
                    newTags.addAll(mappedTerm.getTags());
                    for (final Tag tag : t.getTags()) {
                        if (!newTags.contains(tag)) {
                            newTags.add(tag);
                        }
                    }

                    // create new term
                    mappedTerm = new Term(t.getWords(), newTags, t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);

            } else {
                // DELETE CONFLICTING TAGS
                Term mappedTerm = newTerms.get(t.getText());
                // count tag types
                for (final Tag tag : t.getTags()) {
                    Integer i = tagTypeCount.get(tag.getTagType());
                    if (i == null) {
                        i = 0;
                    }
                    i++;
                    tagTypeCount.put(tag.getTagType(), i);
                }

                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), t.getTags(), t.isUnmodifiable());
                } else {
                    // collect non conflicting tags
                    final List<Tag> newTags = new ArrayList<Tag>();
                    for (final Tag tag : mappedTerm.getTags()) {
                        if (tagTypeCount.get(tag.getTagType()) <= 1) {
                            newTags.add(tag);
                        }
                    }
                    for (final Tag tag : t.getTags()) {
                        if (tagTypeCount.get(tag.getTagType()) <= 1) {
                            newTags.add(tag);
                        }
                    }

                    // create new term
                    mappedTerm = new Term(t.getWords(), newTags, t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);
            }
        }

        // put all mapped terms into mapping table and return it
        for (final Term t : chunk.getTerms()) {
            final Term mappedTerm = newTerms.get(t.getText());
            if (mappedTerm != null) {
                mapping.put(t, mappedTerm);
            }
        }
        return mapping;
    }
}

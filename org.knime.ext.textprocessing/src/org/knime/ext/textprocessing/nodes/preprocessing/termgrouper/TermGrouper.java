/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.termgrouper;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.ChunkPreprocessing;
import org.knime.ext.textprocessing.util.DocumentChunk;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
    
    
    
    private String m_policy;
    
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
        Hashtable<Term, Term> mapping = new Hashtable<Term, Term>();
        Hashtable<String, Term> newTerms = new Hashtable<String, Term>(
                chunk.getTerms().size());
        Hashtable<String, Integer> tagTypeCount = 
            new Hashtable<String, Integer>();
        
        for (Term t : chunk.getTerms()) {
            if (m_policy.equals(DELETE_ALL)) {
                // DELETE ALL TAGS
                Term mappedTerm = newTerms.get(t.getText());
                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), new ArrayList<Tag>(), 
                            t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);
                
            } else if (m_policy.equals(KEEP_ALL)) {
                // KEEP ALL TAGS
                Term mappedTerm = newTerms.get(t.getText());
                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), t.getTags(), 
                            t.isUnmodifiable());
                } else {
                    // add all tags
                    List<Tag> newTags = new ArrayList<Tag>();
                    newTags.addAll(mappedTerm.getTags());
                    for (Tag tag : t.getTags()) {
                        if (!newTags.contains(tag)) {
                            newTags.add(tag);
                        }
                    }
                    
                    // create new term
                    mappedTerm = new Term(t.getWords(), newTags, 
                            t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);
                 
            } else {
                // DELETE CONFLICTING TAGS
                Term mappedTerm = newTerms.get(t.getText());
                // count tag types
                for (Tag tag : t.getTags()) {
                    Integer i = tagTypeCount.get(tag.getTagType());
                    if (i == null) {
                        i = 0;
                    }
                    i++;
                    tagTypeCount.put(tag.getTagType(), i);
                }
                
                if (mappedTerm == null) {
                    mappedTerm = new Term(t.getWords(), t.getTags(), 
                            t.isUnmodifiable());
                } else {
                    // collect non conflicting tags
                    List<Tag> newTags = new ArrayList<Tag>();
                    for (Tag tag : mappedTerm.getTags()) {
                        if (tagTypeCount.get(tag.getTagType()) <= 1) {
                            newTags.add(tag);
                        }
                    }
                    for (Tag tag : t.getTags()) {
                        if (tagTypeCount.get(tag.getTagType()) <= 1) {
                            newTags.add(tag);
                        }
                    }
                    
                    // create new term
                    mappedTerm = new Term(t.getWords(), newTags, 
                            t.isUnmodifiable());
                }
                newTerms.put(t.getText(), mappedTerm);
            }
        }
        
        // put all mapped terms into mapping table and return it
        for (Term t : chunk.getTerms()) {
            Term mappedTerm = newTerms.get(t.getText());
            if (mappedTerm != null) {
                mapping.put(t, mappedTerm);
            }
        }
        return mapping;
    }
}

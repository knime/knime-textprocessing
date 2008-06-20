/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.Collections;
import java.util.List;

/**
 * Contains all corresponding 
 * {@link org.knime.ext.textprocessing.data.Paragraph}s as a list as well as a
 * annotation ({@link org.knime.ext.textprocessing.data.SectionAnnotation}) 
 * marking out the position and rolw of the section, i.e. title, abstract, 
 * chapter, etc.  
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Section implements TextContainer {

    private List<Paragraph> m_paragraphs;
    
    private SectionAnnotation m_annotation;
    
    
    /**
     * Creates new instance of <code>Section</code> with given list of 
     * {@link org.knime.ext.textprocessing.data.Paragraph}s and the given
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} to set. If
     * one of these parameters is <code>null</code> a 
     * <code>NullPointerException</code> will be thrown.
     * 
     * @param paragraphs The list of paragraphs to set.
     * @param annotation The annotation to set.
     * @throws NullPointerException If the given list of paragraphs or the
     * annotation is <code>null</code>.
     */
    public Section(final List<Paragraph> paragraphs, 
            final SectionAnnotation annotation) throws NullPointerException {
        if (paragraphs == null) {
            throw new NullPointerException(
                    "List of paragraphs may not be null!");
        } else if (annotation == null) {
            throw new NullPointerException("Annotation may not be null!");
        }
        
        m_paragraphs = paragraphs;
        m_annotation = annotation;
    }
    
    /**
     * Creates new instance of <code>Section</code> with given list of 
     * {@link org.knime.ext.textprocessing.data.Paragraph}s. The annotation
     * is set to <code>UNKONWON</code> by default. If the given list of 
     * paragraphs is <code>null</code> a <code>NullPointerException</code> 
     * will be thrown.
     * 
     * @param paragraphs The list of paragraphs to set.
     * @throws NullPointerException If the given list of paragraphs or is 
     * <code>null</code>.
     */
    public Section(final List<Paragraph> paragraphs) 
    throws NullPointerException {
        this(paragraphs, SectionAnnotation.UNKNOWN);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_paragraphs.size(); i++) {
            sb.append(m_paragraphs.get(i).getText());
            if (i < m_paragraphs.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * @return the paragraphs of the section.
     */
    public List<Paragraph> getParagraphs() {
        return Collections.unmodifiableList(m_paragraphs);
    }

    /**
     * @return the annotation
     */
    public SectionAnnotation getAnnotation() {
        return m_annotation;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_paragraphs.size(); i++) {
            sb.append(m_paragraphs.get(i).toString());
            if (i < m_paragraphs.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Section)) {
            return false;
        }
        Section s = (Section)o;
        if (!s.getParagraphs().equals(m_paragraphs)) {
            return false;
        } else if (!s.getAnnotation().equals(m_annotation)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int fac = 119;
        int div = 19;
        int hash = 0;
        for (Paragraph p : m_paragraphs) {
            hash += fac * p.hashCode() / div; 
        }
        hash -= div * m_annotation.hashCode();
        return hash;
    }     
}

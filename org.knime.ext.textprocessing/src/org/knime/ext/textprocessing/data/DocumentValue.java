/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 * -------------------------------------------------------------------
 * 
 * History
 *   19.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.data.renderer.DefaultDataValueRendererFamily;
import org.knime.core.data.renderer.StringValueRenderer;

/**
 * Interface for the document values.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentValue extends DataValue {

    /**
     * @return The document.
     */
    public Document getDocument();    
    
    /** Meta information to this value type.
     * @see DataValue#UTILITY
     */
    public static final UtilityFactory UTILITY = 
        new DocumentUtilityFactory();
    
    /** Implementations of the meta information of this value class. */
    public static class DocumentUtilityFactory extends UtilityFactory {
        /** Singleton icon to be used to display this cell type. */
        private static final Icon ICON;

        /** Load icon, use <code>null</code> if not available. */
        static {
            ImageIcon icon;
            try {
                ClassLoader loader = DocumentValue.class.getClassLoader();
                String path = 
                    DocumentValue.class.getPackage().getName()
                    .replace('.', '/');
                icon = new ImageIcon(loader.getResource(path 
                                + "/icon/DocumentValue.png"));
            } catch (Exception e) {
                icon = null;
            }
            ICON = icon;
        }

        private static final DocumentValueComparator DOCUMENT_COMPARATOR = 
            new DocumentValueComparator();
        
        /** Only subclasses are allowed to instantiate this class. */
        protected DocumentUtilityFactory() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DataValueComparator getComparator() {
            return DOCUMENT_COMPARATOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected DataValueRendererFamily getRendererFamily(
                final DataColumnSpec spec) {
            return new DefaultDataValueRendererFamily(
                    StringValueRenderer.INSTANCE);
        }

    }
}

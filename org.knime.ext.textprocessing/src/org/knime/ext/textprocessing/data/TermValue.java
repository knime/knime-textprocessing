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
 *   04.01.2007 (thiel): created
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
 * Interface supporting the term values.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TermValue extends DataValue {

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Term} instance.
     */
    public Term getTermValue();
    
    /** Meta information to this value type.
     * @see DataValue#UTILITY
     */
    public static final UtilityFactory UTILITY = 
        new TermUtilityFactory();
    
    /** Implementations of the meta information of this value class. */
    public static class TermUtilityFactory extends UtilityFactory {
        /** Singleton icon to be used to display this cell type. */
        private static final Icon ICON;

        /** Load icon, use <code>null</code> if not available. */
        static {
            ImageIcon icon;
            // TODO create a particular icon for the DocumentListValue
            try {
                ClassLoader loader = TermValue.class.getClassLoader();
                String path = 
                    TermValue.class.getPackage().getName()
                    .replace('.', '/');
                icon = new ImageIcon(loader.getResource(path 
                                + "/icon/TermValue.png"));
            } catch (Exception e) {
                icon = null;
            }
            ICON = icon;
        }

        private static final TermValueComparator TERM_COMPARATOR = 
            new TermValueComparator();
        
        /** Only subclasses are allowed to instantiate this class. */
        protected TermUtilityFactory() {
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
            return TERM_COMPARATOR;
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

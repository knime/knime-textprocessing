/* 
========================================================================
 *
 *  Copyright (C) 2003 - 2013
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

    /**
     * Meta information to this value type.
     * 
     * @see DataValue#UTILITY
     */
    public static final UtilityFactory UTILITY = new DocumentUtilityFactory();

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
                icon =
                        new ImageIcon(loader.getResource(path
                                + "/icon/DocumentValue.png"));
            } catch (Exception e) {
                icon = null;
            }
            ICON = icon;
        }

        private static final DocumentValueComparator DOCUMENT_COMPARATOR =
                new DocumentValueComparator();

        /** Only subclasses are allowed to instantiate this class. */
        protected DocumentUtilityFactory() { /* empty */
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

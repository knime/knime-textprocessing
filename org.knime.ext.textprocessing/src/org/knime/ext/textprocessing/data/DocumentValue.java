/*
========================================================================
 *
 *  Copyright by 
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

import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.ExtensibleUtilityFactory;

/**
 * Interface for the document values.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentValue extends DataValue {
    /**
     * @return The document.
     */
    Document getDocument();

    /**
     * Meta information to this value type.
     *
     * @see DataValue#UTILITY
     */
    UtilityFactory UTILITY = new DocumentUtilityFactory();

    /** Implementations of the meta information of this value class. */
    class DocumentUtilityFactory extends ExtensibleUtilityFactory {
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
        protected DocumentUtilityFactory() {
            super(DocumentValue.class);
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
        public String getName() {
            return "Text documents";
        }
    }
}

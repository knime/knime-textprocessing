/*
 * ------------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 *
 * History
 *   04.01.2007 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.ExtensibleUtilityFactory;

/**
 * Interface supporting the term values.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface TermValue extends DataValue {
    /**
     * @return The {@link org.knime.ext.textprocessing.data.Term} instance.
     */
    Term getTermValue();

    /**
     * Meta information to this value type.
     *
     * @see DataValue#UTILITY
     */
    UtilityFactory UTILITY = new TermUtilityFactory();

    /** Implementations of the meta information of this value class. */
    class TermUtilityFactory extends ExtensibleUtilityFactory {
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
                icon =
                        new ImageIcon(loader.getResource(path
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
            super(TermValue.class);
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
        public String getName() {
            return "Terms";
        }
    }
}

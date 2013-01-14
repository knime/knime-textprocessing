/*
 * ------------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataTableSpec;

/**
 * All classes building up {@link org.knime.core.data.DataTable}s have to
 * implement this interface in order to provide a common type.
 * The method {@link DataTableBuilder#createDataTableSpec()} returns the
 * {@link org.knime.core.data.DataTableSpec} of the data table created by each
 * concrete implementation.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DataTableBuilder {
    
    /**
     * @return The <code>DataTableSpec</code> of the data table build by the
     * underlying implementation.
     */
    public DataTableSpec createDataTableSpec();
}

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
 *   Jul 18, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.clustering;

import java.util.HashSet;

/**
 * Represents a cluster of elements of type T.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the elements contained in the cluster
 */
public class Cluster<T> extends HashSet<T> {
    private static final long serialVersionUID = 1L;
}

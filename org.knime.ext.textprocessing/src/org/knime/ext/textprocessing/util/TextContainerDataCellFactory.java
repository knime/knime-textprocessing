/* ------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * Classes implementing this interface are factories which create certain types
 * of {@link org.knime.core.data.DataCell}s containing certain types of 
 * {@link org.knime.ext.textprocessing.data.TextContainer}. The method
 * {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory#createDataCell(TextContainer)}
 * which has to be implemented creates a new <code>DataCell</code> of a certain 
 * type, i.e. a <code>DocumentCell</code> containing the given 
 * <code>TextContainer</code> which has to be of the right type accordant to 
 * the <code>DataCell</code> (i.e. <code>Document</code>).
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TextContainerDataCellFactory {
    
    /**
     * creates a new <code>DataCell</code> of a certain 
     * type, i.e. a <code>DocumentCell</code> containing the given 
     * <code>TextContainer</code>. If the type of the container does not match
     * the type of the <code>DataCell</code> to create (i.e. a 
     * <code>DocumentCell</code> requires a <code>Document</code> 
     * <code>null</code> is returned, since no proper <code>DataCell</code>
     * can be created.
     * 
     * @param tc The <code>TextContainer</code> to create the 
     * <code>DataCell</code> for.
     * @return The created <code>DataCell</code> containing the given
     * <code>TextContainer</code>  
     */
    public DataCell createDataCell(final TextContainer tc);

}

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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;


/**
 * A factory class which provides the proper 
 * {@link org.knime.ext.textprocessing.util.DataTableBuilder} to build data 
 * tables. Selectively a builder is returned which builds data tables containing
 * <code>BlobDataCell</code>s or usual <code>DataCell</code>s depending on
 * the KNIME Textprocessing preference settings.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DataTableBuilderFactory {

    private DataTableBuilderFactory() { }
    
    /**
     * @return The <code>DataTableBuilder</code> creating Bag of Words.
     */
    public static BagOfWordsDataTableBuilder createBowDataTableBuilder() {
        if (!TextprocessingPreferenceInitializer.useBlobCell()) {
            return new BagOfWordsCellDataTableBuilder();
        }
        return new BagOfWordsBlobCellDataTableBuilder();
    }
    
    /**
     * @return The <code>DataTableBuilder</code> creating document tables.
     */
    public static DocumentDataTableBuilder createDocumentDataTableBuilder() {
        if (!TextprocessingPreferenceInitializer.useBlobCell()) {
            return new DocumentCellDataTableBuilder();
        }
        return new DocumentBlobCellDataTableBuilder();
    }
}

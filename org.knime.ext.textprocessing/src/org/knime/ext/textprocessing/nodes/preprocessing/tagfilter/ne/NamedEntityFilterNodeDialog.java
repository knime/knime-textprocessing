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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.ne;

import java.util.Set;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilterNodeDialog;

/**
 * The dialog class of the named entity filer node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NamedEntityFilterNodeDialog extends TagFilterNodeDialog {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<Tag> getTags() {
        return NamedEntityFilterNodeModel.getTags();
    }
}

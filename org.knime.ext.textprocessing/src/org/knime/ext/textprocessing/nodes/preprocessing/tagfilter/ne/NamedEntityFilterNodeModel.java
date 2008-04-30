/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilterNodeModel;

/**
 * The model class of the named entity filer node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NamedEntityFilterNodeModel extends TagFilterNodeModel {

    /**
     * @return The set of all tags which can be specified as valid.
     */
    public static Set<Tag> getTags() {
        Set<Tag> tags = new HashSet<Tag>();
        List<String> tagStrs = NamedEntityTag.asStringList();
        for (String s : tagStrs) {
            tags.add(NamedEntityTag.stringToTag(s));
        }
        return tags;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TagBuilder getTagBuilder() {
        return NamedEntityTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValidTagType() {
        return NamedEntityTag.TAG_TYPE;
    }
}

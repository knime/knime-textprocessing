/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.abner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.BiomedicalNeTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.nodes.preprocessing.tagfilter.TagFilterNodeModel;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTagFilterNodeModel extends TagFilterNodeModel {
    
    /**
     * @return The set of all tags which can be specified as valid.
     */
    public static Set<Tag> getTags() {
        Set<Tag> tags = new HashSet<Tag>();
        List<String> tagStrs = BiomedicalNeTag.asStringList();
        for (String s : tagStrs) {
            tags.add(BiomedicalNeTag.stringToTag(s));
        }
        return tags;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TagBuilder getTagBuilder() {
        return BiomedicalNeTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValidTagType() {
        return BiomedicalNeTag.TAG_TYPE;
    }
}

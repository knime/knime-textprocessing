/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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

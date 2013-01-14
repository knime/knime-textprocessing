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
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.List;
import java.util.Set;

/**
 * This interface has to be implemented by all different kind of tags such as
 * i.e. {@link org.knime.ext.textprocessing.data.PartOfSpeechTag}. It provides a
 * method to create a valid {@link org.knime.ext.textprocessing.data.Tag} due to
 * the given type. If the underlying implementation is not responsible for the
 * given type no tag instance will be created but <code>null</code>. <br/>
 * <br/>
 * To create your own tag type, i.e. named entity tag type, etc. set up an
 * <code>enum</code> like the
 * {@link org.knime.ext.textprocessing.data.PartOfSpeechTag}. This
 * <code>enum</code> has to implement the interface <code>TagBuilder</code> and
 * additionally it has to provide the method
 * <code>public static Tag getDefault()</code> which returns the default
 * instance of the tag type <code>enum</code> as a <code>TagBuilder</code>. If
 * these conditions are fulfilled, the tag type can be registered via an xml
 * file (see tagset.dtd for details) at the
 * {@link org.knime.ext.textprocessing.data.TagFactory} by calling
 * {@link org.knime.ext.textprocessing.data.TagFactory#addTagSet(java.io.File)}.
 * For more details see: <br/> 
 * {@link org.knime.ext.textprocessing.data.TagFactory}
 * or <br/> {@link org.knime.ext.textprocessing.data.PartOfSpeechTag}.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TagBuilder {

    /**
     * Builds a valid {@link org.knime.ext.textprocessing.data.Tag} instance if
     * there exists a tag with the given string value, otherwise null.
     * 
     * @param value The value of the tag to create.
     * @return The valid instance of a tag if there exists a tag with the given
     *         string value, otherwise <code>null</code>.
     */
    public Tag buildTag(final String value);

    /**
     * Returns a list of all valid tag values of the underlying tag set as
     * strings.
     * 
     * @return a list of all valid tag values of the underlying tag set.
     */
    public List<String> asStringList();

    /**
     * Returns a set of all valid tags of the underlying tag set.
     * 
     * @return a set of all valid tags of the underlying tag set.
     */
    public Set<Tag> getTags();

    /**
     * @return The type of the underlying <code>TagBuilder</code>
     *         implementation.
     */
    public String getType();
}

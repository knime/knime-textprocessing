/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   29.05.2017 (Julian): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides methods given by the {@link TagBuilder} interface to use the
 * {@link AncoraSpanishTreebankTagSet}.
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.4
 */
public class AncoraSpanishTreebankTag implements TagBuilder {

    /**
     * Default tag value.
     */
    public static final String DEFAULT_TAG = AncoraSpanishTreebankTagSet.UNKNOWN.toString();

    /**
     * Creates new instance of {@code SimplifiedSpanishTreebankTag} and {@link org.knime.ext.textprocessing.data.Tag}
     * with the specified Penn Treebank POS tag.
     */
    public AncoraSpanishTreebankTag() {
        // Emtpty constructor...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag buildTag(final String value) {
        return AncoraSpanishTreebankTagSet.stringToTag(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> asStringList() {
        Enum<AncoraSpanishTreebankTagSet>[] values = AncoraSpanishTreebankTagSet.values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new LinkedHashSet<Tag>(AncoraSpanishTreebankTagSet.values().length);
        for (AncoraSpanishTreebankTagSet tag : AncoraSpanishTreebankTagSet.values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return AncoraSpanishTreebankTagSet.TAG_TYPE;
    }

}

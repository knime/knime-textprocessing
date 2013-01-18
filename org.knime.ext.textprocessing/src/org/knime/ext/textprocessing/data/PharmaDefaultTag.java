/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   07.10.2011 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tag set with standard tags used in pharmaceutical companies.
 * @author Tobias Koetter, University of Konstanz
 * @since 2.8
 */
public enum PharmaDefaultTag implements TagBuilder {
    /** Cell. */
    CELL,
    /** Cell line. */
    CELL_LINE,
    /** Cell type. */
    CELL_TYPE,
    /** COMPOUND. */
    COMPOUND,
    /** Disease. */
    DISEASE,
    /**DNA.*/
    DNA,
    /** Drug. */
    DRUG,
    /**Functional group.*/
    FUNCTIONAL_GROUP,
    /** Gene. */
    GENE,
    /** Molecule. */
    MOLECULE,
    /** Ontology. */
    ONTOLOGY,
    /** POLYMER. */
    POLYMER,
    /** Protein. */
    PROTEIN,
    /** REACTION. */
    REACTION,
    /**RNA.*/
    RNA,
    /** Small molecule. */
    SMALL_MOLECULE,
    /** Unknown. */
    UNKOWN;

    private final Tag m_tag;

    /**
     * The constant for Pharma tag types.
     */
    public static final String TAG_TYPE = "PHARMA";

    /**
     * Creates new instance of <code>OscarChemDefaultTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified Oscar
     * tag.
     */
    private PharmaDefaultTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified <code>OscarChemDefaultTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the enum fields as a String list of their names.
     *
     * @return - the enum fields as a String list of their names.
     */
    @Override
    public List<String> asStringList() {
        Enum<PharmaDefaultTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the
     * given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * <code>UNKNOWN</code> tag is returned.
     *
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the
     *         given string.
     */
    public static Tag stringToTag(final String str) {
        for (PharmaDefaultTag ne : values()) {
            if (ne.getTag().getTagValue().equals(str)) {
                return ne.getTag();
            }
        }
        return PharmaDefaultTag.getDefault().getTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag buildTag(final String value) {
        return PharmaDefaultTag.stringToTag(value);
    }

    /**
     * @return The default "UNKNOWN" <code>OscarChemDefaultTag</code> as
     *         <code>TagBuilder</code>.
     */
    public static PharmaDefaultTag getDefault() {
        return PharmaDefaultTag.UNKOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TAG_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new HashSet<Tag>(values().length);
        for (PharmaDefaultTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }
}

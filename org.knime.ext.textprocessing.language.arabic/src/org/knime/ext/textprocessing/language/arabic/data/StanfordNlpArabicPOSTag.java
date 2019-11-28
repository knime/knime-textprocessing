/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Nov 26, 2019 (Julian Bunzel, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ext.textprocessing.language.arabic.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * This class provides methods given by the {@link TagBuilder} interface to use the StanfordNLP Arabic tag set.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public final class StanfordNlpArabicPOSTag implements TagBuilder {

    /**
     * The tag set is from StanfordNLP's {@link MaxentTagger#readModelAndInit} class which creates a TTags object
     * holding all available tags for the specific language.
     *
     * TODO: Whenever the StanfordNLP library/models are updated, we need to check that the we still provide the correct
     * tag set.
     */
    private static final Set<String> TAG_SET;
    static {
        TAG_SET = Collections.unmodifiableSet(Stream
            .of(".$$.", "ADJ_NUM", "CC", "CD", "CPRP$", "DT", "DTJJ", "DTJJR", "DTNN", "DTNNP", "DTNNPS", "DTNNS", "IN",
                "JJ", "JJR", "NN", "NNP", "NNPS", "NNS", "NOUN_QUANT", "PRP", "PRP$", "PUNC", "RB", "RP", "UH",
                "UNKNOWN", "VB", "VBD", "VBG", "VBN", "VBP", "VN", "WP", "WRB")
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * The tag type constant for the StanfordNLP Arabic Part-of-speech tag set.
     */
    private static final String TAG_TYPE = "ARABPOS";

    @Override
    public Tag buildTag(final String value) {
        return TAG_SET.contains(value) ? new Tag(value, TAG_TYPE) : new Tag("UNKNOWN", TAG_TYPE);
    }

    @Override
    public List<String> asStringList() {
        return new ArrayList<>(TAG_SET);
    }

    @Override
    public Set<Tag> getTags() {
        return (TAG_SET).stream()//
            .map(tv -> new Tag(tv, TAG_TYPE))//
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getType() {
        return TAG_TYPE;
    }

}

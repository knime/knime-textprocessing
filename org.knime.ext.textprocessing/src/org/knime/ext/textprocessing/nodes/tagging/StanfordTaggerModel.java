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
 *   05.05.2017 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger.nermodels.English3ClassesDistsimModel;

/**
 * This interface has to be implemented to add Stanford part-of-speech or named entity recognition models to the KNIME
 * Text Processing extension. It provides methods to return the model's name, file path, and {@link TagBuilder} that
 * contains an enum with tags used by the specific model.<br>
 * <br>
 * To add another Stanford model for the Stanford (pos) tagger or the Stanford NE tagger node, create a class like the
 * {@link English3ClassesDistsimModel}. and register it as an {@code org.knime.ext.textprocessing.StanfordTaggerModel}
 * extension in the {@code plugin.xml}.
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.4
 */
public interface StanfordTaggerModel {

    /**
     * The name of the model. This name will be shown in the model selection dialog of the specific tagger nodes.
     *
     * @return Returns the name of the model.
     */
    public String getModelName();

    /**
     * The path to the model file.
     *
     * @return Returns the path of the model.
     */
    public String getModelPath();

    /**
     * The {@link TagBuilder}, containing the tag set, providing the tags, that will be assigned by the specific model.
     *
     * @return Returns the TagBuilder.
     */
    public TagBuilder getTagBuilder();

    /**
     * Creates proper tags out of the given string and returns them.
     * Override this method for special tag handling.
     *
     * @param tag The string to create a tag out of
     * @return A list of tags build out of the given string
     */
    public default List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<>();
        tags.add(getTagBuilder().buildTag(tag));
        return tags;
    }
}

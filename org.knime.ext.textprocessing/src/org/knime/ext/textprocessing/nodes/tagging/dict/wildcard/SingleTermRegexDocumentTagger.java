/*
 * ------------------------------------------------------------------------
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
 * Created on 04.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.wildcard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;


/**
 * Tags single terms as named entities if at least one of the specified regular expressions matches. If more than one
 * expression is matching the last matching expression overrides previous, conflicting matches.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class SingleTermRegexDocumentTagger extends RegexDocumentTagger {

    /**
     * Creates a new instance of <code>SingleTermRegexDocumentTagger</code> with
     * given flag to set found named entities unmodifiable, to ignore the case
     * of the named entities to detect, the tag to assign to the found named
     * entities and the set of regular expressions to match.
     *
     * @param setUnmodifiable If <code>true</code> found named entities are set
     * unmodifiable, otherwise not.
     * @param regexpattern The set of regex pattern to match.
     * @param tag The tag to assign to found named entities.
     * @param caseSensitive If <code>false</code> the case of named entities
     * and words of the sentences are ignored, otherwise not.
     */
    public SingleTermRegexDocumentTagger(final boolean setUnmodifiable, final Set<Pattern> regexpattern, final Tag tag,
            final boolean caseSensitive) {
        super(setUnmodifiable, regexpattern, tag, caseSensitive);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger#tagEntities(org.knime.ext.textprocessing.data
     * .Sentence)
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
      final List<TaggedEntity> foundEntities = new ArrayList<TaggedEntity>();

      final Set<Pattern> pattern = getRegexpattern();
      final List<Term> terms = sentence.getTerms();

      for (Pattern p : pattern) {
          for (Term t : terms) {
              String termStr = t.getText();
              if (!m_caseSensitive) {
                  termStr = termStr.toLowerCase();
              }

              if (p.matcher(termStr).matches()) {
                  TaggedEntity taggedEntity = new TaggedEntity(termStr, getTag().getTagValue());
                  foundEntities.add(taggedEntity);
              }
          }
      }

      return foundEntities;
    }

}

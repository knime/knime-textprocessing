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
 * Created on 05.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.wildcard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.knime.base.util.WildcardMatcher;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel;


/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 * @deprecated Use {@link WildcardTaggerNodeModel2} instead.
 */
@Deprecated
public class WildcardTaggerNodeModel extends AbstractDictionaryTaggerModel {

    /**
     * The single term matching setting.
     */
    public static final String SINGLETERM_MATCHINGLEVEL = "Single term (term based)";

    /**
     * The multi term matching setting.
     */
    public static final String MULTITERM_MATCHINGLEVEL = "Multi term (sentence based)";

    /**
     * The default term matching setting.
     */
    public static final String DEF_MATCHINGLEVEL = SINGLETERM_MATCHINGLEVEL;

    /**
     * The wildcard matching method setting.
     */
    public static final String WILDCARD_MATCHINGMETHOD = "Wildcard ('*' = any sequence, '?' = any character)";

    /**
     * The regex matching method setting.
     */
    public static final String REGEX_MATCHINGMETHOD = "Regular expression";

    /**
     * The default matching method.
     */
    public static final String DEF_MATCHINGMETHOD = WILDCARD_MATCHINGMETHOD;


    private final SettingsModelString m_matchingLevelModel = WildcardTaggerNodeDialog.createMatchingLevelModel();

    private final SettingsModelString m_matchingMethodModel = WildcardTaggerNodeDialog.createMatchingMethodModel();

    private Pattern createPattern(final String str) throws PatternSyntaxException {
        String regexStr = str;
        Pattern p = null;

        if (!getCaseSensitiveSetting()) {
            p = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(regexStr);
        }

        return p;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#createDocumentTagger(java.util.Set)
     */
    @Override
    protected DocumentTagger createDocumentTagger(final Set<String> dictionary) {
        final Set<Pattern> pattern = new LinkedHashSet<Pattern>();
        final List<String> invalidPattern = new ArrayList<String>();
        final boolean wildcardMatching = m_matchingMethodModel.getStringValue().equals(WILDCARD_MATCHINGMETHOD);

        // create regex pattern
        for (String str : dictionary) {
            Pattern p = null;

            // transform wildcard expression into regular expression
            if (wildcardMatching) {
                str = WildcardMatcher.wildcardToRegex(str);
            }

            try {
                p = createPattern(str);
                if (p != null) {
                    pattern.add(p);
                }
            } catch (PatternSyntaxException e) {
                invalidPattern.add(str);
            } finally {
                if (p != null) {
                    pattern.add(p);
                }
            }
        }

        // prepare warning msg if invalid pattern have been detected
        if (invalidPattern.size() > 0) {
            final StringBuilder invalidPatternMsg = new StringBuilder();
            int count = 0;
            for (String ip : invalidPattern) {
                if (invalidPatternMsg.length() > 0) {
                    invalidPatternMsg.append(", ");
                }
                invalidPatternMsg.append(ip);
                count++;

                if (count > 3) {
                    break;
                }
            }
            if (invalidPattern.size() > count) {
                invalidPatternMsg.append(", ...");
            }

            this.setWarningMessage("Found " + invalidPattern.size() + " invalid pattern in dictionary ["
                    + invalidPatternMsg.toString() + "]!");
        }

        // create single or multi term tagger
        if (m_matchingLevelModel.getStringValue().equals(SINGLETERM_MATCHINGLEVEL)) {
            return new SingleTermRegexDocumentTagger(getUnmodifiableSetting(), pattern, getTagSetting(),
                                                    getCaseSensitiveSetting(), getTokenizerName());
        } else {
            return new MultiTermRegexDocumentTagger(getUnmodifiableSetting(), pattern, getTagSetting(),
                                                 getCaseSensitiveSetting(), getTokenizerName());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#saveSettingsToInternal(org.knime
     * .core.node.NodeSettingsWO)
     */
    @Override
    protected void saveSettingsToInternal(final NodeSettingsWO settings) {
        m_matchingLevelModel.saveSettingsTo(settings);
        m_matchingMethodModel.saveSettingsTo(settings);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#validateSettingsInternal(org.knime
     * .core.node.NodeSettingsRO)
     */
    @Override
    protected void validateSettingsInternal(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_matchingLevelModel.validateSettings(settings);
        m_matchingMethodModel.validateSettings(settings);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel#loadValidatedSettingsFromInternal
     * (org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFromInternal(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_matchingLevelModel.loadSettingsFrom(settings);
        m_matchingMethodModel.loadSettingsFrom(settings);
    }
}

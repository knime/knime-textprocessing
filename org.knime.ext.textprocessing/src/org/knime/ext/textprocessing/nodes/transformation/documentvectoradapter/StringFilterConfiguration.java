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
 *   04.09.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import java.util.List;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.util.filter.NameFilterConfiguration;

/**
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 */
public class StringFilterConfiguration extends NameFilterConfiguration {

    /**
     * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
     */
    public static class StringFilterResult extends FilterResult {
        /**
         * @param filter
         * @param includeMissing
         */
        public StringFilterResult(final FilterResult filter, final boolean includeMissing) {
            super(filter.getIncludes(), filter.getExcludes(), filter.getRemovedFromIncludes(),
                filter.getRemovedFromExcludes());
        }

        /**
         * @param incls
         * @param excls
         * @param removedFromIncludes
         * @param removedFromExcludes
         * @param includeMissing
         */
        public StringFilterResult(final String[] incls, final String[] excls, final String[] removedFromIncludes,
            final String[] removedFromExcludes, final boolean includeMissing) {
            super(incls, excls, removedFromIncludes, removedFromExcludes);
        }

        /**
         * @param incls list of included elements
         * @param excls list of excluded elements
         * @param removedFromIncludes see {@link #getRemovedFromIncludes()}
         * @param removedFromExcludes see {@link #getRemovedFromExcludes()}
         * @param includeMissing whether missing values should be included
         */
        public StringFilterResult(final List<String> incls, final List<String> excls, final List<String> removedFromIncludes,
            final List<String> removedFromExcludes, final boolean includeMissing) {
            super(incls, excls, removedFromIncludes, removedFromExcludes);
        }
    }

    /**
     * @param configRootName
     */
    public StringFilterConfiguration(final String configRootName) {
        super(configRootName);
        setEnforceOption(EnforceOption.EnforceInclusion);
    }

    /**
     * @param settings
     * @param names
     */
    public void loadConfigurationForDialog(final NodeSettingsRO settings, final String[] names) {
        super.loadConfigurationInDialog(settings, names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getIncludeList() {
        return super.getIncludeList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getExcludeList() {
        return super.getExcludeList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDefaults(final String[] names, final boolean includeByDefault) {
        super.loadDefaults(names, includeByDefault);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDefaults(final String[] includes, final String[] excludes, final EnforceOption enforceOption) {
        super.loadDefaults(includes, excludes, enforceOption);
    }

    /** {@inheritDoc} */
    @Override
    protected NameFilterConfiguration clone() {
        StringFilterConfiguration clone = (StringFilterConfiguration)super.clone();
        return clone;
    }

    /**
     * @return the pattern config
     */
    @Override
    protected StringPatternFilterConfiguration createPatternConfig() {
        return new StringPatternFilterConfiguration();
    }


}

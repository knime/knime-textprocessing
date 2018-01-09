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
 *   04.09.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import java.util.List;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.util.filter.NameFilterConfiguration;

/**
 * Configuration class for the DialogComponentStringFilter.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.5
 */
public class StringFilterConfiguration extends NameFilterConfiguration {

    /**
     * The {@code StringFilterResult} stores included and excluded Strings as well as Strings that have
     * been removed from the included or excluded String array, respectively.
     *
     * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
     */
    public static class StringFilterResult extends FilterResult {
        /**
         * Creates a new instance of {@code StringFilterResult} based on a {@code FilterResult}.
         *
         * @param filter The filter result.
         * @param includeMissing If missing values should be included.
         */
        public StringFilterResult(final FilterResult filter, final boolean includeMissing) {
            super(filter.getIncludes(), filter.getExcludes(), filter.getRemovedFromIncludes(),
                filter.getRemovedFromExcludes());
        }

        /**
         * Creates a new instance of {@code StringFilterResult} based on String arrays that contain included and
         * excluded Strings as well as Strings that have been removed from the included and excluded String arrays.
         *
         * @param incls included elements
         * @param excls excluded elements
         * @param removedFromIncludes see {@link #getRemovedFromIncludes()}
         * @param removedFromExcludes see {@link #getRemovedFromExcludes()}
         * @param includeMissing whether missing values should be included
         */
        public StringFilterResult(final String[] incls, final String[] excls, final String[] removedFromIncludes,
            final String[] removedFromExcludes, final boolean includeMissing) {
            super(incls, excls, removedFromIncludes, removedFromExcludes);
        }

        /**
         * Creates a new instance of {@code StringFilterResult} based on lists that contain included and
         * excluded Strings as well as list of Strings that have been removed from the included and excluded lists.
         *
         * @param incls list of included elements
         * @param excls list of excluded elements
         * @param removedFromIncludes see {@link #getRemovedFromIncludes()}
         * @param removedFromExcludes see {@link #getRemovedFromExcludes()}
         * @param includeMissing whether missing values should be included
         */
        public StringFilterResult(final List<String> incls, final List<String> excls,
            final List<String> removedFromIncludes, final List<String> removedFromExcludes,
            final boolean includeMissing) {
            super(incls, excls, removedFromIncludes, removedFromExcludes);
        }
    }

    /**
     * Creates a new instance of {@code StringFilterConfiguration}.
     *
     * @param configRootName The name of the configuration.
     */
    public StringFilterConfiguration(final String configRootName) {
        super(configRootName);
        setEnforceOption(EnforceOption.EnforceInclusion);
    }

    /**
     * Loads the configuration to use it in the node dialog.
     *
     * @param settings The node settings.
     * @param names The column names.
     */
    public void loadConfigurationForDialog(final NodeSettingsRO settings, final String[] names) {
        super.loadConfigurationInDialog(settings, names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDefaults(final String[] names, final boolean includeByDefault) {
        super.loadDefaults(names, includeByDefault);
    }

    /**
     * Creates and returns a new {@code StringPatternFilterConfiguration}.
     *
     * @return The pattern configuration.
     */
    @Override
    protected StringPatternFilterConfiguration createPatternConfig() {
        return new StringPatternFilterConfiguration();
    }

}

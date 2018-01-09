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
 *   05.09.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public final class TokenizerFactoryRegistry {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TokenizerFactoryRegistry.class);

    /**
     * The extension point id of the TokenizerFactory extension point.
     */
    public static final String EXT_POINT_ID = "org.knime.ext.textprocessing.TokenizerFactory";

    /**
     * The extension point attribute.
     */
    public static final String EXT_POINT_ATTR_DF = "TokenizerFactory";

    private static TokenizerFactoryRegistry instance;

    private Map<String, TokenizerFactory> m_tokenizerMap = new TreeMap<>();

    private TokenizerFactoryRegistry() {
        registerExtensionPoints();
    }

    /**
     * @return Returns a new instance of {@code TokenizerFactoryRegistry}
     */
    public static synchronized TokenizerFactoryRegistry getInstance() {
        if (instance == null) {
            instance = new TokenizerFactoryRegistry();
        }
        return instance;
    }

    private void registerTokenizerFactory(final TokenizerFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null.");
        }
        final String name = factory.getTokenizerName();
        final TokenizerFactory existingFactory = m_tokenizerMap.get(name);
        if (existingFactory != null) {
            throw new IllegalArgumentException("TokenizerFactory with name: " + name + " already registered for class: "
                + existingFactory.getClass().getName());
        }
        m_tokenizerMap.put(name, factory);
    }

    private void registerExtensionPoints() {
        try {
            final IExtensionRegistry registry = Platform.getExtensionRegistry();
            final IExtensionPoint point = registry.getExtensionPoint(EXT_POINT_ID);
            if (point == null) {
                LOGGER.error("Invalid extension point: " + EXT_POINT_ID);
                throw new IllegalStateException("ACTIVATION ERROR: " + " --> Invalid extension point: " + EXT_POINT_ID);
            }
            for (final IConfigurationElement elem : point.getConfigurationElements()) {
                final String operator = elem.getAttribute(EXT_POINT_ATTR_DF);
                final String decl = elem.getDeclaringExtension().getUniqueIdentifier();

                if (operator == null || operator.isEmpty()) {
                    LOGGER.error("The extension '" + decl + "' doesn't provide the required attribute '"
                        + EXT_POINT_ATTR_DF + "'");
                    LOGGER.error("Extension " + decl + " ignored.");
                    continue;
                }

                try {
                    final TokenizerFactory factory =
                        (TokenizerFactory)elem.createExecutableExtension(EXT_POINT_ATTR_DF);
                    registerTokenizerFactory(factory);
                } catch (final Throwable t) {
                    LOGGER.error("Problems during initialization of tokenizer factory (with id '" + operator + "'.)");
                    if (decl != null) {
                        LOGGER.error("Extension " + decl + "ignored.", t);
                    }
                }

            }
        } catch (final Exception e) {
            LOGGER.error("Exception while registering TokenizerFactory extensions");
        }
    }

    /**
     * @return Returns the map containing the name of the tokenizer and the related TokenizerFactory.
     */
    public static ImmutableMap<String, TokenizerFactory> getTokenizerFactoryMap() {
        return ImmutableMap.copyOf(getInstance().m_tokenizerMap);
    }

    /**
     * @return Returns m_tokenizerMap as a String array, so it can be used for the {@code ComboFieldEditor} in the
     *         {@code TextprocessingPreferenceInitializer} class.
     * @since 3.3
     */
    public static String[][] getMapAsStringArray() {
        ImmutableMap<String, TokenizerFactory> map = getTokenizerFactoryMap();
        String[][] array = new String[map.size()][2];
        int counter = 0;
        for (ImmutableMap.Entry<String, TokenizerFactory> entry : map.entrySet()) {
            array[counter][0] = entry.getKey();
            array[counter][1] = entry.getKey();
            counter++;
        }
        return array;
    }

}

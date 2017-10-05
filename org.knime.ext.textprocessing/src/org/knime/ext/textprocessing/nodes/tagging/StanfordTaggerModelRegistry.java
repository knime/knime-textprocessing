/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   05.05.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;

import com.google.common.collect.ImmutableMap;

/**
 * The {@code StanfordTaggerModelRegistry} registers all {@link StanfordTaggerModel}s that have been added to the
 * StanfordTaggerModel extension point. Those models are used by the Stanford tagger and StanfordNLP NE tagger,
 * respectively. To add another model, the {@link StanfordTaggerModel} interface has to be implemented and a new entry
 * in the extension point has to be added.
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.4
 */
public class StanfordTaggerModelRegistry {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StanfordTaggerModelRegistry.class);

    /**
     * The extension point id of the StanfordTaggerModel extension point.
     */
    public static final String EXT_POINT_ID = "org.knime.ext.textprocessing.StanfordTaggerModel";

    /**
     * The extension point model attribute.
     */
    public static final String EXT_POINT_ATTR_DF = "StanfordTaggerModel";

    /**
     * The extension point type attribute.
     */
    private static final String EXT_POINT_ATTR_TYPE = "type";

    private static final String MODEL_TYPE_NER = "Named Entity Recognition";

    private static final String MODEL_TYPE_POS = "Part-Of-Speech";

    private static StanfordTaggerModelRegistry instance;

    // The map containing all registered pos tagger models
    private Map<String, StanfordTaggerModel> m_posTaggerMap = new TreeMap<>();

    // The map containing all registered ner tagger models
    private Map<String, StanfordTaggerModel> m_nerTaggerMap = new TreeMap<>();

    private StanfordTaggerModelRegistry() {
        registerExtensionPoints();
    }

    /**
     * @return Returns a new instance of {@code StanfordTaggerModelRegistry}
     */
    public static synchronized StanfordTaggerModelRegistry getInstance() {
        if (instance == null) {
            instance = new StanfordTaggerModelRegistry();
        }
        return instance;
    }

    private void registerStanfordTaggerModel(final StanfordTaggerModel model, final String type) {
        if (model == null) {
            throw new NullPointerException("factory must not be null.");
        }
        final String name = model.getModelName();
        final StanfordTaggerModel existingPosModel = m_posTaggerMap.get(name);
        final StanfordTaggerModel existingNerModel = m_nerTaggerMap.get(name);
        if (existingPosModel != null && type.equals(MODEL_TYPE_POS)) {
            LOGGER.error("StanfordTaggerModel with name: '" + name + "' already registered.");
            throw new IllegalArgumentException("StanfordTaggerModel with name: " + name
                + " already registered for class: " + existingPosModel.getClass().getName());
        } else if (existingNerModel != null && type.equals(MODEL_TYPE_NER)) {
            LOGGER.error("StanfordTaggerModel with name: '" + name + "' already registered.");
            throw new IllegalArgumentException("StanfordTaggerModel with name: " + name
                + " already registered for class: " + existingNerModel.getClass().getName());
        }
        if (type.equals(MODEL_TYPE_NER)) {
            m_nerTaggerMap.put(name, model);
        } else if (type.equals(MODEL_TYPE_POS)) {
            m_posTaggerMap.put(name, model);
        }
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
                    final StanfordTaggerModel model =
                        (StanfordTaggerModel)elem.createExecutableExtension(EXT_POINT_ATTR_DF);
                    final String type = elem.getAttribute(EXT_POINT_ATTR_TYPE);
                    registerStanfordTaggerModel(model, type);
                } catch (final Throwable t) {
                    LOGGER
                        .error("Problems during initialization of stanford tagger model (with id '" + operator + "'.)");
                    if (decl != null) {
                        LOGGER.error("Extension " + decl + "ignored.", t);
                    }
                }

            }
        } catch (final Exception e) {
            LOGGER.error("Exception while registering StanfordTaggerModel extensions");
        }
    }

    /**
     * @return Returns the map containing the name of the Stanford POS tagger models and the related
     *         StanfordTaggerModels.
     */
    public Map<String, StanfordTaggerModel> getPosTaggerModelMap() {
        return ImmutableMap.copyOf(m_posTaggerMap);
    }

    /**
     * @return Returns the map containing the name of the Stanford NER tagger models and the related
     *         StanfordTaggerModels.
     */
    public Map<String, StanfordTaggerModel> getNerTaggerModelMap() {
        return ImmutableMap.copyOf(m_nerTaggerMap);
    }

}

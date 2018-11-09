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
 *   Nov 2, 2018 (julian): created
 */
package org.knime.ext.textprocessing.data;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.PortObjectSpec;

import opennlp.tools.namefind.TokenNameFinderModel;

/**
 * The {@link PortObjectSpec} for the {@link OpenNlpNerTaggerModelPortObject}. It contains several information about the
 * model present in the port object.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.7
 */
public final class OpenNlpNerTaggerModelPortObjectSpec extends AbstractSimplePortObjectSpec {

    /**
     * The (empty) serializer. Values will be saved and loaded via
     * {@link OpenNlpNerTaggerModelPortObjectSpec#load(ModelContentRO)} and
     * {@link OpenNlpNerTaggerModelPortObjectSpec#save(ModelContentWO)}.
     *
     * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
     */
    public static final class Serializer
        extends AbstractSimplePortObjectSpecSerializer<OpenNlpNerTaggerModelPortObjectSpec> {
        // Nothing to do here...
    }

    /** Key for the manifest-version of the model. */
    private static final String MANIFEST_VERSION_PROPERTY = "Manifest-Version";

    /** Key for the component name of the model. */
    private static final String COMPONENT_NAME_PROPERTY = "Component-Name";

    /** Key for the OpenNLP version number of the model. */
    private static final String VERSION_PROPERTY = "OpenNLP-Version";

    /** Key for the time stamp of the model. */
    private static final String TIMESTAMP_PROPERTY = "Timestamp";

    /** Key for the language the model was trained for. */
    private static final String LANGUAGE_PROPERTY = "Language";

    /** The manifest version of the model. */
    private String m_manifestVersion;

    /** The component name of the model. */
    private String m_componentName;

    /** The OpenNLP version number of the model. */
    private String m_version;

    /** The time stamp of the model. */
    private String m_timeStamp;

    /** The language the model was trained for. */
    private String m_language;

    /**
     * Parameterless constructor. This constructor should only be called if the model where the parameter come from has
     * not been loaded so far (e.g. in the configure method of a node).
     */
    public OpenNlpNerTaggerModelPortObjectSpec() {
        // Empty constructor...
    }

    /**
     * Creates a new instance of {@code OpenNlpNerTaggerModelPortObjectSpec}. It holds several information about the
     * specifications of the model that is present in the port object.
     *
     * @param model The model containing the specifications.
     */
    public OpenNlpNerTaggerModelPortObjectSpec(final TokenNameFinderModel model) {
        m_manifestVersion = model.getManifestProperty(MANIFEST_VERSION_PROPERTY);
        m_componentName = model.getManifestProperty(COMPONENT_NAME_PROPERTY);
        m_version = model.getManifestProperty(VERSION_PROPERTY);
        m_timeStamp = model.getManifestProperty(TIMESTAMP_PROPERTY);
        m_language = model.getManifestProperty(LANGUAGE_PROPERTY);
    }

    /** {@inheritDoc} */
    @Override
    public JComponent[] getViews() {
        StringBuilder htmlText = new StringBuilder();
        htmlText.append("<html>\n");
        htmlText.append("<head>\n");
        htmlText.append("<style type=\"text/css\">\n");
        htmlText.append("body {color:#333333;}");
        htmlText.append("table {width: 100%;margin: 7px 0 7px 0;}");
        htmlText.append("th {font-weight: bold;background-color: #aaccff;" + "vertical-align: bottom;}");
        htmlText.append("td {padding: 4px 10px 4px 10px;}");
        htmlText.append("th {padding: 4px 10px 4px 10px;}");
        htmlText.append(".left {text-align: left}");
        htmlText.append(".right {text-align: right}");
        htmlText.append(".numeric {text-align: right}");
        htmlText.append(".odd {background-color:#ddeeff;}");
        htmlText.append(".even {background-color:#ffffff;}");
        htmlText.append("</style>\n");
        htmlText.append("</head>\n");

        htmlText.append("<body><table>\n");
        htmlText.append("<tr><th class=\"left\">Parameter</th><th class=\"left\">Value</th></tr>\n");
        htmlText.append("<tr class=\"odd\"><td>" + MANIFEST_VERSION_PROPERTY + "</td><td>").append(m_manifestVersion)
            .append("</td></tr>\n");
        htmlText.append("<tr class=\"even\"><td>" + COMPONENT_NAME_PROPERTY + "</td><td>").append(m_componentName)
            .append("</td></tr>\n");
        htmlText.append("<tr class=\"odd\"><td>" + VERSION_PROPERTY + "</td><td>").append(m_version)
            .append("</td></tr>\n");
        htmlText.append("<tr class=\"even\"><td>" + TIMESTAMP_PROPERTY + "</td><td>").append(m_timeStamp)
            .append("</td></tr>\n");
        htmlText.append("<tr class=\"even\"><td>" + LANGUAGE_PROPERTY + "</td><td>").append(m_language)
            .append("</td></tr>\n");
        htmlText.append("</table></body></html>");
        JEditorPane tablePane = new JEditorPane("text/html", "");
        tablePane.setEditable(false);
        tablePane.setText(htmlText.toString());

        JComponent component = new JScrollPane(tablePane);
        component.setName("OpenNLP NE Model Spec");
        return new JComponent[]{component};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model) {
        model.addString(MANIFEST_VERSION_PROPERTY, m_manifestVersion);
        model.addString(COMPONENT_NAME_PROPERTY, m_componentName);
        model.addString(VERSION_PROPERTY, m_version);
        model.addString(TIMESTAMP_PROPERTY, m_timeStamp);
        model.addString(LANGUAGE_PROPERTY, m_language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        m_manifestVersion = model.getString(MANIFEST_VERSION_PROPERTY);
        m_componentName = model.getString(COMPONENT_NAME_PROPERTY);
        m_version = model.getString(VERSION_PROPERTY);
        m_timeStamp = model.getString(TIMESTAMP_PROPERTY);
        m_language = model.getString(LANGUAGE_PROPERTY);
    }

}

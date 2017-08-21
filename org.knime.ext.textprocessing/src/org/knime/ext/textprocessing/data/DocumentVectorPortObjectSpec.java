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
 *   21.08.2017 (Julian): created
 */
package org.knime.ext.textprocessing.data;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * The {@code DocumentVectorPortObjectSpec} is used to transfer vector creation specifications, as well as feature space
 * column names from Document Vector node to the Document Vector Adapter node.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
public class DocumentVectorPortObjectSpec extends AbstractSimplePortObjectSpec {

    private boolean m_ignoreTags;

    private boolean m_bitVector;

    private String m_vectorValue;

    private boolean m_asCollectionCell;

    private String[] m_featureSpaceColumns;

    /**
     * The (empty) serializer. Values will be saved and loaded via
     * {@link DocumentVectorPortObjectSpec#load(ModelContentRO)} and
     * {@link DocumentVectorPortObjectSpec#save(ModelContentWO)}
     *
     * @author Julian Bunzel, KNIME.com, Berlin, Germany
     */
    public final static class Serializer extends AbstractSimplePortObjectSpecSerializer<DocumentVectorPortObjectSpec> {
    }

    /**
     * Empty constructor. Needed for loading.
     */
    public DocumentVectorPortObjectSpec() {
    }

    /**
     * Creates a new instance of {@code DocumentVectorPortObjectSpec} that contains information about vector creation
     * of Document Vector node, as well as feature space column names.
     *
     * @param ignoreTags The boolean value of the ignore tags setting.
     * @param bitVector The boolean value of the bitvector setting.
     * @param vectorValue The name of the column containing feature vector values.
     * @param asCollectionCell The boolean value of the collection cell setting.
     * @param featureSpaceColumns The names of the feature space columns.
     */
    public DocumentVectorPortObjectSpec(final boolean ignoreTags, final boolean bitVector, final String vectorValue,
        final boolean asCollectionCell, final String[] featureSpaceColumns) {
        m_ignoreTags = ignoreTags;
        m_bitVector = bitVector;
        m_vectorValue = vectorValue;
        m_asCollectionCell = asCollectionCell;
        m_featureSpaceColumns = featureSpaceColumns;
    }

    /**
     * @return Returns the value of the ignore tags setting.
     */
    public boolean getIgnoreTagsSetting() {
        return m_ignoreTags;
    }

    /**
     * @return Returns the value of the bitvector setting.
     */
    public boolean getBitVectorSetting() {
        return m_bitVector;
    }

    /**
     * @return Returns the column name of the feature vector values.
     */
    public String getVectorValueColumnName() {
        return m_vectorValue;
    }

    /**
     * @return Returns the value of the collection cell setting.
     */
    public boolean getCollectionCellSetting() {
        return m_asCollectionCell;
    }

    /**
     * @return Returns the names of the feature space columns.
     */
    public String[] getFeatureSpaceColumns() {
        return m_featureSpaceColumns;
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
        htmlText.append("<tr class=\"odd\"><td>Ignore Tags</td><td>").append(m_ignoreTags).append("</td></tr>\n");
        htmlText.append("<tr class=\"even\"><td>BitVector</td><td>").append(m_bitVector).append("</td></tr>\n");
        htmlText.append("<tr class=\"odd\"><td>Vector Value</td><td>").append(m_vectorValue).append("</td></tr>\n");
        htmlText.append("<tr class=\"even\"><td>As Collection Cell</td><td>").append(m_asCollectionCell)
            .append("</td></tr>\n");
        htmlText.append("<tr class=\"odd\"><td>Feature Space Columns</td><td>")
            .append(String.join(" | ", m_featureSpaceColumns)).append("</td></tr>\n");
        htmlText.append("</table></body></html>");
        JEditorPane tablePane = new JEditorPane("text/html", "");
        tablePane.setEditable(false);
        tablePane.setText(htmlText.toString());

        JComponent component = new JScrollPane(tablePane);
        component.setName("Vector Hashing");
        return new JComponent[]{component};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model) {
        model.addBoolean("ignoreTags", getIgnoreTagsSetting());
        model.addBoolean("bitVector", getBitVectorSetting());
        model.addString("vectorValue", getVectorValueColumnName());
        model.addBoolean("asCollectionCell", getCollectionCellSetting());
        model.addStringArray("featureSpaceColumns", getFeatureSpaceColumns());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        m_ignoreTags = model.getBoolean("ignoreTags");
        m_bitVector = model.getBoolean("bitVector");
        m_vectorValue = model.getString("vectorValue");
        m_asCollectionCell = model.getBoolean("asCollectionCell");
        m_featureSpaceColumns = model.getStringArray("featureSpaceColumns");
    }

}

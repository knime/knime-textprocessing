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
 *   08.07.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public class NERModelPortObjectSpec extends AbstractSimplePortObjectSpec {

    private String m_tokenizerName;

    /**
     * Not used.
     */
    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<NERModelPortObjectSpec> {
        /**
         * {@inheritDoc}
         * @since 3.3
         */
        @Override
        public NERModelPortObjectSpec loadPortObjectSpec(final PortObjectSpecZipInputStream in) throws IOException {
            in.getNextEntry();
            ModelContentRO config = ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
            String nameOfUsedTokenizer;
            try {
                nameOfUsedTokenizer = config.getString("tokenizerName");
            } catch (InvalidSettingsException e) {
                throw new IOException("Failed to deserialize port object spec", e);
            }
            return new  NERModelPortObjectSpec(nameOfUsedTokenizer);
        }

        /**
         * {@inheritDoc}
         * @since 3.3
         */
        @Override
        public void savePortObjectSpec(final NERModelPortObjectSpec portObject, final PortObjectSpecZipOutputStream out)
            throws IOException {
            String XML_CONFIG_NAME = "content.xml";
            out.putNextEntry(new ZipEntry(XML_CONFIG_NAME));
            ModelContent spec = new ModelContent(XML_CONFIG_NAME);
            spec.addString("tokenizerName", portObject.getTokenizerName());
            spec.saveToXML(new NonClosableOutputStream.Zip(out));
        }
    }

    /**
     * Creates a new instance of {NERModelPortObjectSpec}
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public NERModelPortObjectSpec(final String tokenizerName) {
        m_tokenizerName = tokenizerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model) {
        //Nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        //Nothing to do here
    }

    /**
     * @return Returns the name of the used word tokenizer.
     * @since 3.3
     */
    public String getTokenizerName() {
        return m_tokenizerName;
    }

    /**
     * @param spec The data table spec to validate.
     * @return {@code true} if given spec is compatible to the model port spec, otherwise {@code false}.
     */
    public boolean validateSpec(final PortObjectSpec spec) {
        if (spec == null) {
            return false;
        }
        return true;
    }

}

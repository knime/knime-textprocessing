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
 *   Oct 11, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import opennlp.tools.namefind.TokenNameFinderModel;

/**
 * This port object is a specific implementation of {@code TaggerModelPortObject} used for OpenNLP NER models.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.7
 */
public final class OpenNlpNerTaggerModelPortObject extends TaggerModelPortObject<TokenNameFinderModel> {

    /**
     * The {@link TokenNameFinderModel}.
     */
    private final TokenNameFinderModel m_model;

    /**
     * Define port type of objects of this class when used as PortObjects.
     */
    @SuppressWarnings("hiding")
    public static final PortType TYPE =
        PortTypeRegistry.getInstance().getPortType(OpenNlpNerTaggerModelPortObject.class);

    /**
     * The serializer used to save/load the port object.
     *
     * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<OpenNlpNerTaggerModelPortObject> {

        /** The name of the file to write the model to. */
        private static final String MODEL_FILE_NAME = "outputmodel.bin";

        /**
         * {@inheritDoc}
         */
        @Override
        public OpenNlpNerTaggerModelPortObject loadPortObject(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            in.getNextEntry();
            final TokenNameFinderModel model = new TokenNameFinderModel(in);
            return new OpenNlpNerTaggerModelPortObject(model);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final OpenNlpNerTaggerModelPortObject portObject,
            final PortObjectZipOutputStream out, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
            out.putNextEntry(new ZipEntry(MODEL_FILE_NAME));
            portObject.getModel().serialize(out);
            out.close();
        }
    }

    /**
     * Creates a new instance of {@code OpenNlpNerTaggerModelPortObject} given a
     *
     * @param model The {@code TokenNameFinderModel}.
     */
    public OpenNlpNerTaggerModelPortObject(final TokenNameFinderModel model) {
        super(new OpenNlpNerTaggerModelPortObjectSpec(model));
        m_model = model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return "This is an OpenNlpNerTaggerModelPortObject";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenNameFinderModel getModel() {
        return m_model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws InvalidSettingsException, CanceledExecutionException {
        // Nothing to do here...
    }
}

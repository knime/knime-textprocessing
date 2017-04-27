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
 *   25.04.2017 (Julian): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class VectorHashingPortObject extends AbstractSimplePortObject {

    private final int m_dim;

    private final int m_seed;

    private final String m_hashFunc;

    private final String m_vectVal;

    private final PortObjectSpec m_spec;

    /**
     *
     * @author Julian Bunzel, KNIME.com, Berlin, Germany
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<VectorHashingPortObject> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final VectorHashingPortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            String XML_CONFIG_NAME = "config.xml";
            out.putNextEntry(new ZipEntry(XML_CONFIG_NAME));
            ModelContent config = new ModelContent(XML_CONFIG_NAME);
            config.addInt("dimension", portObject.getDimension());
            config.addInt("seed", portObject.getSeed());
            config.addString("hashFunc", portObject.getHashFunc());
            config.addString("vectorVal", portObject.getVectVal());
            config.saveToXML(new NonClosableOutputStream.Zip(out));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public VectorHashingPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            in.getNextEntry();
            ModelContentRO config = ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
            int dim;
            int seed;
            String hashFunc;
            String vectVal;

            try {
                dim = config.getInt("dimension");
                seed = config.getInt("seed");
                hashFunc = config.getString("hashFunc");
                vectVal = config.getString("vectorVal");
            } catch (InvalidSettingsException e) {
                throw new IOException("Failed to deserialize port object", e);
            }
            return new VectorHashingPortObject(dim, seed, hashFunc, vectVal);
        }
    }

    /**
     * @param dim
     * @param seed
     * @param hashFunc
     * @param vectVal
     */
    public VectorHashingPortObject(final int dim, final int seed, final String hashFunc, final String vectVal) {
        m_dim = dim;
        m_seed = seed;
        m_hashFunc = hashFunc;
        m_vectVal = vectVal;
        m_spec = new VectorHashingPortObjectSpec(dim, seed, hashFunc, vectVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return "This VectorHashingPortObject contains dimension, seed and hashfunction used to create document vectors.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * @return Returns the dimension of the document vector.
     */
    public int getDimension() {
        return m_dim;
    }

    /**
     * @return Returns the seed.
     */
    public int getSeed() {
        return m_seed;
    }

    /**
     * @return Returns the hashing function.
     */
    public String getHashFunc() {
        return m_hashFunc;
    }

    /**
     * @return Returns the value type that fills the vector.
     */
    public String getVectVal() {
        return m_vectVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        //Nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws InvalidSettingsException, CanceledExecutionException {
        //Nothing to do here
    }

}

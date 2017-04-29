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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * The {@code VectorHashingPortObjectSpec} is used to transfer vector creation specifications from one Document vector
 * hashing node to another.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class VectorHashingPortObjectSpec extends AbstractSimplePortObjectSpec {

    private int m_dim;

    private int m_seed;

    private String m_hashFunc;

    private String m_vectVal;

    /**
     * The (empty) serializer. Values will be saved and loaded via
     * {@link VectorHashingPortObjectSpec#load(ModelContentRO)} and
     * {@link VectorHashingPortObjectSpec#save(ModelContentWO)}
     *
     * @author Julian Bunzel, KNIME.com, Berlin, Germany
     */
    public final static class Serializer extends AbstractSimplePortObjectSpecSerializer<VectorHashingPortObjectSpec> {
    }

    /**
     * Empty constructor. Needed for loading.
     */
    public VectorHashingPortObjectSpec() {
    }

    /**
     * Creates a new instance of {@code VectorHashingPortObjectSpec} that contains information about vector creation of
     * the Document vector hashing node.
     *
     * @param dim The dimension of the vector
     * @param seed The seed.
     * @param hashFunc The hashing function
     * @param vectVal The type of value that is stored in the vector (binary, tf-rel, tf-abs).
     */
    public VectorHashingPortObjectSpec(final int dim, final int seed, final String hashFunc, final String vectVal) {
        m_dim = dim;
        m_seed = seed;
        m_hashFunc = hashFunc;
        m_vectVal = vectVal;
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
    protected void save(final ModelContentWO model) {
        model.addInt("dimension", getDimension());
        model.addInt("seed", getSeed());
        model.addString("hashFunc", getHashFunc());
        model.addString("vectorVal", getVectVal());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        m_dim = model.getInt("dimension");
        m_seed = model.getInt("seed");
        m_hashFunc = model.getString("hashFunc");
        m_vectVal = model.getString("vectorVal");
    }

}

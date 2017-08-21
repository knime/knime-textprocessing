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

import java.io.IOException;
import java.util.Objects;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.util.CheckUtils;

/**
 * The {@code DocumentVectorPortObject} is used to transfer vector creation specifications, as well as name of feature
 * space column names from Document Vector node to the Document Vector Adapter node.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
public class DocumentVectorPortObject implements PortObject {

    /** Serializer as required by extension point. */
    public static final class DocumentVectorPortObjectSerializer
        extends PortObjectSerializer<DocumentVectorPortObject> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final DocumentVectorPortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DocumentVectorPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            CheckUtils.checkArgument(spec instanceof DocumentVectorPortObjectSpec, "Spec not instance of '%s' but '%s'",
                DocumentVectorPortObjectSpec.class.getSimpleName(),
                spec == null ? "<null>" : spec.getClass().getSimpleName());
            return new DocumentVectorPortObject((DocumentVectorPortObjectSpec)spec);
        }
    }

    private final DocumentVectorPortObjectSpec m_spec;

    /**
     * New port object based on the non-null spec.
     *
     * @param spec The {@code DocumentVectorPortObject}
     */
    public DocumentVectorPortObject(final DocumentVectorPortObjectSpec spec) {
        m_spec = CheckUtils.checkArgumentNotNull(spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return "Document Vector";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentVectorPortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        return new JComponent[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VectorHashingPortObject)) {
            return false;
        }
        return Objects.equals(m_spec, ((DocumentVectorPortObject)obj).m_spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_spec.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getSummary();
    }

}

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
 *   04.10.2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;

/**
 * Abstract class that extends the generic superclass while extensions of this class use the
 * {@link SentencePreprocessing} interface.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.7
 */
public abstract class StreamableSentencePreprocessingNodeModel
    extends GenericStreamablePreprocessingNodeModel<SentencePreprocessing> {

    /**
     * Default constructor, defining one data input and one data output port.
     */
    public StreamableSentencePreprocessingNodeModel() {
        this(1, new InputPortRole[]{});
    }

    /**
     * Constructor defining a specified number of data input and one data output port.
     *
     * @param dataInPorts The number of data input ports.
     * @param roles The roles of the input ports after the first port.
     */
    public StreamableSentencePreprocessingNodeModel(final int dataInPorts, final InputPortRole[] roles) {
        super(dataInPorts, roles);
    }

    /**
     * Constructor defining a node model with different input and output port types.
     *
     * @param inPortTypes The input port types. First port type has to be BufferedDataTable, since its role is set to
     *            {@link InputPortRole#DISTRIBUTED_STREAMABLE}.
     * @param outPortTypes The output port types.
     * @param roles The roles of the input ports after the first port.
     */
    public StreamableSentencePreprocessingNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes,
        final InputPortRole[] roles) {
        super(inPortTypes, outPortTypes, roles);
    }
}

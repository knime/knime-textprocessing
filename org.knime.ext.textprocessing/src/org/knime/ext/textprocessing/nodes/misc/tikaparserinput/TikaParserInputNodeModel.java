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
 *   26.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikaparserinput;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.uri.URIDataCell;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.nodes.source.parser.tika.AbstractTikaNodeModel;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaParserConfig;

/**
 * The node model of the Tika Parser URL Input node. This model extends
 * {@link org.knime.ext.textprocessing.nodes.source.parser.tika} and is streamable but not distributable.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
final class TikaParserInputNodeModel extends AbstractTikaNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParserInputNodeModel.class);

    private final SettingsModelString m_colModel = TikaParserConfig.getColModel();

    /**
     * Creates a new instance of {@code TikaParserInputNodeModel}
     */
    TikaParserInputNodeModel() {
        super(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec in = inSpecs[0];
        int colIndex = -1;
        final String colName = m_colModel.getStringValue();
        if (colName == null) {
            // auto-guessing parameters
            for (int i = 0; i < in.getNumColumns(); i++) {
                DataType dtype = in.getColumnSpec(i).getType();
                if (dtype.isCompatible(StringValue.class) || dtype.isCompatible(URIDataValue.class)) {
                    colIndex = i;
                    LOGGER.info("Guessing column \"" + in.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
            CheckUtils.checkSetting(colIndex >= 0, "No string/URI compatible column in input");
            m_colModel.setStringValue(in.getColumnSpec(colIndex).getName());
        } else {
            // we have user setting -- expect the column to be present and of appropriate type
            colIndex = in.findColumnIndex(colName);
            // column must be present, otherwise fail
            CheckUtils.checkSetting(colIndex >= 0, "No such URI/String column in input: \"%s\"", colName);
            DataType type = in.getColumnSpec(colIndex).getType();
            // column must be URI or string compatible, otherwise fails
            CheckUtils.checkSetting(type.isCompatible(StringValue.class) || type.isCompatible(URIDataCell.class),
                "Column \"%s\" is present in the input table but not String/URI compatible, its type is \"%s\"",
                colName, type.toPrettyString());
        }

        assert colIndex >= 0 : "colindex expected to be non-negative at this point";

        return createDataTableSpec();
    }

    /** {@inheritDoc} */
    @Override
    public InputPortRole[] getInputPortRoles() {
        InputPortRole[] in = new InputPortRole[getNrInPorts()];
        Arrays.fill(in, InputPortRole.NONDISTRIBUTED_STREAMABLE);
        return in;
    }

    /** {@inheritDoc} */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        OutputPortRole[] out = new OutputPortRole[getNrOutPorts()];
        Arrays.fill(out, OutputPortRole.DISTRIBUTED);
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInputSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateInputSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedInputSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidSettingsException
     * @throws InterruptedException
     * @throws MalformedURLException
     * @throws InvalidPathException
     */
    @Override
    protected Iterable<URL> readInput(final RowInput input) throws InvalidSettingsException, InterruptedException, InvalidPathException, MalformedURLException {
        List<URL> files = new ArrayList<URL>();
        int colIndex = input.getDataTableSpec().findColumnIndex(m_colModel.getStringValue());
        CheckUtils.checkSetting(colIndex >= 0, "no such column \"%s\"", m_colModel.getStringValue());
        DataRow row;
        while ((row = input.poll()) != null) {
            String url;
            DataCell cell = row.getCell(colIndex);
            if (cell.isMissing()) {
                files.add(null);
                continue;
            }
            if (cell instanceof URIDataValue) {
                url = ((URIDataValue)cell).getURIContent().getURI().toString();
            } else {
                url = ((StringValue)cell).getStringValue();
            }
            files.add(FileUtil.toURL(url));
        }
        return files;
    }

}

/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringNodeModel extends NodeModel {

    /**
     * Set with all tag types.
     */
    public static final Set<String> ALL_TAG_TYPES =
        TagFactory.getInstance().getTagTypes();

    /**
     * The default tag type.
     */
    public static final String DEFAULT_TAG_TYPE =
        PartOfSpeechTag.getDefault().getType();

    /**
     * The missing cell vaue.
     */
    public static final String MISSING_CELL_VALUE = "<MissingCell>";

    /**
     * The default missing cell value.
     */
    public static final String DEFAULT_MISSING_VALUE = MISSING_CELL_VALUE;

    private int m_termColIndex = -1;

    private SettingsModelStringArray m_tagTypesModel =
        TagToStringNodeDialog.getTagTypesModel();

    private SettingsModelString m_termColModel =
        TagToStringNodeDialog.getTermColModel();

    private SettingsModelString m_missingTagValueModel =
        TagToStringNodeDialog.getMissingTagModel();

    /**
     * Creates new instance of <code>TagToStringNodeModel</code>.
     */
    public TagToStringNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[0]);
        verifier.verifyMinimumTermCells(1, true);

        m_termColIndex = inSpecs[0].findColumnIndex(
                m_termColModel.getStringValue());
        if (m_termColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified term column is not valid! "
                    + "Check your settings!");
        }

        List<String> tagTypes = new ArrayList<String>();
        for (String tagType : m_tagTypesModel.getStringArrayValue()) {
            tagTypes.add(tagType);
        }

        return new DataTableSpec[]{new DataTableSpec(inSpecs[0],
                                   new DataTableSpec(getDataTableSpec(
                                           tagTypes, inSpecs[0])))};
    }

    /**
     * Creates output <code>DataColumnSpec</code>s based on given tag types and
     * incoming <code>DataTableSpec</code>.
     * @param tagTypes tag types to consider.
     * @param oldSpec The incoming <code>DataTableSpec</code>
     * @return Output <code>DataColumnSpec</code>s.
     */
    static DataColumnSpec[] getDataTableSpec(final List<String> tagTypes,
            final DataTableSpec oldSpec) {
        DataColumnSpec[] dataColumnSpecs = new DataColumnSpec[tagTypes.size()];
        int i = 0;
        for (String tagType : tagTypes) {
            String name = DataTableSpec.getUniqueColumnName(oldSpec, tagType);
            dataColumnSpecs[i] = new DataColumnSpecCreator(
                    name, StringCell.TYPE).createSpec();
            i++;
        }
        return dataColumnSpecs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        m_termColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_termColModel.getStringValue());

        List<String> tagTypes = new ArrayList<String>();
        for (String tagType : m_tagTypesModel.getStringArrayValue()) {
            tagTypes.add(tagType);
        }
        CellFactory cellFac = new TagToStringCellFactory(m_termColIndex, tagTypes,
                    inData[0].getDataTableSpec(),
                    m_missingTagValueModel.getStringValue());
        ColumnRearranger rearranger = new ColumnRearranger(
                inData[0].getDataTableSpec());
        rearranger.append(cellFac);

        return new BufferedDataTable[]{exec.createColumnRearrangeTable(
                inData[0], rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_tagTypesModel.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
        m_missingTagValueModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tagTypesModel.validateSettings(settings);
        m_termColModel.validateSettings(settings);
        m_missingTagValueModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tagTypesModel.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
        m_missingTagValueModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // nothing to do ...
    }
}

/*
 * ------------------------------------------------------------------------
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
 *   15.11.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud.outport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.base.node.util.DefaultDataArray;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NodeViewExport;
import org.knime.core.node.NodeViewExport.ExportType;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.view.tagcloud.outport.font.SettingsModelFont;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The NodeModel of the tag cloud node.
 *
 * @author Iris Adae, University of Konstanz
 */

public class TagCloudNodeModel extends NodeModel {

    /** stores the input data table. */
    private DataTable m_data;

    /** stores a copy of the  data, needed to show the tagcloud. */
    private TagCloud m_tagcloud;

    private SettingsModelString m_termColModel = TagCloudNodeDialog.getTermColumnModel();

    private SettingsModelString m_valueColModel = TagCloudNodeDialog.getValueModel();

    private SettingsModelString m_calcTCTypeModel = TagCloudNodeDialog.getTypeofTCcalculationModel();

    private SettingsModelBoolean m_ignoretags = TagCloudNodeDialog.getBooleanModel();

    private SettingsModelIntegerBounded m_noOfRows = TagCloudNodeDialog.getNoofRowsModel();

    private SettingsModelBoolean m_allRows = TagCloudNodeDialog.getUseallrowsBooleanModel();

    private SettingsModelIntegerBounded m_widthModel = TagCloudNodeDialog.getWidthModel();

    private SettingsModelIntegerBounded m_heightModel = TagCloudNodeDialog.getHeightModel();

    private SettingsModelString m_imagetypeModel = TagCloudNodeDialog.getImageTypeModel();

    private SettingsModelColor m_backgroundColorModel = TagCloudNodeDialog.getBackgroundColorModel();

    private SettingsModelBoolean m_antialiasingModel = TagCloudNodeDialog.getAntiAliasingModel();

    private SettingsModelIntegerBounded m_alphaModel = TagCloudNodeDialog.getAlphaModel();

    private SettingsModelIntegerBounded m_boldModel = TagCloudNodeDialog.getBoldModel();

    private SettingsModelFont m_fontModel = TagCloudNodeDialog.getFontModel();

    /** The selected ID of the Column containing the value. */
    private int m_valueColIndex;

    /** The selected ID of the Column containing the term . */
    private int m_termColIndex;

    /** The name of the configuration file.  */
    private static final String DATA_FILE_NAME = "tagcloudpoints.data";

    /** The configuration key for the internal model of the Tagcloud. */
    public static final String INTERNAL_MODEL = "TagCloudNodel.data";

    /** The default width of the panel.*/
    public static final int DEFAULT_WIDTH = 800;

    /** The default height of the panel.*/
    public static final int DEFAULT_HEIGHT = 600;

    /** The default antia aliasing setting of the panel. */
    public static final boolean DEFAULT_ANTIALIASING = true;

    /** The default back ground color of the tag cloud. */
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;

    /** The default font of the tag cloud.*/
    public static final Font DEFAULT_FONT = Font.decode(Font.SANS_SERIF);


    /**
     * Initializes NodeModel.
     */
     TagCloudNodeModel() {
         super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {ImagePortObject.TYPE});
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        checkDataTableSpec(inSpec);

        final String imgType = m_imagetypeModel.getStringValue();
        ImagePortObjectSpec outSpec;
        if (imgType.toUpperCase().startsWith("SVG")) {
            outSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        } else {
            outSpec = new ImagePortObjectSpec(PNGImageContent.TYPE);
        }
        return new PortObjectSpec[] {outSpec};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumTermCells(1, true);
        verifier.verifyMinimumNumberCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_termColModel, spec, TermValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));
        ColumnSelectionVerifier.verifyColumn(m_valueColModel, spec, DoubleValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        m_termColIndex = spec.findColumnIndex(m_termColModel.getStringValue());
        m_valueColIndex = spec.findColumnIndex(m_valueColModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        BufferedDataTable dataTable = (BufferedDataTable)inData[0];
        long numofRows = dataTable.size();
        if (!m_allRows.getBooleanValue()) {
            numofRows = Math.min(m_noOfRows.getIntValue(), numofRows);

        }

        TagCloudImageExportPanel panel;
        if (numofRows <= 0) {
            m_tagcloud = null;
            setWarningMessage("Empty data table, nothing to display.");
            panel = new TagCloudImageExportPanel(null);
        } else {
       		m_data = new DefaultDataArray(dataTable, 1, (int)numofRows, exec);
        	checkDataTableSpec(dataTable.getDataTableSpec());
        	m_termColIndex = dataTable.getDataTableSpec().findColumnIndex(m_termColModel.getStringValue());
        	m_valueColIndex = dataTable.getDataTableSpec().findColumnIndex(m_valueColModel.getStringValue());

            try {
                m_tagcloud = new TagCloud();
                m_tagcloud.createTagCloud(exec, this);
                m_tagcloud.changealpha(m_alphaModel.getIntValue());
                m_tagcloud.changebold(m_boldModel.getIntValue());
                m_tagcloud.changeFontsizes(m_tagcloud.getminFontsize(),
                        m_tagcloud.getmaxFontsize(), getFont().getName(),
                        m_tagcloud.getCalcType(), m_boldModel.getIntValue());
                m_tagcloud.changeWidth(m_widthModel.getIntValue());
                exec.setProgress(1, "TagCloud completed");

                TagCloudViewPlotter plotter = new TagCloudViewPlotter();
                plotter.setTagCloudModel(m_tagcloud);
                plotter.updatePaintModel();
                plotter.fitToSize(getWindowDimension());

                panel = new TagCloudImageExportPanel(m_tagcloud);
                panel.setAntialiasing(m_antialiasingModel.getBooleanValue());
                panel.setOpaque(true);
                panel.setBackground(m_backgroundColorModel.getColorValue());
                // don't know exactly why bonds have to be set, but they need
                // to be set in order to get the background drawn properly.
                // If bonds are not set background settings will be ignored even
                // if opaque is set.
                panel.setBounds(0, 0, m_tagcloud.getPreferredSize().width, m_tagcloud.getPreferredSize().height);
            } catch (IllegalStateException e) {
                setWarningMessage("Empty data table, nothing to display.");
                m_tagcloud = null;
                panel = new TagCloudImageExportPanel(null);
            }
        }

        final String imgType = m_imagetypeModel.getStringValue();
        final ExportType exportType = NodeViewExport.getViewExportMap().get(imgType);
        if (exportType == null) {
            throw new InvalidSettingsException("Invalid image type:" + imgType);
        }
        final File file = FileUtil.createTempFile("image", "." + exportType.getFileSuffix());
        exec.setMessage("Creating image file...");

        exportType.export(file, panel, panel.getPreferredSize().width, panel.getPreferredSize().height);

        final InputStream is = new FileInputStream(file);
        ImagePortObjectSpec outSpec;
        final ImageContent image;
        if (imgType.toUpperCase().startsWith("SVG")) {
            outSpec = new ImagePortObjectSpec(SvgCell.TYPE);
            image = new SvgImageContent(is);
        } else {
            outSpec = new ImagePortObjectSpec(PNGImageContent.TYPE);
            image = new PNGImageContent(is);
        }
        is.close();
        file.delete();
        final PortObject po = new ImagePortObject(image, outSpec);
        return new PortObject[]{po};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.loadSettingsFrom(settings);
        m_calcTCTypeModel.loadSettingsFrom(settings);
        m_ignoretags.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
        m_allRows.loadSettingsFrom(settings);
        m_noOfRows.loadSettingsFrom(settings);
        m_widthModel.loadSettingsFrom(settings);
        m_heightModel.loadSettingsFrom(settings);
        m_imagetypeModel.loadSettingsFrom(settings);
        m_antialiasingModel.loadSettingsFrom(settings);
        m_backgroundColorModel.loadSettingsFrom(settings);
        m_alphaModel.loadSettingsFrom(settings);
        m_boldModel.loadSettingsFrom(settings);
        m_fontModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_tagcloud = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // AP-14023 only save internals if tagcloud not null
        if (m_tagcloud != null) {
            // Save tagcloud content
            ModelContent modelContent = new ModelContent(INTERNAL_MODEL);
            m_tagcloud.saveTo(modelContent);

            File file = new File(nodeInternDir, DATA_FILE_NAME);
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                modelContent.saveToXML(fos);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        File file = new File(nodeInternDir, DATA_FILE_NAME);
        // AP-14023 only load internals if the file has been written
        if (file.exists()) {
            try (final FileInputStream fis = new FileInputStream(file)) {
                ModelContentRO modelContent = ModelContent.loadFromXML(fis);
                m_tagcloud = new TagCloud();
                m_tagcloud.loadFrom(modelContent);
            } catch (InvalidSettingsException e1) {
                IOException ioe = new IOException("Could not load settings, due to invalid settings in model content!");
                ioe.initCause(e1);
                throw ioe;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_valueColModel.saveSettingsTo(settings);
        m_calcTCTypeModel.saveSettingsTo(settings);
        m_ignoretags.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
        m_noOfRows.saveSettingsTo(settings);
        m_allRows.saveSettingsTo(settings);
        m_widthModel.saveSettingsTo(settings);
        m_heightModel.saveSettingsTo(settings);
        m_imagetypeModel.saveSettingsTo(settings);
        m_antialiasingModel.saveSettingsTo(settings);
        m_backgroundColorModel.saveSettingsTo(settings);
        m_alphaModel.saveSettingsTo(settings);
        m_boldModel.saveSettingsTo(settings);
        m_fontModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.validateSettings(settings);
        m_calcTCTypeModel.validateSettings(settings);
        m_ignoretags.validateSettings(settings);
        m_termColModel.validateSettings(settings);
        m_noOfRows.validateSettings(settings);
        m_allRows.validateSettings(settings);
        m_widthModel.validateSettings(settings);
        m_heightModel.validateSettings(settings);
        m_imagetypeModel.validateSettings(settings);
        m_antialiasingModel.validateSettings(settings);
        m_backgroundColorModel.validateSettings(settings);
        m_alphaModel.validateSettings(settings);
        m_boldModel.validateSettings(settings);
        m_fontModel.validateSettings(settings);
    }


    /**
     * @return the kind of calculation for the TagCloud
     */
    public String getTCcalcType() {
        return m_calcTCTypeModel.getStringValue();
    }

    /**
     * @return the selected column ID of the value column.
     */
    public int getValueCol() {
        return m_valueColIndex;
    }

    /**
     * @return the input data table
     */
    public DataTable getData() {
        if (m_data != null) {
            return m_data;
        }
        return null;
    }

    /**
     * @return the chosen Column id Containing the Term
     */
    public int getTermCol() {
        return m_termColIndex;
    }


    /**
     * @return the pre calculated TagCloud data
     */
    public TagCloud getTagCloud() {
        return m_tagcloud;
    }

    /**
     * @return true if tags should be ignored
     */
    public boolean ignoreTags() {
        return m_ignoretags.getBooleanValue();
    }

    /**
     * @return the preferred dimensions of the window which is the layout
     * dimension plus an offset
     */
    public Dimension getWindowDimension() {
        return new Dimension(m_widthModel.getIntValue(), m_heightModel.getIntValue());
    }

    /**
     * @return the value for using the anti aliasing.
     */
    public boolean useAntialiasing() {
        return m_antialiasingModel.getBooleanValue();
    }

    /**
     * @return the color of the background.
     */
    public Color getBackgroundColor() {
        return m_backgroundColorModel.getColorValue();
    }

    /**
     * @return the selected font size.
     */
    public Font getFont() {
        return m_fontModel.getFont();
    }
}

/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.io.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorFileStorePortObject;
import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;

/**
 * Node to write word vector models.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorModelWriterNodeModel extends AbstractDLNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WordVectorModelWriterNodeModel.class);

    private SettingsModelString m_outfile;

    private SettingsModelBoolean m_overwrite;

    /**
     * Constructor for class WordVectorModelWriterNodeModel.
     */
    protected WordVectorModelWriterNodeModel() {
        super(new PortType[]{WordVectorFileStorePortObject.TYPE}, new PortType[]{});
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_outfile = WordVectorModelWriterNodeDialog.createFileModel();
        m_overwrite = WordVectorModelWriterNodeDialog.createOverwriteOKModel();

        final List<SettingsModel> settings = new ArrayList<>();
        settings.add(m_outfile);
        settings.add(m_overwrite);

        return settings;
    }

    @Override
    protected WordVectorFileStorePortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        final WordVectorFileStorePortObject port = (WordVectorFileStorePortObject)inData[0];

        final URL url = FileUtil.toURL(m_outfile.getStringValue());
        final File file = FileUtil.getFileFromURL(url);

        if (file == null) {
            throw new IllegalArgumentException("WordVectorModels can only by written to local files.");
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file))) {
            WordVectorPortObjectUtils.writeFileStorePortObject(port, zipOut);
        } catch (IOException e) {
            LOGGER.error("Error writing word vector model!", e);
            throw e;
        }

        return new WordVectorFileStorePortObject[]{};
    }

    @Override
    protected DLModelPortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final String warning =
            CheckUtils.checkDestinationFile(m_outfile.getStringValue(), m_overwrite.getBooleanValue());
        if (warning != null) {
            LOGGER.warn(warning);
        }
        return new DLModelPortObjectSpec[]{};
    }
}

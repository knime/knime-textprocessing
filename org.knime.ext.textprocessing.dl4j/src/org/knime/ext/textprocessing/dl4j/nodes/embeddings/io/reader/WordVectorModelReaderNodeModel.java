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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorFileStorePortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;

/**
 * Node to read word vector models in different formats: </br>
 * 1. KNIME - models saved by the corresponding word vector model writer </br>
 * 2. CSV - common CSV format, each row contains the word in the first column and the vector in the following columns.
 * The columns should be separated by whitespace. </br>
 * 3. Binary - e.g. Google news vectors
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorModelReaderNodeModel extends AbstractDLNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WordVectorModelReaderNodeModel.class);

    private SettingsModelString m_inFile;

    private WordVectorPortObjectSpec m_outSpec;

    private boolean m_isDl4jFormat;

    /**
     * Constructor for class WordVectorModelReaderNodeModel.
     */
    protected WordVectorModelReaderNodeModel() {
        super(new PortType[]{}, new PortType[]{WordVectorFileStorePortObject.TYPE});
    }

    @Override
    protected WordVectorPortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final String warning = CheckUtils.checkSourceFile(m_inFile.getStringValue());
        if (warning != null) {
            LOGGER.warn(warning);
        }

        //if spec can't be loaded we assume that the selected format is non-KNIME
        try {
            m_outSpec = loadSpec();
            m_isDl4jFormat = true;
        } catch (final Exception e) {
            LOGGER.debug(e);
            LOGGER.warn(
                "Selected word vector model file seems not to be in KNIME format. CSV or Binary Word2Vector format is expected.");
            m_isDl4jFormat = false;
            //currently (0.8.0), WordVectorSerializer only supports Word2Vec for external models. Hence, only weights will be loaded.
            //See WordVectorSerializer.readWord2VecModel()
            m_outSpec = new WordVectorPortObjectSpec(WordVectorTrainingMode.WORD2VEC);
        }

        return new WordVectorPortObjectSpec[]{m_outSpec};
    }

    @Override
    protected WordVectorFileStorePortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
        throws Exception {
        final URL url = FileUtil.toURL(m_inFile.getStringValue());
        final File file = FileUtil.getFileFromURL(url);
        WordVectors wv = null;

        if (m_isDl4jFormat) {
            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file))) {
                wv = WordVectorPortObjectUtils.loadWordVectors(zipIn, m_outSpec.getWordVectorTrainingsMode());
            } catch (Exception e) {
                LOGGER.error("Error loading word vector model!", e);
                throw e;
            }
        } else {
            wv = WordVectorSerializer.readWord2VecModel(file);
        }

        return new WordVectorFileStorePortObject[]{WordVectorFileStorePortObject.create(wv, m_outSpec,
            exec.createFileStore(UUID.randomUUID().toString() + ""))};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_inFile = WordVectorModelReaderNodeDialog.createFileModel();

        final List<SettingsModel> settings = new ArrayList<>();
        settings.add(m_inFile);

        return settings;
    }

    /**
     * Helper method to load only spec.
     *
     * @return the loaded spec
     * @throws InvalidPathException
     * @throws IOException
     * @throws Exception
     */
    private WordVectorPortObjectSpec loadSpec() throws InvalidPathException, IOException {
        final URL url = FileUtil.toURL(m_inFile.getStringValue());
        final File file = FileUtil.getFileFromURL(url);

        WordVectorPortObjectSpec spec = null;

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file))) {
            spec = WordVectorPortObjectUtils.loadSpecFromZip(zipIn);
        } catch (IOException e) {
            LOGGER.debug("Error loading word vector model specification!", e);
            throw e;
        }

        return spec;
    }

}
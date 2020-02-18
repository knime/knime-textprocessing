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
 *   Feb 18, 2020 (Perla Gjoka, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.io.filehandling.wordVector.reader;

import java.io.File;
import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.knime.core.data.util.CancellableReportingInputStream;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorFileStorePortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;
import org.knime.filehandling.core.node.portobject.reader.PortObjectFromFileReaderNodeModel;
import org.knime.filehandling.core.node.portobject.reader.PortObjectReaderNodeConfig;

/**
 * This class creates the node model for the Word Vector Model Reader node.
 *
 * @author Perla Gjoka, KNIME GmbH, Konstanz, Germany
 */
final class WordVectorModelReader2NodeModel extends PortObjectFromFileReaderNodeModel<PortObjectReaderNodeConfig> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WordVectorModelReader2NodeModel.class);

    /**
     * Constructor.
     *
     * @param creationConfig the node creation configuration.
     * @param config the reader configuration.
     */
    protected WordVectorModelReader2NodeModel(final NodeCreationConfiguration creationConfig,
        final PortObjectReaderNodeConfig config) {
        super(creationConfig, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] read(final InputStream inputStream, final ExecutionContext exec) throws Exception {
        final File tmp = WordVectorPortObjectUtils.copyInputStreamToTmpFile(inputStream, false);
        final Path path = tmp.toPath();
        WordVectors wordVectors = null;
        WordVectorTrainingMode trainingMode =
            WordVectorPortObjectUtils.readTrainingMode(path, exec.createSubExecutionContext(0.5));
        final boolean isKnimeFormat = trainingMode != null;
        final String knimeFormat;
        if (isKnimeFormat) {
            knimeFormat = "knime";
        } else {
            knimeFormat = "external";
            trainingMode = WordVectorTrainingMode.WORD2VEC;
        }
        final WordVectorPortObjectSpec outputSpec = new WordVectorPortObjectSpec(trainingMode);
        try {
            exec.setMessage(String.format("Reading model in %s format", knimeFormat));
            if (isKnimeFormat) {
                try (final ZipInputStream zipIn = new ZipInputStream(
                    new CancellableReportingInputStream(Files.newInputStream(path), exec.createSubProgress(0.5)))) {
                    wordVectors = WordVectorPortObjectUtils.loadWordVectors(zipIn, trainingMode);
                }
            } else {
                wordVectors = WordVectorPortObjectUtils.loadWordVectorsExternalFormat(tmp);
            }
        } catch (ClosedByInterruptException e) {
            LOGGER.error(String.format("IO Error loading model in %s format from specified location!", knimeFormat), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(String.format("Error loading word vector model in %s format", knimeFormat), e);
            throw new RuntimeException("DL4J error message: " + e.getMessage(), e);
        } finally {
            WordVectorPortObjectUtils.deleteTmpFile(path, "The temporary copy of the model could not be deleted");
        }
        return new PortObject[]{WordVectorFileStorePortObject.create(wordVectors, outputSpec,
            exec.createFileStore(UUID.randomUUID().toString()))};
    }
}

/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
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
 * KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JComponent;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.ext.dl4j.base.cache.MemoryAlertAwareGuavaCache;
import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;

/**
 * Port Object for Word Vector Models using File Store and model caching.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public final class WordVectorFileStorePortObject extends FileStorePortObject {

    /**
     * Serializer for class WordVectorFileStorePortObject.
     *
     * @author David Kolb, KNIME.com GmbH
     */
    public static final class Serializer extends PortObjectSerializer<WordVectorFileStorePortObject> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final WordVectorFileStorePortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            portObject.save(out, exec);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public WordVectorFileStorePortObject loadPortObject(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            WordVectorFileStorePortObject portObject = new WordVectorFileStorePortObject();
            portObject.load(in, spec, exec);
            return portObject;
        }
    }

    /**
     * PortType of this FileStorePortObject.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(WordVectorFileStorePortObject.class);

    /**
     * Cache to store word vector models.
     */
    private final static MemoryAlertAwareGuavaCache CACHE = MemoryAlertAwareGuavaCache.getInstance();

    private static final String SUMMARY = "Word Vector Model";

    private static final String CFG_MODELKEY = "model_key";

    private WordVectorPortObjectSpec m_spec;

    private UUID m_modelKey;

    /**
     * Factory method to create a WordVectorFileStorePortObject. This will serialize the contained WordVectors model.
     *
     * @param wordVectors
     * @param spec
     * @param fileStore
     * @return a new WordVectorFileStorePortObject object containing the specified model and spec
     */
    public static WordVectorFileStorePortObject create(final WordVectors wordVectors,
        final WordVectorPortObjectSpec spec, final FileStore fileStore) {
        WordVectorFileStorePortObject obj = new WordVectorFileStorePortObject(wordVectors, spec, fileStore);
        // FIXME (later): We could wait with serialization until requested by KNIME.
        // Also it would be of benefit for very  large models to _not_ be stored within KNIME,
        // rather we could keep a reference to the original model and load the model as needed from this reference.
        // Users should be able to select behavior in reader.
        serialize(wordVectors, fileStore);
        return obj;
    }

    /**
     * Constructor for class WordVectorFileStorePortObject specifying the WordVectors model, the port object spec and
     * the FileStore.
     *
     * @param wordVectors
     * @param spec
     * @param fileStore
     */
    public WordVectorFileStorePortObject(final WordVectors wordVectors, final WordVectorPortObjectSpec spec,
        final FileStore fileStore) {
        super(Collections.singletonList(fileStore));
        m_spec = spec;
        m_modelKey = UUID.randomUUID();
        CACHE.put(m_modelKey, wordVectors);
    }

    /** Framework constructor, not to be used by node code. */
    public WordVectorFileStorePortObject() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return SUMMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WordVectorPortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * Get the WordVectors model. This may trigger deserialization.
     *
     * @return a WordVectorsModel
     */
    public synchronized WordVectors getWordVectors() {
        try {
           return CACHE.get(m_modelKey, new Callable<WordVectors>() {
                @Override
                public WordVectors call() {
                    return deserialize();
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error retrieving word vector model from cache!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        return null;
    }

    /**
     * Save the cache key for this model.
     *
     * @param out
     * @param exec
     * @throws IOException
     */
    private void save(final PortObjectZipOutputStream out, final ExecutionMonitor exec) throws IOException {
        ModelContent mc = new ModelContent("");
        mc.addString(CFG_MODELKEY, m_modelKey.toString());
        mc.saveToXML(out);
    }

    /**
     * Restore the cache key for this model.
     *
     * @param in
     * @param spec
     * @param exec
     * @throws IOException
     */
    private void load(final PortObjectZipInputStream in, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws IOException {
        m_spec = (WordVectorPortObjectSpec)spec;
        ModelContentRO mc = ModelContent.loadFromXML(in);
        try {
            m_modelKey = UUID.fromString(mc.getString(CFG_MODELKEY));
        } catch (InvalidSettingsException e) {
            throw new IOException("Unable to restore meta information: " + e.getMessage(), e);
        }
    }

    private WordVectors deserialize() {
        final File file = getFileStore(0).getFile();
        try (ZipInputStream zIn = new ZipInputStream(new FileInputStream(file))) {
            return WordVectorPortObjectUtils.loadWordVectors(zIn, m_spec.getWordVectorTrainingsMode());
        } catch (IOException e) {
            throw new IllegalStateException("Error loading word vector model!", e);
        }
    }

    private static void serialize(final WordVectors model, final FileStore fileStore) {
        File file = fileStore.getFile();
        try (ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(file))) {
            WordVectorPortObjectUtils.writeWordVectors(model, zOut);
        } catch (IOException e) {
            throw new IllegalStateException("Error writing word vector model!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        CACHE.remove(m_modelKey);
    }
}
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
 *   01.08.2016 (Julian): created
 */
package org.knime.ext.textprocessing.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
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

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This port object is a specific implementation of {@code NERModelPortObject} used for models trained by the
 * StanfordNLP NE Learner node.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public class StanfordNERModelPortObject extends NERModelPortObject<CRFClassifier<CoreLabel>> {

    /**
     * Define port type of objects of this class when used as PortObjects.
     */
    @SuppressWarnings("hiding")
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(StanfordNERModelPortObject.class);

    /**
     * The serializer used to save/load the port object.
     *
     * @author Julian Bunzel, KNIME.com, Berlin, Germany
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<StanfordNERModelPortObject> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final StanfordNERModelPortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            final String MODEL_FILE_NAME = "outputmodel" + portObject.getFileExtension();
            final String XML_CONFIG_NAME = "config.xml";
            final String DICT_FILE_NAME = "dict.bin";

            out.putNextEntry(new ZipEntry(XML_CONFIG_NAME));
            final ModelContent config = new ModelContent(XML_CONFIG_NAME);
            config.addString("tagValue", portObject.getTagValue());
            config.addString("tagType", portObject.getTagType());
            config.addString("tokenizerName", portObject.getTokenizerName());
            config.saveToXML(new NonClosableOutputStream.Zip(out));
            out.putNextEntry(new ZipEntry(MODEL_FILE_NAME));
            out.write(portObject.getByteArray());
            out.putNextEntry(new ZipEntry(DICT_FILE_NAME));
            try {
                out.write(portObject.getDictAsByteArray());
            } catch (final IOException e) {
                throw new IOException("Could not convert dictionary to byte array", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public StanfordNERModelPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            // get xml entry
            in.getNextEntry();
            final ModelContentRO config;
            final InputStream is = new NonClosableInputStream.Zip(in);
            config = ModelContent.loadFromXML(is);

            // get model byte array
            in.getNextEntry();
            final byte[] outputModelByteArray = IOUtils.toByteArray(in);

            // get dict set
            in.getNextEntry();
            final byte[] dictByteArray = IOUtils.toByteArray(in);
            final String dict = new String(dictByteArray);
            final BufferedReader stringReader = new BufferedReader(new StringReader(dict));
            String line = null;
            final Set<String> dictSet = new LinkedHashSet<>();
            while ((line = stringReader.readLine()) != null) {
                dictSet.add(line);
            }

            // get settings
            try {
                final String usedTagValue = config.getString("tagValue");
                final String usedTagType = config.getString("tagType");
                final String nameOfUsedTokenizer = config.containsKey("tokenizerName")
                    ? config.getString("tokenizerName") : "OpenNLP English WordTokenizer";
                final Tag usedTag = new Tag(usedTagValue, usedTagType);
                return new StanfordNERModelPortObject(outputModelByteArray, usedTag, dictSet, nameOfUsedTokenizer);
            } catch (final InvalidSettingsException e) {
                throw new IOException("Failed to deserialize port object", e);
            } catch (final Exception e) {
                throw new IOException("Could not create NLPModelPortObject");
            }
        }
    }

    /**
     * Creates an instance of {@code StanfordNERModelPortObject}.
     *
     * @param outputBuffer The byte array containing the NER model.
     * @param tag The used tag.
     * @param dict The used dictionary.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public StanfordNERModelPortObject(final byte[] outputBuffer, final Tag tag, final Set<String> dict,
        final String tokenizerName) {
        super(outputBuffer, tag, dict, tokenizerName);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException If the model file cannot be created or if there are problems accessing the input stream.
     * @throws ClassNotFoundException If there are problems interpreting the serialized data.
     * @throws ClassCastException If there are problems interpreting the serialized data.
     */
    @Override
    public CRFClassifier<CoreLabel> getNERModel() throws IOException, ClassNotFoundException {
        File file;
        file = getModelFile();
        final CRFClassifier<CoreLabel> crf = CRFClassifier.getClassifier(file);
        Files.delete(file.toPath());
        return crf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileExtension() {
        return ".crf.ser.gz";
    }
}

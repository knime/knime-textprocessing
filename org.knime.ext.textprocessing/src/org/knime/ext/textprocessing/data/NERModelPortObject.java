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
 *   01.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.PortObjectSpec;

import com.google.common.io.Files;

/**
 * This is a {@code TaggerModelPortObject} for models trained by any named entity model learner node. It keeps specific
 * information that was used to train the model like the tag type, tag value, the named entity dictionary and the
 * tokenizer used to tokenize the training data.
 *
 * An example is the {@code StandfordNERModelPortObject}.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @param <T> T is a specific implementation of the model class that is serialized through this port object.
 * @since 3.3
 */
public abstract class NERModelPortObject<T> extends TaggerModelPortObject<T> {

    /** The tag used to train the model. */
    private final Tag m_tag;

    /** The tag type used to train the model. */
    private final String m_tagType;

    /** The tag value used to train the model. */
    private final String m_tagValue;

    /** The dictionary used to train the model. */
    private final Set<String> m_dict;

    /** The name of the tokenizer used to train the model. */
    private final String m_tokenizerName;

    /** The byte array holding the model. */
    private final byte[] m_byteArray;

    /**
     * Creates a new instance of {@code NERModelPortObject}.
     *
     * @param outputBuffer The byte array which will be used to write the binary file in the {@code Serializer}.
     * @param tag The tag that has been used to build the model.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public NERModelPortObject(final byte[] outputBuffer, final Tag tag, final String tokenizerName) {
        super(new NERModelPortObjectSpec(tokenizerName));
        m_byteArray = outputBuffer.clone();
        m_tag = tag;
        m_tagType = tag.getTagType();
        m_tagValue = tag.getTagValue();
        m_dict = null;
        m_tokenizerName = tokenizerName;
    }

    /**
     * Creates a new instance of {@code NERModelPortObject}.
     *
     * @param outputBuffer The byte array which will be used to write the binary file in the {@code Serializer}.
     * @param tag The tag that has been used to build the model.
     * @param dict The dictionary, a set of Strings used for validation in the StanfordNLP tagger.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public NERModelPortObject(final byte[] outputBuffer, final Tag tag, final Set<String> dict,
        final String tokenizerName) {
        super(new NERModelPortObjectSpec(tokenizerName));
        m_byteArray = outputBuffer.clone();
        m_tag = tag;
        m_tagType = tag.getTagType();
        m_tagValue = tag.getTagValue();
        m_dict = new HashSet<>(dict);
        m_tokenizerName = tokenizerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getModel() throws Exception {
        return getNERModel();
    }

    /**
     * @return Returns the specific NER model.
     * @throws Exception Thrown if named-entity model could not be initialized.
     */
    public abstract T getNERModel() throws Exception;

    /**
     * Creates and returns a {@link File} based on the byte array stored as a member of this class.
     *
     * @return Returns the model file.
     * @throws IOException Thrown if the model file could not be written.
     * @deprecated Get model directly by using {@link #getNERModel()} instead of using {@link File Files}.
     */
    @Deprecated
    public File getModelFile() throws IOException {
        final String tempDir = KNIMEConstants.getKNIMETempDir();
        final String outputModel = tempDir + "/outputmodel_" + UUID.randomUUID().toString() + getFileExtension();
        final File file = new File(outputModel);
        Files.write(getByteArray(), file);
        return file;
    }

    /**
     * Method to return the file extension of the model to load/read. The defined extension must contain a preceding
     * dot.
     *
     * @return Returns the file extension of the model to load/read. The defined extension must contain a preceding dot.
     */
    public abstract String getFileExtension();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return "This is an NERModelPortObject";
    }

    /**
     * Returns the tag.
     *
     * @return Returns the name of the tag used to train the model.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the tag value.
     *
     * @return Returns the tag value (e.g. PERSON).
     */
    public String getTagValue() {
        return m_tagValue;
    }

    /**
     * Returns the tag type.
     *
     * @return Returns the tag type (e.g. NE).
     */
    public String getTagType() {
        return m_tagType;
    }

    /**
     * Returns the byte array containing the model.
     *
     * @return Returns the byte array containing the model.
     */
    public byte[] getByteArray() {
        return m_byteArray.clone();
    }

    /**
     * Returns the dictionary as a String set.
     *
     * @return Returns the dictionary as a String set.
     */
    public Set<String> getDictSet() {
        return Collections.unmodifiableSet(m_dict);
    }

    /**
     * Returns the dictionary as a byte array to serialize it.
     *
     * @return Converts the dictionary as a byte array to serialize it.
     * @throws IOException Thrown if the dictionary could not be written as a byte array.
     */
    public byte[] getDictAsByteArray() throws IOException {
        final StringBuilder dict = new StringBuilder();
        final String dictTempDir = KNIMEConstants.getKNIMETempDir() + "/tempDict" + UUID.randomUUID() + ".txt";
        final File dictFile = new File(dictTempDir);
        for (final String entity : m_dict) {
            dict.append(entity + "\n");
        }
        try (final PrintWriter dictFileWriter = new PrintWriter(dictFile, "UTF-8")) {
            dictFileWriter.println(dict.toString());
        }
        final byte[] dictAsBytes = Files.toByteArray(dictFile);
        dictFile.delete();
        return dictAsBytes;
    }

    /**
     * Returns the name of the tokenizer used for word tokenization
     *
     * @return Returns the name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public String getTokenizerName() {
        return m_tokenizerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        // Is not used, because the Serializer saves and passes all data
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws InvalidSettingsException, CanceledExecutionException {
        // Is not used, because the Serializer loads all data
    }

}

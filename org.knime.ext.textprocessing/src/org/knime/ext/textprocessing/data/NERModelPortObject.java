/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   01.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;

import com.google.common.io.Files;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @param <T>
 * @since 3.3
 */
public abstract class NERModelPortObject<T> extends AbstractSimplePortObject {
    private PortObjectSpec m_spec;

    private final byte[] m_outputByteArray;

    private final Tag m_tag;

    private final String m_tagType;

    private final String m_tagValue;

    private final Set<String> m_dict;

    private final String m_tokenizerName;

    /**
     * @param outputBuffer The byte array which will be used to write the binary file in the {@code Serializer}.
     * @param tag The tag that has been used to build the model.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public NERModelPortObject(final byte[] outputBuffer, final Tag tag, final String tokenizerName) {
        m_outputByteArray = outputBuffer;
        m_tag = tag;
        m_tagType = tag.getTagType() ;
        m_tagValue = tag.getTagValue() ;
        m_dict = null;
        m_tokenizerName = tokenizerName;
        m_spec = new NERModelPortObjectSpec(tokenizerName);
    }

    /**
     * @param outputBuffer The byte array which will be used to write the binary file in the {@code Serializer}.
     * @param tag The tag that has been used to build the model.
     * @param dict The dictionary, a set of Strings used for validation in the StanfordNLP tagger.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public NERModelPortObject(final byte[] outputBuffer, final Tag tag, final Set<String> dict, final String tokenizerName) {
        m_outputByteArray = outputBuffer;
        m_tag = tag;
        m_tagType = tag.getTagType() ;
        m_tagValue = tag.getTagValue() ;
        m_dict = dict;
        m_tokenizerName = tokenizerName;
        m_spec = new NERModelPortObjectSpec(tokenizerName);
    }

    /**
     * @return Returns the specific NER model.
     * @throws IOException If there are problems accessing the input stream.
     * @throws ClassNotFoundException If there are problems interpreting the serialized data.
     * @throws ClassCastException If there are problems interpreting the serialized data.
     */
    public abstract T getNERModel() throws IOException, ClassCastException, ClassNotFoundException;

    /**
     * @return Returns the specific file extension.
     */
    public abstract String getFileExtension();

    /**
     * @return Returns the model file.
     * @throws IOException If the file could not be created.
     */
    public File getModelFile() throws IOException {
        String tempDir = KNIMEConstants.getKNIMETempDir();
        String outputModel = tempDir + "/outputmodel_" + UUID.randomUUID().toString() + getFileExtension();
        File file = new File(outputModel);
        Files.write(getByteArray(), file);
        return file;
    }

    /**
     * @return Returns a summary of the PortObject.
     */
    @Override
    public String getSummary() {
        return "This is an NERModelPortObject";
    }

    /**
     * @return Returns the name of the used tag model.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * @return Returns the tag value (e.g. PERSON).
     */
    public String getTagValue() {
        return m_tagValue;
    }

    /**
     * @return Returns the tag type (e.g. NE).
     */
    public String getTagType() {
        return m_tagType;
    }

    /**
     * @return Returns the byte array.
     */
    public byte[] getByteArray() {
        return m_outputByteArray;
    }

    /**
     * @return Returns the dictionary as a String set.
     */
    public Set<String> getDictSet() {
        return m_dict;
    }

    /**
     * @return Converts the dictionary as a byte array to serialize it.
     * @throws UnsupportedEncodingException If the specified character encoding for the PrintWriter is not supported.
     * @throws FileNotFoundException If the PrintWriter could not find the specified file.
     * @throws IOException If the file could not be written to a byte array.
     */
    public byte[] getDictAsByteArray() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        StringBuilder dict = new StringBuilder();
        String dictTempDir = KNIMEConstants.getKNIMETempDir() + "/tempDict" + UUID.randomUUID() + ".txt";
        File dictFile = new File(dictTempDir);
        for (String entity : m_dict) {
            dict.append(entity + "\n");
        }
        PrintWriter dictFileWriter = new PrintWriter(dictFile, "UTF-8");
        dictFileWriter.println(dict.toString());
        dictFileWriter.close();
        byte[] dictAsBytes = Files.toByteArray(dictFile);
        dictFile.delete();
        return dictAsBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * @return Returns the name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public String getTokenizerName() {
        return m_tokenizerName;
    }

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

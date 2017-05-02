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
package org.knime.ext.textprocessing.dl4j.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;

/**
 * Utility class for {@link WordVectorPortObject} and {@link WordVectorPortObjectSpec} Serialization. Also contains
 * utility methods to load/save members of port and spec class.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorPortObjectUtils {

    private WordVectorPortObjectUtils() {
        // Utility class
    }

    /**
     * Serializes a {@link WordVectorPortObject} to zip using the specified {@link ZipOutputStream}. It can be specified
     * if both PortObject and Spec should be written or only one of them.
     *
     * @param portObject the {@link WordVectorPortObject} to write
     * @param writePortObject whether to write PortObject
     * @param writeSpec whether to write Spec
     * @param outStream stream to write to
     * @throws IOException
     */
    @Deprecated
    public static void saveModelToZip(final WordVectorPortObject portObject, final boolean writePortObject,
        final boolean writeSpec, final ZipOutputStream outStream) throws IOException {
        final WordVectorPortObjectSpec spec = (WordVectorPortObjectSpec)portObject.getSpec();

        if (outStream == null) {
            throw new IOException("OutputStream is null");
        }
        if (writePortObject && !writeSpec) {
            savePortObjectOnly(portObject, outStream);
        }
        if (!writePortObject && writeSpec) {
            saveSpecOnly(spec, outStream);
        }
        if (writePortObject && writeSpec) {
            savePortObjectAndSpec(portObject, spec, outStream);
        }
    }

    /**
     * Reads {@link WordVectorPortObjectSpec} from specified {@link ZipInputStream}.
     *
     * @param inStream stream to read from
     * @return WordVectorPortObjectSpec
     * @throws IOException
     */
    public static WordVectorPortObjectSpec loadSpecFromZip(final ZipInputStream inStream) throws IOException {
        return new WordVectorPortObjectSpec(loadTrainingsMode(inStream));
    }

    /**
     * Reads {@link WordVectorPortObject} from specified {@link ZipInputStream}.
     *
     * @param inStream stream to read from
     * @param mode the type of WordVector model to expect
     * @return WordVectorPortObject
     * @throws IOException
     */
    @Deprecated
    public static WordVectorPortObject loadPortFromZip(final ZipInputStream inStream, final WordVectorTrainingMode mode)
        throws IOException {
        return new WordVectorPortObject(loadWordVectors(inStream, mode), null);
    }

    /**
     * Reads {@link WordVectors} from specified {@link ZipInputStream}.
     *
     * @param in stream to read from
     * @param mode the type of WordVector model to expect
     * @return {@link WordVectors} loaded from stream
     * @throws IOException
     */
    public static WordVectors loadWordVectors(final ZipInputStream in, final WordVectorTrainingMode mode)
        throws IOException {
        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (entry.getName().matches("word_vectors")) {
                switch (mode) {
                    case DOC2VEC:
                        return WordVectorSerializer.readParagraphVectors(in);
                    case WORD2VEC:
                        /* Need to copy stream to temp file because API does not support Word2VecModel reading
                         * with InputStreams. */
                        File tmpFile = inputStreamToTmpFile(in, "w2v_model");
                        return WordVectorSerializer.readWord2VecModel(tmpFile);
                    default:
                        break;
                }
            }
        }
        throw new IllegalStateException("No deserialization method defined for WordVectors of type: " + mode);
    }

    /**
     * Reads {@link WordVectorTrainingMode} from specified {@link ZipInputStream}.
     *
     * @param in stream to read from
     * @return {@link WordVectorTrainingMode} loaded from stream
     * @throws IOException
     */
    public static WordVectorTrainingMode loadTrainingsMode(final ZipInputStream in) throws IOException {
        ZipEntry entry;
        WordVectorTrainingMode mode = null;

        while ((entry = in.getNextEntry()) != null) {
            if (entry.getName().matches("word_vector_trainings_mode")) {
                final String read = readStringFromZipStream(in);
                mode = WordVectorTrainingMode.valueOf(read);
            }
        }
        return mode;
    }

    /**
     * Writes port object without spec. Calling this will close the stream.
     *
     * @param portObject
     * @param out
     * @throws IOException
     */
    @Deprecated
    private static void savePortObjectOnly(final WordVectorPortObject portObject, final ZipOutputStream out)
        throws IOException {
        writeWordVectors(portObject.getWordVectors(), out);
    }

    /**
     * Writes port spec without object.
     *
     * @param spec
     * @param out
     * @throws IOException
     */
    private static void saveSpecOnly(final WordVectorPortObjectSpec spec, final ZipOutputStream out)
        throws IOException {
        final WordVectorTrainingMode mode = spec.getWordVectorTrainingsMode();

        writeWordVectorTrainingsMode(mode, out);
    }

    /**
     * Writes port object and spec.
     *
     * @param portObject
     * @param spec
     * @param out
     * @throws IOException
     */
    @Deprecated
    private static void savePortObjectAndSpec(final WordVectorPortObject portObject,
        final WordVectorPortObjectSpec spec, final ZipOutputStream out) throws IOException {
        savePortObjectOnly(portObject, out);
        saveSpecOnly(spec, out);
    }

    /**
     * Writes {@link WordVectorTrainingMode} to specified {@link ZipInputStream} .
     *
     * @param mode the {@link WordVectorTrainingMode} to write
     * @param out stream to write to
     * @throws IOException
     */
    public static void writeWordVectorTrainingsMode(final WordVectorTrainingMode mode, final ZipOutputStream out)
        throws IOException {
        final ZipEntry entry = new ZipEntry("word_vector_trainings_mode");
        out.putNextEntry(entry);
        out.write(mode.toString().getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Writes {@link WordVectors} to specified {@link ZipInputStream}.
     *
     * @param wordVectors {@link WordVectors} to write
     * @param out stream to write to
     * @throws IOException
     */
    public static void writeWordVectors(final WordVectors wordVectors, final ZipOutputStream out) throws IOException {
        final ZipEntry entry = new ZipEntry("word_vectors");
        out.putNextEntry(entry);
        //first check if ParagraphVectors because it extends Word2Vec
        if (wordVectors instanceof ParagraphVectors) {
            ParagraphVectors d2v = (ParagraphVectors)wordVectors;
            WordVectorSerializer.writeParagraphVectors(d2v, out);
        } else if (wordVectors instanceof Word2Vec) {
            Word2Vec w2v = (Word2Vec)wordVectors;
            WordVectorSerializer.writeWord2VecModel(w2v, out);
        } else {
            throw new IllegalStateException(
                "No serialization method defined for WordVectors of type: " + wordVectors.getClass().getSimpleName());
        }
    }

    private static String readStringFromZipStream(final ZipInputStream in) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final byte[] byteBuffer = new byte[1024];
        int currentRead = 0;

        while ((currentRead = in.read(byteBuffer, 0, 1024)) >= 0) {
            stringBuilder.append(new String(byteBuffer, 0, currentRead));
        }

        return stringBuilder.toString();
    }

    /**
     * Copies the content of the specified InputStream to a temp file using the specified file name as prefix. The file
     * name must be at least three characters long. The temp file will be deleted when the virtual machine terminates.
     *
     * @param is stream to copy
     * @param tmpFileName name to use for the temp file, must be at least three characters long
     * @return file containing stream content
     * @throws IOException
     */
    private static File inputStreamToTmpFile(final InputStream is, final String tmpFileName) throws IOException {
        File tmpFile = File.createTempFile(tmpFileName, null);
        tmpFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(is, tmpFile);
        return tmpFile;
    }

    /**
     * Converts wordVectors to {@link Word2Vec}. Sets {@link WeightLookupTable} and {@link VocabCache}.
     * Depending on specified word vector type this may lead to information loss. E.g. labels for
     * {@link ParagraphVectors}.
     *
     * @param wordVectors
     * @return Word2Vec containing vocab and lookup table
     */
    public static Word2Vec wordVectorsToWord2Vec(final WordVectors wordVectors) {
        final Word2Vec w2v = new Word2Vec();
        w2v.setLookupTable(wordVectors.lookupTable());
        w2v.setVocab(wordVectors.vocab());
        return w2v;
    }

}

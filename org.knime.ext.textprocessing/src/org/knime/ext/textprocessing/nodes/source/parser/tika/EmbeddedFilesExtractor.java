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
 *   19.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public final class EmbeddedFilesExtractor {

    private Parser m_parser;

    private Detector m_detector;

    private TikaConfig m_config;

    private String m_filename;

    private Metadata m_metadata;

    private ParseContext m_context;

    private ContentHandler m_handler;

    private Map<String, String> m_outputFiles;

    private boolean m_hasError;

    private Map<String, Integer> m_dupFiles;

    private boolean m_extractInlineImages;

    /**
     * Constructor for EmbeddedFilesExtractor class
     */
    public EmbeddedFilesExtractor() {
        m_parser = new AutoDetectParser();
        m_detector = ((AutoDetectParser)m_parser).getDetector();
        m_config = TikaConfig.getDefaultConfig();
        m_metadata = new Metadata();
        m_handler = new BodyContentHandler(-1);
        m_outputFiles = new LinkedHashMap<String, String>();
        m_hasError = false;
        m_context = new ParseContext();
        m_extractInlineImages = false;
    }

    private void extract(final InputStream is, final Path outputDir) throws SAXException, TikaException, IOException {
        m_metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, m_filename);
        if (m_extractInlineImages) {
            PDFParserConfig pdfConfig = m_context.get(PDFParserConfig.class);
            pdfConfig.setExtractInlineImages(m_extractInlineImages);
            m_context.set(PDFParserConfig.class, pdfConfig);
        }
        CustomEmbeddedDocumentExtractor ex = new CustomEmbeddedDocumentExtractor(outputDir, m_context);
        m_context.set(EmbeddedDocumentExtractor.class, ex);

        m_parser.parse(is, m_handler, m_metadata, m_context);
        m_outputFiles = ex.getOutputFiles();
        m_hasError = ex.hasError();
    }

    /**
     * Extract all embedded files within a file (if exists)
     *
     * @param is input stream
     * @param outputDir directory to write the extracted files to
     * @param fname the name of the file
     * @throws SAXException
     * @throws TikaException
     * @throws IOException
     */
    public void extract(final InputStream is, final Path outputDir, final String fname)
        throws SAXException, TikaException, IOException {
        m_filename = fname.split("\\.")[0];
        extract(is, outputDir);
    }

    /**
     * Set parse context for the extractor
     *
     * @param pcontext the parse context to be used
     */
    public void setContext(final ParseContext pcontext) {
        m_context = pcontext;
    }

    /**
     * Set the list that keeps track of duplicate files. This is needed to prevent overwrite for files with the same
     * name.
     *
     * @param duplicateFiles the hash map containing filenames and their corresponding file counter
     */
    public void setDuplicateFilesList(final Map<String, Integer> duplicateFiles) {
        m_dupFiles = duplicateFiles;
    }

    /**
     * Set whether to extract inline images for PDFs.
     *
     * @param extract the boolean value
     */
    public void setExtractInlineImages(final boolean extract) {
        m_extractInlineImages = extract;
    }

    /**
     * @return metadata of the container file
     */
    public Metadata getMetadata() {
        return m_metadata;
    }

    /**
     * @return content handler that contains the content of the container file
     */
    public ContentHandler getHandler() {
        return m_handler;
    }

    /**
     * @return a map of extracted files and their types (attachment or inline image)
     */
    public Map<String, String> getOutputFiles() {
        return m_outputFiles;
    }

    /**
     * @return boolean if the extract method throws an error
     */
    public boolean hasError() {
        return m_hasError;
    }

    private class CustomEmbeddedDocumentExtractor extends ParsingEmbeddedDocumentExtractor {
        private static final String UNKNOWN_EXT = ".bin";

        private static final String INLINE_IMG = "Inline Image";

        private static final String ATTACHMENT = "Attachment";

        private final Path outputDir;

        private int fileCount;

        private Parser autoParser;

        private boolean skippedContainer;

        private boolean error;

        private Map<String, String> output;

        private CustomEmbeddedDocumentExtractor(final Path outputDirectory, final ParseContext parseContext) {
            super(parseContext);
            outputDir = outputDirectory;
            fileCount = 0;
            autoParser = new AutoDetectParser();
            skippedContainer = false;
            error = false;
            output = new LinkedHashMap<String, String>();
        }

        @Override
        public boolean shouldParseEmbedded(final Metadata mdata) {
            return true;
        }

        @Override
        public void parseEmbedded(final InputStream stream, final ContentHandler contentHandler, final Metadata mdata,
            final boolean outputHtml) throws SAXException, IOException {

            TemporaryResources tmp = new TemporaryResources();
            TikaInputStream tis = TikaInputStream.get(stream, tmp);

            MediaType contentType = m_detector.detect(tis, mdata);

            byte[] byteArray = getBytes(tis, tis.available());
            TikaInputStream tisParse = TikaInputStream.get(new ByteArrayInputStream(byteArray), tmp);
            EmbeddedContentHandler embedH = new EmbeddedContentHandler(new BodyContentHandler(contentHandler));
            try {
                autoParser.parse(tisParse, embedH, mdata, new ParseContext());
            } catch (TikaException e1) {
                // no problem if it can't be parsed
            } finally {
                tisParse.close();
            }

            String name = mdata.get(TikaMetadataKeys.RESOURCE_NAME_KEY);

            if (name == null) {
                if (!skippedContainer) {
                    skippedContainer = true;
                    return;
                }
                name = m_filename + "_file_" + fileCount++;
            } else {
                String outputName = FilenameUtils.normalize(name);
                if (outputName == null) {
                    outputName = FilenameUtils.normalize(FilenameUtils.getName(name));
                }
                if (!outputName.contains(m_filename)) {
                    name = m_filename + "_" + outputName;
                }
            }

            if (name.indexOf('.') == -1 && contentType != null) {
                try {
                    name += m_config.getMimeRepository().forName(contentType.toString()).getExtension();
                    if (name.indexOf('.') == -1) {
                        name += UNKNOWN_EXT;
                    }
                } catch (MimeTypeException e) {
                    name += UNKNOWN_EXT;
                }
            } else if (name.indexOf('.') == -1 && contentType == null) {
                name += UNKNOWN_EXT;
            }

            File outputFile = new File(outputDir.toFile(), name);
            if (!m_dupFiles.containsKey(name)) {
                m_dupFiles.put(name, 0);
            } else {
                m_dupFiles.replace(name, m_dupFiles.get(name) + 1);
                outputFile = new File(outputDir.toFile(), FilenameUtils.getBaseName(name) + "_"
                    + String.valueOf(m_dupFiles.get(name)) + "." + FilenameUtils.getExtension(name));

            }

            // check if inline images or attachments
            // might need more refinement for the attachment part
            String type = mdata.get(TikaCoreProperties.EMBEDDED_RESOURCE_TYPE);
            if (type != null && type.equals(TikaCoreProperties.EmbeddedResourceType.INLINE.toString())) {
                type = INLINE_IMG;
            } else {
                type = ATTACHMENT;
            }

            output.put(outputFile.getAbsolutePath(), type);
            File parent = outputFile.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("Unable to create directory \"" + parent + "\"");
                }
            }

            TikaInputStream tisExtract = TikaInputStream.get(new ByteArrayInputStream(byteArray), tmp);

            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                if (tisExtract.getOpenContainer() != null && tisExtract.getOpenContainer() instanceof DirectoryEntry) {
                    POIFSFileSystem fs = new POIFSFileSystem();
                    copy((DirectoryEntry)tisExtract.getOpenContainer(), fs.getRoot());
                    fs.writeFilesystem(os);
                    fs.close();
                } else {
                    IOUtils.copy(tisExtract, os);
                }
            } catch (FileNotFoundException | SecurityException e) {
                error = true;
            } finally {
                tisExtract.close();
            }
        }

        private byte[] getBytes(final InputStream input, final int size) throws IOException {
            int read = 0;
            byte[] bytes = new byte[size];

            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            if (size != 0) {
                while ((read = input.read(bytes)) != -1) {
                    ostream.write(bytes, 0, read);
                }
            }
            byte[] res = ostream.toByteArray();
            ostream.close();
            return res;
        }

        public boolean hasError() {
            return error;
        }

        public Map<String, String> getOutputFiles() {
            return output;
        }

        private void copy(final DirectoryEntry sourceDir, final DirectoryEntry destDir) throws IOException {
            for (Entry entry : sourceDir) {
                if (entry instanceof DirectoryEntry) {
                    DirectoryEntry newDir = destDir.createDirectory(entry.getName());
                    copy((DirectoryEntry)entry, newDir);
                } else {
                    try (InputStream contents = new DocumentInputStream((DocumentEntry)entry)) {
                        destDir.createDocument(entry.getName(), contents);
                    }
                }
            }
        }

    }

}

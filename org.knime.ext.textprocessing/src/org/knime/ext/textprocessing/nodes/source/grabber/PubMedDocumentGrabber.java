/*
========================================================================
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
 *   18.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.FileCollector;
import org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class PubMedDocumentGrabber extends AbstractDocumentGrabber {

    /**
     * The source of the documents grabbed by this grabber.
     */
    public static final String SOURCE = "PubMed";

    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(PubMedDocumentGrabber.class);

    private static final String PROTOCOL = "https";

    private static final String HOST = "eutils.ncbi.nlm.nih.gov";


    private static final String SEARCH_PATH = "/entrez/eutils/esearch.fcgi";

    private static final String SEARCH_QUERY = "db=pubmed&term=";

    private static final String SEARCH_QUERY_POSTFIX = "&retmax=";


    private static final String FETCH_PATH = "/entrez/eutils/efetch.fcgi";

    private static final String FETCH_QUERY = "db=pubmed&id=";

    private static final String FETCH_QUERY_POSTFIX =
        "&retmode=xml&rettype=abstract";


    private static final String BASIC_FILE_NAME = "PubMedAbstracts";

    private static final String FILE_EXTENSION = "gz";

    private int m_stepSize = 100;

    private long m_delayMillis = 1000;

    private List<Integer> m_idList = new ArrayList<Integer>();

    // initialize the tokenizer with the old standard tokenizer for backwards compatibility
    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();


    /**
     * Creates empty instance of <code>PubMedDocumentGrabber</code>.
     * @deprecated Use {@link #PubMedDocumentGrabber(String)} to define the tokenizer used for word tokenization.
     */
    @Deprecated
    PubMedDocumentGrabber() {
        super();
    }

    /**
     * Creates empty instance of <code>PubMedDocumentGrabber</code>.
     * @since 3.3
     */
    PubMedDocumentGrabber(final String tokenizerName) {
        super();
        m_tokenizerName = tokenizerName;
    }


    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getName() {
        return "PUBMED";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numberOfResults(final Query query) throws Exception {
        URL pubmed = buildUrl(query, false);
        LOGGER.info("PubMed Query: " + pubmed.toString());
        return buildResultList(pubmed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List < Document > grabDocuments(final File directory,
            final Query query) throws Exception {
        if (directory != null && query != null) {
            if (directory.exists() && directory.isDirectory()) {

                fetchDocuments(directory, query);

                List<Document> docs = new ArrayList<Document>();
                try {
                    docs = parseDocuments(directory);
                } catch (URISyntaxException e) {
                    LOGGER.warn("Could not find file containing "
                            + "PubMed documents!");
                    throw(e);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse PubMed documents!");
                    throw(e);
                }

                return docs;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetchAndParseDocuments(final File directory, final Query query)
            throws Exception {
        if (directory != null && query != null) {
            if (directory.exists() && directory.isDirectory()) {

                fetchDocuments(directory, query);

                try {
                    parseDocumentsAndNotify(directory);
                } catch (URISyntaxException e) {
                    LOGGER.warn("Could not find file containing "
                            + "PubMed documents!");
                    throw(e);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse PubMed documents!");
                    throw(e);
                }
            }
        }
        return;
    }

    private void fetchDocuments(final File directory, final Query query)
    throws Exception {
        if (directory != null && query != null) {
            if (directory.exists() && directory.isDirectory()) {

                URL pubmed = buildUrl(query, true);
                LOGGER.info("PubMed Query: " + pubmed.toString());

                // Read search result xml
                buildResultList(pubmed);

                // go through all ids (with certain step size)
                // and download document
                int idStart = 0;
                int count = 1;
                while (idStart < m_idList.size()) {
                    checkCanceled();
                    int idEnd = getEnd(idStart, m_idList.size() - 1);

                    // setting progress
                    double progress = (double)idEnd  / (double)m_idList.size()
                                        * 0.5;
                    message("Fetching documents from " + idStart + " to "
                            + idEnd + " of " + m_idList.size(), progress);

                    String idString = "";
                    for (int i = idStart; i <= idEnd; i++) {
                        idString += m_idList.get(i) + ",";
                    }

                    String fetchStr = FETCH_QUERY + idString
                        + FETCH_QUERY_POSTFIX;
                    URI uri = new URI(PROTOCOL, HOST, FETCH_PATH, fetchStr, "");
                    pubmed = uri.toURL();

                    LOGGER.info("PubMed fetching: " + pubmed.toString());

                    String filename = BASIC_FILE_NAME + count + "."
                        + FILE_EXTENSION;
                    try {
                        saveDocument(pubmed, directory, filename);
                    } catch (IOException e) {
                        LOGGER.warn("Could not read PubMed Xml-Website!");
                        throw(e);
                    } catch (URISyntaxException e) {
                        LOGGER.warn("URL Syntax is not valid!");
                        throw(e);
                    }

                    Thread.sleep(m_delayMillis);
                    idStart = idEnd + 1;
                    count++;
                }
            }
        }
        return;
    }

    private void parseDocumentsAndNotify(final File dir) throws Exception {
        DocumentParser parser = new PubMedDocumentParser(getExtractMetaInfo(), m_tokenizerName);

        parser.addDocumentParsedListener(
                new InternalDocumentParsedEventListener());

        parser.setDocumentSource(new DocumentSource(SOURCE));
        if (getDocumentCategory() != null) {
            parser.setDocumentCategory(getDocumentCategory());
        }
        if (getDocumentType() != null) {
            parser.setDocumentType(getDocumentType());
        }

        List<String> validExtensions = new ArrayList<String>();
        validExtensions.add(FILE_EXTENSION);

        FileCollector fc = new FileCollector(dir, validExtensions, false, true);
        List<File> files = fc.getFiles();
        int fileCount = files.size();
        int currFile = 1;
        for (File f : files) {
            double progress = (double)currFile / (double)fileCount;
            setProgress(progress, "Parsing file " + currFile + " of "
                    + fileCount);
            checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + f.getAbsolutePath());

            InputStream is;
            if (f.getName().toLowerCase().endsWith(".gz")
                    || f.getName().toLowerCase().endsWith(".zip")) {
                is = new GZIPInputStream(new FileInputStream(f));
            } else {
                is = new FileInputStream(f);
            }
            parser.setDocumentFilepath(f.getAbsolutePath());
            parser.parseDocument(is);
        }

        if (getDeleteFiles()) {
            for (File file : files) {
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }
        return;
    }

    @Deprecated
    private List < Document > parseDocuments(final File dir) throws Exception {
        List<Document> docs = new ArrayList<Document>();

        DocumentParser parser = new PubMedDocumentParser(m_tokenizerName);
        parser.setDocumentSource(new DocumentSource(SOURCE));
        if (getDocumentCategory() != null) {
            parser.setDocumentCategory(getDocumentCategory());
        }
        if (getDocumentType() != null) {
            parser.setDocumentType(getDocumentType());
        }

        List<String> validExtensions = new ArrayList<String>();
        validExtensions.add(FILE_EXTENSION);

        FileCollector fc = new FileCollector(dir, validExtensions, false, true);
        List<File> files = fc.getFiles();
        int fileCount = files.size();
        int currFile = 1;
        for (File f : files) {
            double progress = (double)currFile / (double)fileCount;
            setProgress(progress, "Parsing file " + currFile + " of "
                    + fileCount);
            checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + f.getAbsolutePath());

            InputStream is;
            if (f.getName().toLowerCase().endsWith(".gz")
                    || f.getName().toLowerCase().endsWith(".zip")) {
                is = new GZIPInputStream(new FileInputStream(f));
            } else {
                is = new FileInputStream(f);
            }
            parser.setDocumentFilepath(f.getAbsolutePath());

            docs.addAll(parser.parse(is));
            parser.clean();
        }

        if (getDeleteFiles()) {
            for (File file : files) {
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }

        return docs;
    }


    private void saveDocument(final URL url, final File dir,
            final String filename) throws IOException, URISyntaxException {

        File dst = new File(dir.getAbsolutePath() + "/" + filename);
        if (!dst.exists()) {
            dst.createNewFile();
        }

        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(60000);
        try {
            conn.connect();
        } catch (SocketTimeoutException e) {
            LOGGER.error("Timeout! Connection could not be established.");
            throw e;
        } catch (IOException e) {
            LOGGER.error("Connection could not be opened.");
            throw e;
        }
        InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
        BufferedReader in = new BufferedReader(isr);
        OutputStream out = new GZIPOutputStream(new FileOutputStream(dst));
        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");

        // Transfer bytes from in to out
        try {
            String line;
            while ((line = in.readLine()) != null) {
                writer.write(line);
            }
        } catch (IOException e) {
            LOGGER.error("Documents could not be downloaded.");
            throw e;
        }

        in.close();
        writer.close();
        out.close();
    }

    private int getEnd(final int start, final int max) {
        int end = start + m_stepSize;
        if (end > max) {
            end = max;
        }
        return end;
    }

    private void message(final String str, final double progress) {
        if (getExec() != null) {
            getExec().setProgress(progress, str);
        }
    }

    private URL buildUrl(final Query query, final boolean applyMaxResults)
    throws URISyntaxException, MalformedURLException {

        // Build search url
        String str = SEARCH_QUERY + query.getQuery();
        if (applyMaxResults) {
            str += SEARCH_QUERY_POSTFIX + query.getMaxResults();
        }
        URI uri = new URI(PROTOCOL, HOST, SEARCH_PATH, str, "");

        return uri.toURL();
    }

    private int buildResultList(final URL url) throws IOException,
    CanceledExecutionException {
        // Read search result xml
        int results = -1;
        BufferedReader r = new BufferedReader(
                new InputStreamReader(url.openStream()));
        m_idList.clear();
        String line = null;
        while ((line = r.readLine()) != null) {
            checkCanceled();

            // regular expression to find the "id" field
            String regex = "<Id>(\\d+)</Id>";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(line);

            // find id sequences in search result
            if (m.find()) {
                int id = Integer.parseInt(m.group(1));
                m_idList.add(id);
            }

            if (results == -1) {
                // regular expression to find the "count" field
                String regexCount = "<Count>(\\d+)</Count>";
                p = Pattern.compile(regexCount);
                m = p.matcher(line);

                // find count sequences in search result
                if (m.find()) {
                    results = Integer.parseInt(m.group(1));
                }
            }
        }

        return results;
    }

    /**
     * @return The number of grabbed documents.
     */
    public int getNumberOfDocuments() {
        return m_idList.size();
    }

    /**
     * @return the delay time between two requests in milliseconds
     */
    public long getDelayMillis() {
        return m_delayMillis;
    }

    /**
     * @param delayMillis the delay time between two requests in milliseconds
     * to set.
     */
    public void setDelayMillis(final long delayMillis) {
        m_delayMillis = delayMillis;
    }

    /**
     * @return the stepSize which specifies the number of abstracts stored in
     * one file.
     */
    public int getStepSize() {
        return m_stepSize;
    }

    /**
     * @param stepSize the stepSize to set, which specifies the number of
     * abstracts stored in one file.
     */
    public void setStepSize(final int stepSize) {
        m_stepSize = stepSize;
    }

    private class InternalDocumentParsedEventListener
    implements DocumentParsedEventListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void documentParsed(final DocumentParsedEvent event) {
            notifyAllListener(event);
        }
    }
}

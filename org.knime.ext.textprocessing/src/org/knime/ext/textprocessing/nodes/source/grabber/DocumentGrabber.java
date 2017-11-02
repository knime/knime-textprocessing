/*
========================================================================
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

import java.io.File;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;



/**
 * An interface which provides method declarations to get the number of results
 * of a specified query or download the result documents and parse them.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentGrabber {

    /**
     * @return the unique name of the grabber used for registration and selection
     * @since 2.8
     */
    public String getName();

    /**
     * Sends the given query to the corresponding bibliographic database and
     * stores the resulting documents into the specified directory.
     * To which database the query is send relates on the underlying
     * implementation of this interface. After downloading the related parser
     * id used to parse the documents and return them in a list.
     *
     * @param directory The directory to save the documents to.
     * @param query The query to send to the bibliographic database.
     * @throws Exception If grabber cannot connect to the server or something
     * else goes wrong.
     * @return The list of parsed documents.
     */
    @Deprecated
    public List < Document > grabDocuments(final File directory,
            final Query query) throws Exception;

    /**
     * Sends the given query to the corresponding bibliographic database
     * and returns the number of resulting documents.
     * To which database the query is send relates on the underlying
     * implementation of this interface.
     *
     * @param query The query to send to the bibliographic database.
     * @return The number of resulting documents related to the given query.
     * @throws Exception If grabber cannot connect to the server of something
     * else goes wrong.
     */
    public int numberOfResults(final Query query) throws Exception;

    /**
     * Fetches and the documents resulting from the given query and writes
     * them to the given directory. Afterwards the documents are parsed and
     * instances of {@link org.knime.ext.textprocessing.data.Document}s are
     * created. After each parsed document all registered
     * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener}
     * are notified.
     *
     * @param directory The directory to save the documents to.
     * @param query The query to send to the bibliographic database.
     * @throws Exception If grabber cannot connect to the server or something
     * else goes wrong.
     */
    public void fetchAndParseDocuments(final File directory, final Query query)
    throws Exception;

    /**
     * Adds the given {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener}
     * from the list of listeners.
     * @param listener Listener to add.
     */
    public void addDocumentParsedListener(
            final DocumentParsedEventListener listener);

    /**
     * Removes the given {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener}
     * from the list of listeners.
     * @param listener Listener to remove.
     */
    public void removeDocumentParsedListener(
            final DocumentParsedEventListener listener);

    /**
     * Removes all {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener}
     * from the list of listeners.
     */
    public void removeAllDocumentParsedListener();

    /**
     * Sets the name of the tokenizer for the document grabber.
     *
     * @param tokenizerName The name of the word tokenizer, must not be <code>null</code>
     * @since 3.3
     */
    public void setTokenizerName(final String tokenizerName);

    /**
     * Returns the name of the tokenizer used for word tokenization.
     *
     * @return the name of the word tokenizer, never <code>null</code>
     * @since 3.3
     */
    public String getTokenizerName();
}

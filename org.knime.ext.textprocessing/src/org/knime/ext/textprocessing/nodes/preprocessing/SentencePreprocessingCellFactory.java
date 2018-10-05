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
 *   Sep 21, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * This class provides functionality for {@link Sentence} based preprocessing. This {@code CellFactory} should be used
 * if the preprocessing task takes cross term information into account. If terms can be processed one by one, please
 * consider to use {@code PreprocessingCellFactory}.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.7
 */
public final class SentencePreprocessingCellFactory extends SingleCellFactory {

    private final SentencePreprocessing m_preprocessing;

    private final int m_docColIndex;

    private final TextContainerDataCellFactory m_documentCellFac;

    private final boolean m_preprocessUnmodifiable;

    private boolean m_isFactoryPrepared = false;

    /**
     * Creates a new instance of {@code SentencePreprocessingCellFactory}.
     *
     * @param preprocessing Instance of {@code SentenceProcessing} holding methods to preprocess {@code Sentences}
     * @param documentColIndex The index of the {@code Document} column
     * @param newColSpec The {@code DataColumnSpec} of the data table to create
     * @param preprocessUnmodifiable Set true, if unmodifiable terms should be processed
     */
    public SentencePreprocessingCellFactory(final SentencePreprocessing preprocessing, final int documentColIndex,
        final DataColumnSpec newColSpec, final boolean preprocessUnmodifiable) {
        super(true, newColSpec);
        this.setParallelProcessing(true);

        m_preprocessing = preprocessing;
        m_docColIndex = documentColIndex;
        m_preprocessUnmodifiable = preprocessUnmodifiable;
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getCell(final DataRow row) {
        // prepare document cell factory
        synchronized (m_documentCellFac) {
            if (!m_isFactoryPrepared) {
                m_documentCellFac.prepare(getFileStoreFactory());
                m_isFactoryPrepared = true;
            }
        }

        // check missing
        if (row.getCell(m_docColIndex).isMissing()) {
            return DataType.getMissingCell();
        }

        // apply preprocessing
        Document preprocessedDoc;
        preprocessedDoc = preprocessDocument(((DocumentValue)row.getCell(m_docColIndex)).getDocument());

        // create new document cell
        return m_documentCellFac.createDataCell(preprocessedDoc);
    }

    /**
     * Preprocesses the document based on the specific implementation of
     * {@link SentencePreprocessing#preprocessSentence(Sentence, boolean)}.
     *
     * @param document The {@code Document} to preprocess
     * @return Returns the preprocessed {@code Document}
     */
    private Document preprocessDocument(final Document document) {
        final DocumentBuilder builder = new DocumentBuilder(document);
        for (final Section s : document.getSections()) {
            for (final Paragraph p : s.getParagraphs()) {
                for (final Sentence sen : p.getSentences()) {
                    final Sentence newSentence = sen.getText().isEmpty() ? sen
                        : m_preprocessing.preprocessSentence(sen, m_preprocessUnmodifiable);
                    builder.addSentence(newSentence);
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        return builder.createDocument();
    }

}

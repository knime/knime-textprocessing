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
 *   28.10.2015 (Kilian): created
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
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * Cell Factory, creating new document cells of preprocessed documents.
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 */
final class PreprocessingCellFactory extends SingleCellFactory {

    private final TermPreprocessing m_preprocessing;

    private final int m_docColIndex;

    private final TextContainerDataCellFactory m_documentCellFac;

    private final boolean m_preprocessUnmodifiable;

    private boolean m_isFactoryPrepared = false;

    public PreprocessingCellFactory(final TermPreprocessing preprocessing, final int documentColIndex,
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
        // Note: TermPreprocessing has to be thread safe!
        Document preprocessedDoc = preprocessDocument(((DocumentValue)row.getCell(m_docColIndex)).getDocument());

        // create new document cell
        return m_documentCellFac.createDataCell(preprocessedDoc);
    }

    private Document preprocessDocument(final Document document) {
        final DocumentBuilder builder = new DocumentBuilder(document);
        for (final Section s : document.getSections()) {
            for (final Paragraph p : s.getParagraphs()) {
                for (final Sentence sen : p.getSentences()) {
                    Term previous = null;
                    for (Term t : sen.getTerms()) {
                        // check unmodifiability, or ignore flag and preprocess term
                        if (!t.isUnmodifiable() || m_preprocessUnmodifiable) {
                            t = m_preprocessing.preprocessTerm(t);
                        }
                        // if current term is empty (filtered, replaced) shift white space suffix to previous term
                        if (t != null && t.getText().isEmpty() && previous != null) {
                            final String whiteSpaceSuffix =
                                t.getWords().get(t.getWords().size() - 1).getWhitespaceSuffix();
                            previous.getWords().get(previous.getWords().size() - 1)
                                .addWhiteSpaceSuffix(whiteSpaceSuffix);
                        }
                        // add previous term if not empty
                        if (previous != null && !previous.getText().isEmpty()) {
                            builder.addTerm(previous);
                        }
                        // shift current term to previous
                        previous = t;
                    }
                    // add last term if not empty
                    if (previous != null && !previous.getText().isEmpty()) {
                        builder.addTerm(previous);
                    }

                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        return builder.createDocument();
    }
}

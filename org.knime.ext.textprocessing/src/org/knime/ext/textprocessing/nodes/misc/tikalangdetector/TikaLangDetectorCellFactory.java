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
 *   05.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikalangdetector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaLangDetectorCellFactory extends AbstractCellFactory {

    private static final String UNDEFINED = "Undefined";

    private int m_colIndex = -1;

    private LanguageDetector m_langDetector;

    private int m_specLength;

    private boolean m_isCollection;

    private int m_langIndex;

    private int m_cValueIndex;

    /**
     * Creates a new instance of <code>TikaLangDetectorCellFactory</code> given an index of the column containing
     * <code>DocumentCell</code>s or <code>StringCell</code>s.
     *
     * @param colIndex the index of the column containing <code>DocumentCell</code>s or <code>StringCell</code>s.
     * @param newColSpecs the column specs for the detected languages and confidence value.
     * @param langIndex the column index for the language column.
     * @param cValueIndex the confidence value column index.
     * @param collection boolean to specify whether the cells should be collection cells.
     * @throws IOException if Tika fails to load the language models.
     */
    public TikaLangDetectorCellFactory(final int colIndex, final DataColumnSpec[] newColSpecs, final int langIndex,
        final int cValueIndex, final boolean collection) throws IOException {
        super(newColSpecs);
        m_colIndex = colIndex;
        m_langDetector = new OptimaizeLangDetector().loadModels();
        m_specLength = newColSpecs.length;
        m_isCollection = collection;
        m_langIndex = langIndex;
        m_cValueIndex = cValueIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DataCell cell = row.getCell(m_colIndex);
        DataCell[] newCells = new DataCell[m_specLength];

        if (cell.isMissing()) {
            newCells[m_langIndex] = DataType.getMissingCell();
            if (m_cValueIndex > 0) {
                newCells[m_cValueIndex] = DataType.getMissingCell();
            }
            return newCells;
        }

        List<LanguageResult> lang = new ArrayList<LanguageResult>();
        if (cell.getType().equals(StringCell.TYPE)) {
            lang = m_langDetector.detectAll(((StringCell)cell).getStringValue());
        } else if (cell instanceof DocumentValue) {
            lang = m_langDetector.detectAll(((DocumentValue)cell).getDocument().getText());
        }

        List<StringCell> langCells = new ArrayList<StringCell>();
        List<DoubleCell> valueCells = new ArrayList<DoubleCell>();
        for (LanguageResult res : lang) {
            if (res != null && !res.isUnknown()) {
                langCells.add(new StringCell(res.getLanguage()));
                valueCells.add(
                    new DoubleCell(new BigDecimal(res.getRawScore()).setScale(2, BigDecimal.ROUND_DOWN).doubleValue()));
            }
        }

        if (lang.isEmpty() || (langCells.isEmpty() && valueCells.isEmpty())) {
            if (m_isCollection) {
                langCells.add(new StringCell(UNDEFINED));
                valueCells.add(new DoubleCell(0));
                newCells[m_langIndex] = CollectionCellFactory.createListCell(langCells);
                if (m_cValueIndex > 0) {
                    newCells[m_cValueIndex] = CollectionCellFactory.createListCell(valueCells);
                }
            } else {
                newCells[m_langIndex] = new StringCell(UNDEFINED);
                if (m_cValueIndex > 0) {
                    newCells[m_cValueIndex] = new DoubleCell(0);
                }
            }
            return newCells;
        }

        if (m_specLength > 1) {
            if (m_isCollection) {
                newCells[m_langIndex] = CollectionCellFactory.createListCell(langCells);
                newCells[m_cValueIndex] = CollectionCellFactory.createListCell(valueCells);
            } else {
                newCells[m_langIndex] = new StringCell(langCells.get(0).getStringValue());
                newCells[m_cValueIndex] = new DoubleCell(valueCells.get(0).getDoubleValue());
            }
            return newCells;
        } else {
            if (m_isCollection) {
                newCells[m_langIndex] = CollectionCellFactory.createListCell(langCells);
            } else {
                newCells[m_langIndex] = new StringCell(langCells.get(0).getStringValue());
            }
            return newCells;
        }
    }

}

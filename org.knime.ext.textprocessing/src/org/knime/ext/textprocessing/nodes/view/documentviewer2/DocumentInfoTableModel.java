/*
 * ------------------------------------------------------------------------
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
 * Created on 09.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.DefaultTableModel;

import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;


/**
 * A table model containing the basic document meta information.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class DocumentInfoTableModel extends DefaultTableModel implements Observer {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = 3735659735470727304L;

    private final DocumentViewModel m_docViewerModel;

    private final List<DocumentSource> m_docSources = new ArrayList<DocumentSource>();

    private final List<DocumentCategory> m_docCategories = new ArrayList<DocumentCategory>();

    /**
     * Creates a new instance of {@code DocumentInfoTableModel} with given document view model, containing the basic
     * document meta information.
     *
     * @param docViewerModel The document view model, containing the basic document meta information.
     */
    public DocumentInfoTableModel(final DocumentViewModel docViewerModel) {
        super();

        if (docViewerModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewerModel = docViewerModel;
        fillSourceAndCategorList();
    }

    private void fillSourceAndCategorList() {
        m_docSources.clear();
        m_docCategories.clear();
        m_docSources.addAll(m_docViewerModel.getDocument().getSources());
        m_docCategories.addAll(m_docViewerModel.getDocument().getCategories());
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        if (m_docSources == null || m_docCategories == null) {
            return 0;
        }

        return 3 + m_docSources.size() + m_docCategories.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(final int column) {
        if (column == 0) {
            return "Name";
        } else if (column == 1) {
            return "Value";
        }
        return "";
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        String value = "";
        if (rowIndex == 0) {
            if (columnIndex == 0) {
                value = "Filename";
            } else if (columnIndex == 1) {
                value = m_docViewerModel.getDocument().getDocFile().getAbsolutePath().toString();
            }
        } else if (rowIndex == 1) {
            if (columnIndex == 0) {
                value = "Publication date";
            } else if (columnIndex == 1) {
                value = m_docViewerModel.getDocument().getPubDate().toString();
            }
        } else if (rowIndex == 2) {
            if (columnIndex == 0) {
                value = "Document type";
            } else if (columnIndex == 1) {
                value = m_docViewerModel.getDocument().getType().toString();
            }
        } else if (rowIndex > 2 && rowIndex <= 2 + m_docSources.size()) {
            if (columnIndex == 0) {
                value = "Document source";
            } else if (columnIndex == 1) {
                value = m_docSources.get(rowIndex - 3).getSourceName();
            }
        } else if (rowIndex > 2 + m_docSources.size() && rowIndex <= 2 + m_docSources.size() + m_docCategories.size()) {
            if (columnIndex == 0) {
                value = "Document category";
            } else if (columnIndex == 1) {
                value = m_docCategories.get(rowIndex - 3 - m_docSources.size()).getCategoryName();
            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable o, final Object arg) {
        fillSourceAndCategorList();
        fireTableDataChanged();
    }
}

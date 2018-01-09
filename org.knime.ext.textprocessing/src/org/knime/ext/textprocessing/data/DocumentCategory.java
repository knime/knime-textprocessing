/*
 * ------------------------------------------------------------------------
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
 *   11.01.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Contains the category of a {@link org.knime.ext.textprocessing.data.Document}
 * , which can for instance be artificial intelligence, presidential elections,
 * breast cancer, etc. The category of a
 * {@link org.knime.ext.textprocessing.data.Document} specifies its superior
 * topic.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentCategory implements Externalizable {

    /**
     * The default source name.
     */
    private static final String DEFAULT_CATEGORY = "";

    private String m_categoryName;

    /**
     * Creates new instance of <code>DocumentCategory</code> with the given name
     * of the category.
     * 
     * @param categoryName The name of the source to set.
     */
    public DocumentCategory(final String categoryName) {
        if (categoryName == null) {
            m_categoryName = DocumentCategory.DEFAULT_CATEGORY;
        } else {
            m_categoryName = categoryName;
        }
    }

    /**
     * Creates empty <code>DocumentCategory</code> instance with the default
     * name.
     */
    public DocumentCategory() {
        this(null);
    }

    /**
     * @return The name of the category.
     */
    public String getCategoryName() {
        return m_categoryName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof DocumentCategory)) {
            return false;
        }
        DocumentCategory dc = (DocumentCategory)o;
        if (!dc.getCategoryName().equals(m_categoryName)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_categoryName.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(m_categoryName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_categoryName = in.readUTF();
    }    
}

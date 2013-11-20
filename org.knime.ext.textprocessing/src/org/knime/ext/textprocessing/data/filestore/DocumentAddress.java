/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 13.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.data.filestore;

import java.util.UUID;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Address of a document in a file store file, containing length of the document, offset in file store file and uuid
 * of the document.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
final class DocumentAddress {
    private final UUID m_uuid;
    private final int m_length;
    private final long m_offset;

    /**
     * Constructor for class {@link DocumentAddress}.
     * @param uuid the uuid of the document.
     * @param offset the offset of the document in its file store file.
     * @param length the length of the document.
     */
    DocumentAddress(final UUID uuid, final long offset, final int length) {
        this.m_uuid = uuid;
        this.m_offset = offset;
        this.m_length = length;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return m_uuid;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return m_length;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return m_offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(119, 17).append(m_uuid).append(m_length).append(m_offset).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DocumentAddress)) {
            return false;
        }
        final DocumentAddress da = (DocumentAddress)obj;
        if (!m_uuid.equals(da.getUuid())) {
            return false;
        } else if (m_offset != da.getOffset()) {
            return false;
        } else if (m_length != da.getLength()) {
            return false;
        }

        return true;
    }
}

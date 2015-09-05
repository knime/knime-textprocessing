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
 * Created on 29.03.2013 by kilian
 */
package org.knime.ext.textprocessing.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Contains meta information of a document. Meta information is stored in a key
 * value manner. Meta information can be assigned to
 * {@link org.knime.ext.textprocessing.data.Document}
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class DocumentMetaInfo implements Externalizable {

    /**
     * SerialVersionID.
     */
    private static final long serialVersionUID = -2290368394234923954L;

    private HashMap<String, String> m_metaInfo;

    private int m_hashCode = -1;

    /**
     * Constructor of {@link DocumentMetaInfo}. Creates empty meta information
     * instance.
     */
    public DocumentMetaInfo() {
        m_metaInfo = new LinkedHashMap<String, String>();
    }

    /**
     * Constructor of {@link DocumentMetaInfo}. Creates meta information
     * instance with given key value pairs to add. Keys and values are only
     * added if not <code>null</code>.
     * @param metaInfo The key value pairs to add.
     * @since 3.0
     */
    public DocumentMetaInfo(final HashMap<String, String> metaInfo) {
        m_metaInfo = new LinkedHashMap<String, String>();
        if (metaInfo != null) {
            for (String key : metaInfo.keySet()) {
                String value = metaInfo.get(key);
                if (value != null) {
                    m_metaInfo.put(key, value);
                }
            }
        }
    }

    /**
     * @return The size of the map storing the meta info key, value pairs.
     * @since 2.9
     */
    public int size() {
        return m_metaInfo.size();
    }

    /**
     * @return The set of keys of the meta information.
     */
    public Set<String> getMetaInfoKeys() {
        return Collections.unmodifiableSet(m_metaInfo.keySet());
    }

    /**
     * Returns the meta information value corresponding to the given key.
     * @param key The key of the meta information.
     * @return the meta information value corresponding to the given key.
     */
    public String getMetaInfoValue(final String key) {
        return m_metaInfo.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DocumentMetaInfo)) {
            return false;
        }

        DocumentMetaInfo mi = (DocumentMetaInfo)o;

        Set<String> keySet1 = m_metaInfo.keySet();
        Set<String> keySet2 = new HashSet<String>(mi.getMetaInfoKeys());

        if (keySet2.retainAll(keySet1)) {
            return false;
        }

        for (String key : m_metaInfo.keySet()) {
            String val1 = this.getMetaInfoValue(key);
            String val2 = mi.getMetaInfoValue(key);
            if (!val1.equals(val2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (m_hashCode == -1) {
            int hashCode = 5;
            for (String key : m_metaInfo.keySet()) {
                hashCode += 119 * (key.hashCode()
                        + m_metaInfo.get(key).hashCode());
            }

            m_hashCode = hashCode;
        }
        return m_hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(m_hashCode);
        out.writeInt(m_metaInfo.size());
        for (String key : m_metaInfo.keySet()) {
            out.writeUTF(key);
            out.writeUTF(m_metaInfo.get(key));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
    ClassNotFoundException {
        m_hashCode = in.readInt();
        int size = in.readInt();
        m_metaInfo = new LinkedHashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            String value = in.readUTF();
            m_metaInfo.put(key, value);
        }
    }
}

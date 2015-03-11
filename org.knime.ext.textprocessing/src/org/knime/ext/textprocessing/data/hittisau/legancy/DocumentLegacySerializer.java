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
 *   10.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.legancy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.hittisau.Document;
import org.knime.ext.textprocessing.data.hittisau.Serializer;

/**
 *
 * @author Alexander Fillbrunn
 */
public class DocumentLegacySerializer implements Serializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final Document doc, final DataOutput out) throws IOException {
        if (!(doc instanceof DocumentLegacy)) {
            throw new IllegalArgumentException("The document must be of type DocumentLegacy.");
        }
        DocumentLegacy docLeg = (DocumentLegacy)doc;
        out.writeUTF(docLeg.getUUID().toString());
        out.writeUTF(docLeg.getTitle());
        out.writeInt(docLeg.getNumberOfTerms());
        out.writeInt(docLeg.getTerms().length);
        for (String t : docLeg.getTerms()) {
            out.writeUTF(t);
        }
        out.writeInt(docLeg.getWhitespaces().length);
        for (String ws : docLeg.getWhitespaces()) {
            out.writeUTF(ws);
        }
        out.writeInt(docLeg.getTagBuilders().length);
        for (TagBuilder tb : docLeg.getTagBuilders()) {
            out.writeUTF(tb.getType());
        }
        DocumentMetaInfo metaInfo = docLeg.getMetaInformation();
        out.writeInt(metaInfo.size());
        for (String key : metaInfo.getMetaInfoKeys()) {
            out.writeUTF(key);
            out.writeUTF(metaInfo.getMetaInfoValue(key));
        }
        out.writeInt(docLeg.getNumberOfSentences());
        for (int si = 0; si < docLeg.getNumberOfSentences(); si++) {
            InternalTerm[] terms = docLeg.getSentences()[si];
            out.writeInt(terms.length);
            for (int ti = 0; ti < terms.length; ti++) {
                InternalTerm term = terms[ti];
                final boolean isComplex = (term.getTags().length > 0);

                out.writeBoolean(isComplex);
                out.writeInt(term.getTermIndex());
                out.writeInt(term.getWhiteSpaceIndex());
                out.writeBoolean(term.isImmutable());

                if (isComplex) {
                    out.writeInt(term.getTags().length);
                    for (int tgi = 0; tgi < term.getTags().length; tgi++) {
                        int[] tagValues = term.getTags()[tgi];
                        out.writeInt(tagValues.length);
                        for (int tvi = 0; tvi < tagValues.length; tvi++) {
                            out.writeInt(tagValues[tvi]);
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document deserialize(final DataInput in) throws IOException {
        String title;
        UUID uuid;
        int numTerms;
        String[] terms;
        String[] whitespaces;
        Map<String, String> metaInfo = new HashMap<String, String>();
        TagBuilder[] tagBuilders;
        InternalTerm[][] sentences;

        uuid = UUID.fromString(in.readUTF());
        title = in.readUTF();

        // Terms
        numTerms = in.readInt();
        terms = new String[in.readInt()];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = in.readUTF();
        }

        // Whitespaces
        whitespaces = new String[in.readInt()];
        for (int i = 0; i < whitespaces.length; i++) {
            whitespaces[i] = in.readUTF();
        }

        tagBuilders = new TagBuilder[in.readInt()];
        for (int i = 0; i < tagBuilders.length; i++) {
            tagBuilders[i] = TagFactory.getInstance().getTagSetByType(in.readUTF());
        }

        // Metainfo
        int metaSize = in.readInt();
        for (int i = 0; i < metaSize; i++) {
            metaInfo.put(in.readUTF(), in.readUTF());
        }

        int numSentences = in.readInt();
        sentences = new InternalTerm[numSentences][];
        // Sentences
        for (int i = 0; i < numSentences; i++) {
            int nt = in.readInt();
            sentences[i] = new InternalTerm[nt];
            for (int ti = 0; ti < nt; ti++) {
                InternalTerm t = new InternalTerm();
                final boolean isComplex = in.readBoolean();
                t.setTermIndex(in.readInt());
                t.setWhiteSpaceIndex(in.readInt());
                t.setImmutable(in.readBoolean());

                if (isComplex) {
                    int[][] values = new int[in.readInt()][];
                    for (int tgi = 0; tgi < values.length; tgi++) {
                        values[tgi] = new int[in.readInt()];
                        for (int tvi = 0; tvi < values[tgi].length; tvi++) {
                            values[tgi][tvi] = in.readInt();
                        }
                    }
                    t.setTags(values);
                } else {
                    int[][] values = new int[0][0];
                    t.setTags(values);
                }

                sentences[i][ti] = t;
            }
        }

        return new DocumentLegacy(uuid, title, numTerms, new DocumentMetaInfo(metaInfo), terms, whitespaces,
            tagBuilders, sentences);
    }
}

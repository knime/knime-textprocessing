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
 *   11.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.betterdoc;

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
 * @author Alexander
 */
public class FastDocumentSerializer implements Serializer {
    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final Document doc, final DataOutput out) throws IOException {
        if (!(doc instanceof FastDocument)) {
            throw new IllegalArgumentException("The document must be of type DocumentLegacy.");
        }
        FastDocument fdoc = (FastDocument)doc;
        out.writeUTF(fdoc.getUUID().toString());
        out.writeUTF(fdoc.getTitle());
        out.writeInt(fdoc.getNumberOfTerms());
        out.writeInt(fdoc.getTerms().length);
        for (String t : fdoc.getTerms()) {
            out.writeUTF(t);
        }
        out.writeInt(fdoc.getWhitespaces().length);
        for (String ws : fdoc.getWhitespaces()) {
            out.writeUTF(ws);
        }
        out.writeInt(fdoc.getTagBuilders().length);
        for (TagBuilder tb : fdoc.getTagBuilders()) {
            out.writeUTF(tb.getType());
        }
        DocumentMetaInfo metaInfo = fdoc.getMetaInformation();
        out.writeInt(metaInfo.size());
        for (String key : metaInfo.getMetaInfoKeys()) {
            out.writeUTF(key);
            out.writeUTF(metaInfo.getMetaInfoValue(key));
        }
        out.writeInt(fdoc.getNumberOfSentences());
        for (int si = 0; si < fdoc.getNumberOfSentences(); si++) {
            FastSentence s = fdoc.getSentences()[si];
            out.writeInt(s.getTermArray().length);
            for (int ti = 0; ti < s.getTermArray().length; ti++) {
                FastTerm term = s.getTermArray()[ti];
                final boolean isComplex = (term.getTagArray().length > 0);

                out.writeBoolean(isComplex);
                out.writeInt(term.getTermIndex());
                out.writeInt(term.getWhiteSpaceIndex());
                out.writeBoolean(term.isImmutable());

                if (isComplex) {
                    out.writeInt(term.getTagArray().length);
                    for (int tgi = 0; tgi < term.getTagArray().length; tgi++) {
                        int[] tagValues = term.getTagArray()[tgi];
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
        FastSentence[] sentences;

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

        DocumentWrappingLookupTable docWrap = new DocumentWrappingLookupTable(null);

        int numSentences = in.readInt();
        sentences = new FastSentence[numSentences];
        // Sentences
        for (int i = 0; i < numSentences; i++) {
            int nt = in.readInt();
            FastTerm[] fastterms = new FastTerm[nt];
            for (int ti = 0; ti < nt; ti++) {

                final boolean isComplex = in.readBoolean();
                int termIdx = in.readInt();
                int wsIdx = in.readInt();
                boolean immutable = in.readBoolean();
                int[][] values;
                if (isComplex) {
                    values = new int[in.readInt()][];
                    for (int tgi = 0; tgi < values.length; tgi++) {
                        values[tgi] = new int[in.readInt()];
                        for (int tvi = 0; tvi < values[tgi].length; tvi++) {
                            values[tgi][tvi] = in.readInt();
                        }
                    }
                } else {
                    values = new int[0][0];
                }

                fastterms[ti] = new FastTerm(termIdx, wsIdx, values, immutable, docWrap, docWrap);
            }
        }

        FastDocument doc = new FastDocument(uuid, title, numTerms, new DocumentMetaInfo(metaInfo), terms, whitespaces,
            tagBuilders, sentences);
        docWrap = new DocumentWrappingLookupTable(doc);
        return doc;
    }
}

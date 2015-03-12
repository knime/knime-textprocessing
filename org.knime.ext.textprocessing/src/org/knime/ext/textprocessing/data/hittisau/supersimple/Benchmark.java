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
 *   09.03.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.data.hittisau.supersimple;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 *
 * @author Kilian
 */
public class Benchmark {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(Benchmark.class);

    //private final static String DATA_PATH = "/Users/Alexander/Desktop/";
    private final static String DATA_PATH = "D:/Data/Text/Benchmarking/";

    private final static String DATA_FILE = "5k-lines.csv";

    private final static String TWEETS_FILE = "Tweets.csv";

    private static final List<String> ARTIFICIAL_DATA_FILES = new LinkedList<>();
    static {
        ARTIFICIAL_DATA_FILES.add("docs10.csv");
        ARTIFICIAL_DATA_FILES.add("docs50.csv");
        ARTIFICIAL_DATA_FILES.add("docs100.csv");
        ARTIFICIAL_DATA_FILES.add("docs500.csv");
    }

    private final static int NUMBER_OF_RUNS = 50;

    @Test
    public void runBenchmark() {
        final File f = new File(DATA_PATH + TWEETS_FILE);

        for (int i = 1; i <= NUMBER_OF_RUNS; i++) {
            doBenchmarkDocumentSuperSimple(f);
        }
    }

    public void doBenchmarkDocumentSuperSimple(final File f) {
        // read string data
        List<String> strs = readStringData(f);
        List<SuperSimpleDocument> documents = new LinkedList<>();

        // benchmark creation
        long start, end;
        long sumDeltaLegacy = 0;

        for (String line : strs) {
            start = System.currentTimeMillis();

            SuperSimpleDocument d = SuperSimpleDocumentBuilder.createDocument(line);

            end = System.currentTimeMillis();
            sumDeltaLegacy += (end - start);
            documents.add(d);
        }
        double sumDeltaLegacyAvg = (double)sumDeltaLegacy / (double)strs.size();

        System.out.println("Document SuperSimple;Creation;" + strs.size() + ";" + sumDeltaLegacy + ";" + sumDeltaLegacyAvg);

        // iteration benchmark
        for (SuperSimpleDocument d : documents) {
            start = System.currentTimeMillis();
            for (SuperSimpleSentence sentence : d) {
                for (String token : sentence) {

                }
            }
            end = System.currentTimeMillis();
            sumDeltaLegacy += (end - start);
        }
        sumDeltaLegacyAvg = (double)sumDeltaLegacy / (double)strs.size();

        System.out.println("Document SuperSimple;Iteration;" + strs.size() + ";" + sumDeltaLegacy + ";" + sumDeltaLegacyAvg);

        // serialization
        List<SuperSimpleDocument> deserializedDocs = new LinkedList<>();

        long sumDeltaLegacyD = 0;
        long numberOfBytes = 0;
        for (SuperSimpleDocument d : documents) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutput out = new DataOutputStream(baos);

            try {
                start = System.currentTimeMillis();
                TermDocumentDeSerializationUtil.fastSerializeDocument(d, out);
                end = System.currentTimeMillis();
                sumDeltaLegacy += (end - start);

                baos.flush();
                byte[] docAsByteArr = baos.toByteArray();
                numberOfBytes += docAsByteArr.length;
                ByteArrayInputStream bis = new ByteArrayInputStream(docAsByteArr);
                DataInput in = new DataInputStream(bis);

                long startD = System.currentTimeMillis();
                org.knime.ext.textprocessing.data.Document newD =
                    TermDocumentDeSerializationUtil.fastDeserializeDocument(in);
                long endD = System.currentTimeMillis();
                sumDeltaLegacyD += (endD - startD);

                deserializedDocs.add(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sumDeltaLegacyAvg = (double)sumDeltaLegacy / (double)strs.size();
        double sumDeltaLegacyAvgD = (double)sumDeltaLegacyD / (double)strs.size();
        double numberOfBytesAvg = (double)numberOfBytes / (double)strs.size();

        System.out.println("Document Old;Serialization;" + strs.size() + ";" + sumDeltaLegacy + ";" + sumDeltaLegacyAvg);
        System.out.println("Document Old;Deserialization;" + strs.size() + ";" + sumDeltaLegacyD + ";" + sumDeltaLegacyAvgD);
        System.out.println("Document Old;Size;" + strs.size() + ";" + numberOfBytes + ";" + numberOfBytesAvg);
    }

    public List<String> readStringData(final File f) {
        List<String> strings = new LinkedList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                line.trim();
                String[] splits = line.split("<@:@>");
                String text = splits[0].trim();
                if (text != null && !text.isEmpty()) {
                    strings.add(text);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Can not read benchmark data file.");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Cannot close buffered reader");
                }
            }
        }

        return strings;
    }
}

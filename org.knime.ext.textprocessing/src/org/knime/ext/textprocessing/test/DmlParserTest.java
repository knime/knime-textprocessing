/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   20.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.sdml.SdmlDocumentParser;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DmlParserTest {

    /**
     * @param args
     */
    public static void main(String[] args) {        
        File file = new File("E:\\Documents\\KNIME\\TextminingPlugin\\"
                + "testDoc\\SdmlTestDocument.xml");

        DocumentParser p = new SdmlDocumentParser();
        try {
            System.out.println("Parse documents ...");
            List<Document> docs = p.parse(new FileInputStream(file));
            for (Document d : docs) {
                System.out.println(d.getTitle());
            }
            
            System.out.println("Serialize and deserialize documents ...");
            for (Document d : docs) {
                String docStr = DmlDocumentParser.documentAsDml(d);
                DocumentParser parser = new DmlDocumentParser();
                List<Document> docs2 = parser.parse(new ByteArrayInputStream(
                        docStr.getBytes()));
                Document doc = null;
                if (docs2.size() > 0) {
                    doc = docs2.get(0);
                }
                System.out.println(doc.getTitle());
                
                String docStr2 = DmlDocumentParser.documentAsDml(doc);
                if (docStr2.equals(docStr)) {
                    System.out.println("EQUAL");
                } else {
                    System.out.println("NOT EQUAL");
                }
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

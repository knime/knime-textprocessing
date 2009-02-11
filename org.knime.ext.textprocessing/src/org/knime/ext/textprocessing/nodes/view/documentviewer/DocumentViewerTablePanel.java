/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   27.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.util.Set;

import javax.swing.JFrame;

import org.knime.core.node.KNIMEConstants;
import org.knime.ext.textprocessing.data.Document;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerTablePanel extends AbstractDocumentTablePanel {

    /**
     * Creates a new instance of <code>DocumentViewerTablePanel</code> with
     * the given set of documents to display.
     *
     * @param documents The set of documents to display.
     */
    public DocumentViewerTablePanel(final Set<Document> documents) {
        super(documents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClick(final int rowIndex, final Document document) {
        JFrame detailsFrame = new JFrame("Details: " + document.getTitle());
        if (KNIMEConstants.KNIME16X16 != null) {
            detailsFrame.setIconImage(
                    KNIMEConstants.KNIME16X16.getImage());
        }

        detailsFrame.setContentPane(
                new DocumentViewPanel(document));
        detailsFrame.pack();
        detailsFrame.setVisible(true);
    }
}

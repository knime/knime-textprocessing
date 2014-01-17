/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   27.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.util.List;

import javax.swing.JFrame;

import org.knime.core.node.KNIMEConstants;
import org.knime.ext.textprocessing.data.Document;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerTablePanel extends AbstractDocumentTablePanel {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = 3735659735470727304L;

    /**
     * Creates a new instance of <code>DocumentViewerTablePanel</code> with
     * the given set of documents to display.
     *
     * @param documents The set of documents to display.
     */
    public DocumentViewerTablePanel(final List<Document> documents) {
        super(documents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClick(final int rowIndex, final Document document) {
        JFrame detailsFrame = new JFrame(document.getTitle());
        if (KNIMEConstants.KNIME16X16 != null) {
            detailsFrame.setIconImage(KNIMEConstants.KNIME16X16.getImage());
        }

        detailsFrame.setContentPane(new DocumentViewPanel(document, this));
        detailsFrame.pack();
        detailsFrame.setVisible(true);
    }
}

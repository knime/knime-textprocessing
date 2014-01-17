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
 *   27.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;


import javax.swing.JPanel;

import org.knime.core.node.NodeView;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerNodeView extends NodeView<DocumentViewerNodeModel> {
    
    /**
     * Creates a new instance of <code>DocumentViewerNodeView</code>.
     * 
     * @param model The model holding the documents to dosplay.
     */
    public DocumentViewerNodeView(final DocumentViewerNodeModel model) {
        super(model);
        JPanel panel = new DocumentViewerTablePanel(model.getDocumentList());
        setComponent(panel);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // Nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // Nothing to do ...
    }
}

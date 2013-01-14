/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2013
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 *
 * History
 *   21.05.2010 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.nio.charset.Charset;

/**
 * The <code>CharsetDocumentParserNodeDialog</code> extends
 * <code>DocumentParserNodeDialog</code> and shows a drop down box allowing
 * for the selection of a charset which has to be used by the parser.
 * All available charset are shown in the drop down box.
 *
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class CharsetDocumentParserNodeDialog extends DocumentParserNodeDialog {

    static SettingsModelString getCharsetModel() {
        return new SettingsModelString(
                DocumentParserConfigKeys.CFGKEY_CHARSET,
                DocumentParserNodeModel.DEFAULT_CHARSET);
    }

    public CharsetDocumentParserNodeDialog() {
        super();

        String[] charsets = Charset.availableCharsets().keySet().toArray(
                new String[] {});
        addDialogComponent(new DialogComponentStringSelection(
                getCharsetModel(), "Charset", charsets));
    }
}

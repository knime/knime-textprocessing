/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   03.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TextprocessingPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    /**
     * Creates a new preference page.
     */
    public TextprocessingPreferencePage() {
        super(GRID);

        setPreferenceStore(
                TextprocessingCorePlugin.getDefault().getPreferenceStore());
        setDescription("KNIME Textprocessing preferences");
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        
        // Blob cell setting
        BooleanFieldEditor useBlobCells =
                new BooleanFieldEditor(
                        TextprocessingPreferenceInitializer.PREF_USE_BLOB,
                        "Use Blob Cells", parent);
        addField(useBlobCells);
        
        // Dml deserialization setting
        BooleanFieldEditor useDmlDeserialization =
            new BooleanFieldEditor(
                   TextprocessingPreferenceInitializer.PREF_DML_DESERIALIZATION,
                   "Enable backwards compatibility / load old " 
                   + "textprocessing nodes (2.4.x and older). " 
                   + "Uncheck to speed up processing.", parent);
        addField(useDmlDeserialization);
    }

    /**
     * {@inheritDoc}
     */
    public void init(final IWorkbench workbench) {
        // nothing to do
    }
}

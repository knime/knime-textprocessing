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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
    
    private static final String DESC_SERIALIZATION = 
        "Check to enable loading of workflows containing old textprocessing " 
        + "nodes (2.4.x and older).\nNote that backwards compatibility slows " 
        + "down deserialization and thus buffering and\nprocessing of nodes. "
        + "Uncheck to speed up processing. If workflows containing older " 
        + " nodes\nare loaded once, option can be unchecked.";

    private static final String DESC_SERIALIZATION_I = 
        "Check to enable loading of workflows containing old textprocessing " 
        + "nodes (2.4.x and older). Note that backwards compatibility slows " 
        + "down deserialization and thus buffering and processing of nodes. "
        + "Uncheck to speed up processing. If workflows containing older " 
        + "nodes are loaded and saved once, option can be unchecked.";
    
    private static final String DESC_PREPROCESSING = 
        "If checked, nodes of the \"Preprocessing\" category process data " 
        + "in memory. Large sets of\ndocuments may not fit into memory and "
        + "thus can not be processed. Uncheck if documents\ndo not fit into "
        + "memory. Note that unchecking slows down processing significantly. "
        + "\nCheck to speed up processing.";

    private static final String DESC_BLOBCELLS = 
        "If checked documents are stored in blob cells to save memory. "
        + "The usage of blob cells is\nhighly recommended. Do not uncheck "
        + "unless you know exactly what you are doing. The\nusage of regular "
        + "cells will increase the usage of memory significantly.";
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        
        // Dml deserialization setting
        Group serializationGrp = new Group(getFieldEditorParent(), 
                SWT.SHADOW_ETCHED_IN);
        serializationGrp.setText("De-Serialization:");        
        
        BooleanFieldEditor useDmlDeserialization =
            new BooleanFieldEditor(
                   TextprocessingPreferenceInitializer.PREF_DML_DESERIALIZATION,
                   "Backwards compatibility", serializationGrp);
        
        Label lSerial = new Label(serializationGrp, SWT.LEFT | SWT.WRAP);
        lSerial.setText(DESC_SERIALIZATION);
        
        addField(useDmlDeserialization);
        
        // row preprocessing
        Group preprocessingGrp = new Group(getFieldEditorParent(), 
                SWT.SHADOW_ETCHED_IN);
        preprocessingGrp.setText("Preprocessing:");
        
        BooleanFieldEditor useRowPreprocessing =
            new BooleanFieldEditor(
                   TextprocessingPreferenceInitializer.PREF_ROW_PREPROCESSING,
                   "Process preprocessing nodes in memory.", preprocessingGrp);
        
        Label lPrepro = new Label(preprocessingGrp, SWT.NONE);
        lPrepro.setText(DESC_PREPROCESSING); 
        
        addField(useRowPreprocessing);
        
        // Blob cell setting
        Group storageGrp = new Group(getFieldEditorParent(), 
                SWT.SHADOW_ETCHED_IN);
        storageGrp.setText("Storage:");
        
        BooleanFieldEditor useBlobCells =
                new BooleanFieldEditor(
                        TextprocessingPreferenceInitializer.PREF_USE_BLOB,
                        "Use Blob Cells.", storageGrp);
        
        Label lStorage = new Label(storageGrp, SWT.NONE);
        lStorage.setText(DESC_BLOBCELLS);
        
        addField(useBlobCells);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }
}

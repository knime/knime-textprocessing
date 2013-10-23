/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TextprocessingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
IPropertyChangeListener {

    private Composite m_mainComposite;

    private Composite m_chunkSizeComp;

    private RadioGroupFieldEditor m_dataCellType;

    private IntegerFieldEditor m_fileStoreChunkSize;

    private Label m_lFileStoreSetings;

    private BooleanFieldEditor m_useDmlDeserialization;

    private BooleanFieldEditor m_useRowPreprocessing;

    /**
     * Creates a new preference page.
     */
    public TextprocessingPreferencePage() {
        super();
        setPreferenceStore(TextprocessingCorePlugin.getDefault().getPreferenceStore());
        setDescription("KNIME Textprocessing preferences");
    }

    private static final String DESC_SERIALIZATION =
        "Check to enable loading of workflows containing old textprocessing "
        + "nodes (2.4.x and older).\nNote that backwards compatibility slows "
        + "down deserialization and thus buffering and\nprocessing of nodes. "
        + "Uncheck to speed up processing. If workflows containing older "
        + " nodes\nare loaded once, option can be unchecked.";

    private static final String DESC_PREPROCESSING =
        "If checked, nodes of the \"Preprocessing\" category process data "
        + "in memory. Large sets of\ndocuments may not fit into memory and "
        + "thus can not be processed. Uncheck if documents\ndo not fit into "
        + "memory. Note that unchecking slows down processing significantly. "
        + "\nCheck to speed up processing.";

    private static final String DESC_CELLTYPE =
        "It is recommended to use file store cells in order to save memory and "
        + "increase the \nprocessing speed. Regular cells are very memory and "
        + "disk space consuming. Blob cells \ndecrease memory and disk space usage "
        + "but may slow down processing speed.";

    private static final String DESC_FILESTORE_CHUNKSIZE =
        "The file store chunk size defines the number of documents to store "
        + "in a single\nfile store file. The larger the number, the less files "
        + "will be created to store the documents,\nwhich increases processing "
        + "speed. For the smallest possible number 1, a file will be created\n"
        + "for each document, which solws down processing speed.";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(final Composite parent) {
        m_mainComposite = new Composite(parent, SWT.LEFT);
        m_mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gl = new GridLayout(1, true);
        gl.verticalSpacing = 15;
        m_mainComposite.setLayout(gl);


        // Cell Type Settings
        final Group storageGrp = new Group(m_mainComposite, SWT.SHADOW_ETCHED_IN);
        storageGrp.setText("Document Storage Settings:");

        m_dataCellType = new RadioGroupFieldEditor(
            TextprocessingPreferenceInitializer.PREF_CELL_TYPE, "Document cell type", 1, new String[][] {
                {"File Store Cells", TextprocessingPreferenceInitializer.FILESTORE_CELLTYPE},
                {"Blob Cells", TextprocessingPreferenceInitializer.BLOB_CELLTYPE},
                {"Regular Cells", TextprocessingPreferenceInitializer.REGULAR_CELLTYPE}}, storageGrp);
        m_dataCellType.setPage(this);
        m_dataCellType.setPreferenceStore(getPreferenceStore());
        m_dataCellType.load();
        m_dataCellType.setPropertyChangeListener(this);

        final Label lStorage = new Label(storageGrp, SWT.LEFT | SWT.WRAP);
        lStorage.setText(DESC_CELLTYPE);

        final Label sep = new Label(storageGrp, SWT.HORIZONTAL | SWT.SEPARATOR);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = GridData.CENTER;
        gridData.grabExcessVerticalSpace = false;
        sep.setLayoutData(gridData);

        // file store chunk size settings
        m_chunkSizeComp = new Composite(storageGrp, SWT.LEFT);
        m_chunkSizeComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        m_fileStoreChunkSize = new IntegerFieldEditor(TextprocessingPreferenceInitializer.PREF_FILESTORE_CHUNKSIZE,
                "File store chunk size", m_chunkSizeComp);
        m_fileStoreChunkSize.setPage(this);
        m_fileStoreChunkSize.setPreferenceStore(getPreferenceStore());
        m_fileStoreChunkSize.load();
        m_fileStoreChunkSize.setValidRange(1, Integer.MAX_VALUE);

        m_lFileStoreSetings = new Label(storageGrp, SWT.LEFT | SWT.WRAP);
        m_lFileStoreSetings.setText(DESC_FILESTORE_CHUNKSIZE);

        storageGrp.setLayoutData(getGridData());
        storageGrp.setLayout(getLayout());

        // Row preprocessing
        final Group preprocessingGrp = new Group(m_mainComposite, SWT.SHADOW_ETCHED_IN);
        preprocessingGrp.setText("Preprocessing:");

        m_useRowPreprocessing =
            new BooleanFieldEditor(TextprocessingPreferenceInitializer.PREF_ROW_PREPROCESSING,
                "Process preprocessing nodes in memory.", preprocessingGrp);
        m_useRowPreprocessing.setPage(this);
        m_useRowPreprocessing.setPreferenceStore(getPreferenceStore());
        m_useRowPreprocessing.load();

        final Label lPrepro = new Label(preprocessingGrp, SWT.LEFT | SWT.WRAP);
        lPrepro.setText(DESC_PREPROCESSING);

        preprocessingGrp.setLayoutData(getGridData());
        preprocessingGrp.setLayout(getLayout());

        // Dml deserialization setting
        final Group serializationGrp = new Group(m_mainComposite, SWT.SHADOW_ETCHED_IN);
        serializationGrp.setText("Serialization:");

        m_useDmlDeserialization =
            new BooleanFieldEditor(TextprocessingPreferenceInitializer.PREF_DML_DESERIALIZATION,
                "Backwards compatibility (to 2.4 and older)", serializationGrp);
        m_useDmlDeserialization.setPage(this);
        m_useDmlDeserialization.setPreferenceStore(getPreferenceStore());
        m_useDmlDeserialization.load();

        final Label lSerial = new Label(serializationGrp, SWT.LEFT | SWT.WRAP);
        lSerial.setText(DESC_SERIALIZATION);

        serializationGrp.setLayoutData(getGridData());
        serializationGrp.setLayout(getLayout());

        return m_mainComposite;
    }

    private static final GridData getGridData() {
        GridData layoutData = new GridData(GridData.FILL);
        layoutData.widthHint = 500;
        return layoutData;
    }

    private static final Layout getLayout() {
        GridLayout gl = new GridLayout(1, true);
        gl.marginTop = 5;
        gl.verticalSpacing = 10;
        return gl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench) { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDefaults() {
        m_dataCellType.loadDefault();
        m_fileStoreChunkSize.loadDefault();
        m_useDmlDeserialization.loadDefault();
        m_useRowPreprocessing.loadDefault();
        super.performDefaults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        m_dataCellType.store();
        m_fileStoreChunkSize.store();
        m_useDmlDeserialization.store();
        m_useRowPreprocessing.store();
        return super.performOk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getNewValue().equals(TextprocessingPreferenceInitializer.FILESTORE_CELLTYPE)) {
            m_fileStoreChunkSize.setEnabled(true, m_chunkSizeComp);
            m_lFileStoreSetings.setEnabled(true);
        } else {
            m_fileStoreChunkSize.setEnabled(false, m_chunkSizeComp);
            m_lFileStoreSetings.setEnabled(false);
        }
    }
}

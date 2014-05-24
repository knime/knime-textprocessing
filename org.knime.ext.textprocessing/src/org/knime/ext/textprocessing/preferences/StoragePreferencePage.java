/*
 * ------------------------------------------------------------------------
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
 * Created on 12.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.preferences;

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
 * The preference page for the document cell storage settings.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public class StoragePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

    private Composite m_mainComposite;

    private Composite m_chunkSizeComp;

    private RadioGroupFieldEditor m_dataCellType;

    private IntegerFieldEditor m_fileStoreChunkSize;

    private Label m_lFileStoreSetings;


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
     * Constructor for class {@link StoragePreferencePage}. Creates a new preference page for cell storage settings.
     */
    public StoragePreferencePage() {
        super();
        setDescription("Textprocessing Storage Preferences");
        setPreferenceStore(TextprocessingCorePlugin.getDefault().getPreferenceStore());
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
            StoragePreferenceInitializer.PREF_CELL_TYPE, "Document cell type", 1, new String[][] {
                {"File Store Cells", StoragePreferenceInitializer.FILESTORE_CELLTYPE},
                {"Blob Cells", StoragePreferenceInitializer.BLOB_CELLTYPE},
                {"Regular Cells", StoragePreferenceInitializer.REGULAR_CELLTYPE}}, storageGrp);
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
        m_fileStoreChunkSize = new IntegerFieldEditor(StoragePreferenceInitializer.PREF_FILESTORE_CHUNKSIZE,
                "File store chunk size", m_chunkSizeComp);
        m_fileStoreChunkSize.setPage(this);
        m_fileStoreChunkSize.setPreferenceStore(getPreferenceStore());
        m_fileStoreChunkSize.load();
        m_fileStoreChunkSize.setValidRange(1, Integer.MAX_VALUE);

        m_lFileStoreSetings = new Label(storageGrp, SWT.LEFT | SWT.WRAP);
        m_lFileStoreSetings.setText(DESC_FILESTORE_CHUNKSIZE);

        storageGrp.setLayoutData(getGridData());
        storageGrp.setLayout(getLayout());

        // enable/disable file store settings
        if (StoragePreferenceInitializer.cellType().equals(
            StoragePreferenceInitializer.FILESTORE_CELLTYPE)) {
            enableFileStorSettings(true);
        } else {
            enableFileStorSettings(false);
        }

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
    protected void performDefaults() {
        m_dataCellType.loadDefault();
        m_fileStoreChunkSize.loadDefault();
        super.performDefaults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        m_dataCellType.store();
        m_fileStoreChunkSize.store();
        return super.performOk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getNewValue().equals(StoragePreferenceInitializer.FILESTORE_CELLTYPE)) {
            enableFileStorSettings(true);
        } else {
            enableFileStorSettings(false);
        }
    }

    private void enableFileStorSettings(final boolean enable) {
        m_fileStoreChunkSize.setEnabled(enable, m_chunkSizeComp);
        m_lFileStoreSetings.setEnabled(enable);
    }
}

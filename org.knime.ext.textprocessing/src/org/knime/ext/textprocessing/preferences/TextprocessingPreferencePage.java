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
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TextprocessingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Composite m_mainComposite;

    private BooleanFieldEditor m_useDmlDeserialization;

    private BooleanFieldEditor m_useRowPreprocessing;

    private IntegerFieldEditor m_tokenizerPoolSize;

    private BooleanFieldEditor m_initTokenizerPoolOnStartup;

    /**
     * Creates a new preference page.
     */
    public TextprocessingPreferencePage() {
        super();
        setPreferenceStore(TextprocessingCorePlugin.getDefault().getPreferenceStore());
        setDescription("KNIME Textprocessing Preferences");
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

    private static final String DESC_TOKENIZER_POOLSIZE =
        "The maximal number of tokenizers which can be used concurrently. "
      + "All tokenizer will be\ncreated on demand or startup and on change "
      + "of the pool size. Be aware that tokenizers do\nrequire memory. As a "
      + "rule of thump, use not more than #documents/100 tokenizers.";

    private static final String DESC_INIT_POOL_ONSTARTUP =
            "If checked tokenizers are initialized on startup, which slows down "
          + "KNIME startup time.";

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

        // Tokenizer pool
        final Group tokenizationGrp = new Group(m_mainComposite, SWT.SHADOW_ETCHED_IN);
        tokenizationGrp.setText("Tokenization:");

        m_tokenizerPoolSize = new IntegerFieldEditor(TextprocessingPreferenceInitializer.PREF_TOKENIZER_POOLSIZE,
                "Tokenizer pool size", tokenizationGrp);
        m_tokenizerPoolSize.setPage(this);
        m_tokenizerPoolSize.setPreferenceStore(getPreferenceStore());
        m_tokenizerPoolSize.load();
        m_tokenizerPoolSize.setValidRange(1, TextprocessingPreferenceInitializer.MAX_TOKENIZER_POOLSIZE);

        final Label lTokenization = new Label(tokenizationGrp, SWT.LEFT | SWT.WRAP);
        lTokenization.setText(DESC_TOKENIZER_POOLSIZE);

        m_initTokenizerPoolOnStartup = new BooleanFieldEditor(
            TextprocessingPreferenceInitializer.PREF_TOKENIZER_INIT_ONSTARTUP, "Initialize pool on startup",
            tokenizationGrp);
        m_initTokenizerPoolOnStartup.setPage(this);
        m_initTokenizerPoolOnStartup.setPreferenceStore(getPreferenceStore());
        m_initTokenizerPoolOnStartup.load();

        final Label lInitialization = new Label(tokenizationGrp, SWT.LEFT | SWT.WRAP);
        lInitialization.setText(DESC_INIT_POOL_ONSTARTUP);

        tokenizationGrp.setLayoutData(getGridData());
        tokenizationGrp.setLayout(getLayout());

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
        m_useDmlDeserialization.loadDefault();
        m_useRowPreprocessing.loadDefault();
        m_tokenizerPoolSize.loadDefault();
        m_initTokenizerPoolOnStartup.loadDefault();
        super.performDefaults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        m_useDmlDeserialization.store();
        m_useRowPreprocessing.store();
        m_tokenizerPoolSize.store();
        m_initTokenizerPoolOnStartup.store();

        // initialize tokenizer pool with new pool size
        DefaultTokenization.createNewTokenizerPool();

        return super.performOk();
    }
}

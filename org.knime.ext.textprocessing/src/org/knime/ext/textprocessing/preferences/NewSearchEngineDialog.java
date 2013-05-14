/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 10.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.preferences;



import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngineSettings;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngines;


/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class NewSearchEngineDialog extends TitleAreaDialog {

    private Text m_nameTextField;

    private Text m_linkTextField;

    private Button m_defaultChk;

    private SearchEngineSettings m_settings;

    /**
     * CReates new instance of {@code NewSearchEngineDialog}.
     * @param parentShell The parent shell of the dialog
     */
    public NewSearchEngineDialog(final Shell parentShell) {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    @Override
    public void create() {
        super.create();
        setTitle("New Search Engine");
        setMessage("Specify name and link to the search engine. Link must contain protocol and query placeholder "
                 + "\"<query>\", e.g. \"http://www.google.de?q=<query>\".", IMessageProvider.INFORMATION);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
      parent.setLayout(new GridLayout(2, false));

      Label nameLabel = new Label(parent, SWT.NONE);
      nameLabel.setText("Search Engine Name");

      GridData gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = GridData.FILL;
      m_nameTextField = new Text(parent, SWT.BORDER);
      m_nameTextField.setLayoutData(gd);

      Label linkLabel = new Label(parent, SWT.NONE);
      linkLabel.setText("Search Engine Link");

      gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = GridData.FILL;
      m_linkTextField = new Text(parent, SWT.BORDER);
      m_linkTextField.setLayoutData(gd);

      Label defaultLabel = new Label(parent, SWT.NONE);
      defaultLabel.setText("");

      gd = new GridData();
      m_defaultChk = new Button(parent, SWT.CHECK);
      m_defaultChk.setText("default search engine");
      m_defaultChk.setLayoutData(gd);

      return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
      GridData gd = new GridData();
      gd.verticalAlignment = GridData.FILL;
      gd.horizontalSpan = 3;
      gd.grabExcessHorizontalSpace = true;
      gd.grabExcessVerticalSpace = true;
      gd.horizontalAlignment = SWT.CENTER;

      parent.setLayoutData(gd);
      createOkButton(parent, OK, "Add", true);

      Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
      cancelButton.addSelectionListener(new SelectionAdapter() {
        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected(final SelectionEvent e) {
          setReturnCode(CANCEL);
          close();
        }
      });
    }

    /**
     * Creates "OK" button and handles the use input.
     * @param parent the parent composite
     * @param id the button id
     * @param label The button label
     * @param defaultButton if {@code true} button is the default button.
     * @return the "OK" button.
     */
    protected Button createOkButton(final Composite parent, final int id, final String label,
            final boolean defaultButton) {
        ((GridLayout)parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));
        button.addSelectionListener(new SelectionAdapter() {

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent event) {
                if (isValidInput()) {
                    okPressed();
                }
            }
        });
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        setButtonLayoutData(button);
        return button;
    }

    /**
     * @return {@code true} if the user input is valid, otherwise {@code false}.
     */
    private boolean isValidInput() {
        boolean valid = true;
        if (m_nameTextField.getText().isEmpty()) {
            setErrorMessage("Please specify the name of the search engine");
            valid = false;
        }
        if (m_nameTextField.getText().contains(SearchEngineSettings.SETTINGS_SEPARATOR)) {
            setErrorMessage("Search engine name may not contain \"" + SearchEngineSettings.SETTINGS_SEPARATOR + "\"");
            valid = false;
        }
        if (SearchEngines.getInstance().getSearchEngineNames().contains(m_nameTextField.getText())) {
            setErrorMessage("Search engine name \"" + m_nameTextField.getText() + "\" already exists");
            valid = false;
        }

        if (m_linkTextField.getText().isEmpty()) {
            setErrorMessage("Please specify the link to the search engine");
            valid = false;
        }
        if (m_linkTextField.getText().contains(SearchEngineSettings.SETTINGS_SEPARATOR)) {
            setErrorMessage("Search engine link may not contain \"" + SearchEngineSettings.SETTINGS_SEPARATOR + "\"");
            valid = false;
        }
        if (!m_linkTextField.getText().startsWith("http://") && !m_linkTextField.getText().startsWith("https://")) {
            setErrorMessage("Search engine link must start with \"http://\" or \"https://\"");
            valid = false;
        }
        if (!m_linkTextField.getText().contains(SearchEngines.QUERY_PLACEHOLDER)) {
            setErrorMessage("Search engine link must contain query place holder \"" + SearchEngines.QUERY_PLACEHOLDER
                            + "\"");
            valid = false;
        }

        return valid;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    private void saveInput() {
        m_settings = new SearchEngineSettings(m_nameTextField.getText(), m_linkTextField.getText(),
                                              m_defaultChk.getSelection());
    }

    /**
     * @return the specified search engine settings
     */
    public SearchEngineSettings getSettings() {
        return m_settings;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }
}

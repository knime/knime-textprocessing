/************************************************************
 * Copyright 2004-2008 Masahiko SAWAI All Rights Reserved. Permission is hereby
 * granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this
 * permission notice shall be included in all copies or substantial portions of
 * the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ************************************************************/
package org.knime.ext.textprocessing.nodes.view.tagcloud.outport.font;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

/**
 * The <code>JFontChooser</code> class is a swing component for font selection.
 * This class has <code>JFileChooser</code> like APIs. The following code pops
 * up a font chooser m_dialog.
 *
 * <pre>
 *   JFontChooser fontChooser = new JFontChooser();
 *   int result = fontChooser.showDialog(parent);
 *   if (result == JFontChooser.OK_OPTION)
 *   {
 *      Font font = fontChooser.getSelectedFont();
 *      System.out.println("Selected Font : " + font);
 * }
 * </pre>
 **/
@SuppressWarnings("rawtypes")
public class JFontChooser extends JComponent {

    private static final long serialVersionUID = -8417346501644932014L;

    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     **/
    public static final int OK_OPTION = 0;

    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     **/
    public static final int CANCEL_OPTION = 1;

    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     **/
    public static final int ERROR_OPTION = -1;

    private static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 10);

    private static final int[] FONT_STYLE_CODES = {Font.PLAIN, Font.BOLD,
        Font.ITALIC, Font.BOLD | Font.ITALIC};

    private static final String[] DEFAULT_FONT_SIZE_STRINGS = {"4", "6", "8",
        "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28",
        "36", "48", "72"};

    private int m_dialogResultValue = ERROR_OPTION;

    private String[] m_fontStyleNames = null;

    private String[] m_fontFamilyNames = null;

    private String[] m_fontSizeStrings = null;

    private JTextField m_fontFamilyTextField = null;

    private JTextField m_fontStyleTextField = null;

    private JTextField m_fontSizeTextField = null;

    private JList m_fontNameList = null;

    private JList m_fontStyleList = null;

    private JList m_fontSizeList = null;

    private JPanel m_fontNamePanel = null;

    private JPanel m_fontStylePanel = null;

    private JPanel m_fontSizePanel = null;

    private JPanel m_samplePanel = null;

    private JTextField m_sampleText = null;

    /**
     * Constructs a <code>JFontChooser</code> object.
     * @param font the default font
     **/
    public JFontChooser(final Font font) {
        this(font, DEFAULT_FONT_SIZE_STRINGS);
    }

    /**
     * Constructs a <code>JFontChooser</code> object using the given font size
     * array.
     * @param font the default font
     * @param fontSizeStrings the array of font size string.
     **/
    public JFontChooser(final Font font, final String[] fontSizeStrings) {
        if (font == null) {
            throw new NullPointerException("font must not be null");
        }
        if (fontSizeStrings == null) {
            throw new NullPointerException("fontSizeStrings must not be null");
        }
        m_fontSizeStrings = fontSizeStrings;
        final JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.add(getFontFamilyPanel());
        selectPanel.add(getFontStylePanel());
        selectPanel.add(getFontSizePanel());

        final JPanel contentsPanel = new JPanel();
        contentsPanel.setLayout(new GridLayout(2, 1));
        contentsPanel.add(selectPanel, BorderLayout.NORTH);
        contentsPanel.add(getSamplePanel(), BorderLayout.CENTER);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(contentsPanel);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setSelectedFont(font);
    }

    private JTextField getFontFamilyTextField() {
        if (m_fontFamilyTextField == null) {
            m_fontFamilyTextField = new JTextField();
            m_fontFamilyTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            m_fontFamilyTextField));
            m_fontFamilyTextField
                    .addKeyListener(
                            new TextFieldKeyHandlerForListSelectionUpDown(
                            getFontFamilyList()));
            m_fontFamilyTextField.getDocument()
                    .addDocumentListener(
                            new ListSearchTextFieldDocumentHandler(
                                    getFontFamilyList()));
            m_fontFamilyTextField.setFont(DEFAULT_FONT);

        }
        return m_fontFamilyTextField;
    }

    private JTextField getFontStyleTextField() {
        if (m_fontStyleTextField == null) {
            m_fontStyleTextField = new JTextField();
            m_fontStyleTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            m_fontStyleTextField));
            m_fontStyleTextField
                    .addKeyListener(
                            new TextFieldKeyHandlerForListSelectionUpDown(
                            getFontStyleList()));
            m_fontStyleTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(getFontStyleList()));
            m_fontStyleTextField.setFont(DEFAULT_FONT);
        }
        return m_fontStyleTextField;
    }

    private JTextField getFontSizeTextField() {
        if (m_fontSizeTextField == null) {
            m_fontSizeTextField = new JTextField();
            m_fontSizeTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            m_fontSizeTextField));
            m_fontSizeTextField
                    .addKeyListener(
                            new TextFieldKeyHandlerForListSelectionUpDown(
                            getFontSizeList()));
            m_fontSizeTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(getFontSizeList()));
            m_fontSizeTextField.setFont(DEFAULT_FONT);
        }
        return m_fontSizeTextField;
    }

    @SuppressWarnings("unchecked")
    private JList getFontFamilyList() {
        if (m_fontNameList == null) {
            m_fontNameList = new JList(getFontFamilies());
            m_fontNameList
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            m_fontNameList.addListSelectionListener(new ListSelectionHandler(
                    getFontFamilyTextField()));
            m_fontNameList.setSelectedIndex(0);
            m_fontNameList.setFont(DEFAULT_FONT);
            m_fontNameList.setFocusable(false);
        }
        return m_fontNameList;
    }

    @SuppressWarnings("unchecked")
    private JList getFontStyleList() {
        if (m_fontStyleList == null) {
            m_fontStyleList = new JList(getFontStyleNames());
            m_fontStyleList
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            m_fontStyleList.addListSelectionListener(new ListSelectionHandler(
                    getFontStyleTextField()));
            m_fontStyleList.setSelectedIndex(0);
            m_fontStyleList.setFont(DEFAULT_FONT);
            m_fontStyleList.setFocusable(false);
        }
        return m_fontStyleList;
    }

    @SuppressWarnings("unchecked")
    private JList getFontSizeList() {
        if (m_fontSizeList == null) {
            m_fontSizeList = new JList(m_fontSizeStrings);
            m_fontSizeList
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            m_fontSizeList.addListSelectionListener(new ListSelectionHandler(
                    getFontSizeTextField()));
            m_fontSizeList.setSelectedIndex(0);
            m_fontSizeList.setFont(DEFAULT_FONT);
            m_fontSizeList.setFocusable(false);
        }
        return m_fontSizeList;
    }

    /**
     * Get the family name of the selected font.
     *
     * @return the font family of the selected font.
     * @see #setSelectedFontFamily
     **/
    public String getSelectedFontFamily() {
        final String fontName = (String)getFontFamilyList().getSelectedValue();
        return fontName;
    }

    /**
     * Get the style of the selected font.
     *
     * @return the style of the selected font. <code>Font.PLAIN</code>,
     *         <code>Font.BOLD</code>, <code>Font.ITALIC</code>,
     *         <code>Font.BOLD|Font.ITALIC</code>
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #setSelectedFontStyle
     **/
    public int getSelectedFontStyle() {
        final int index = getFontStyleList().getSelectedIndex();
        return FONT_STYLE_CODES[index];
    }

    /**
     * Get the size of the selected font.
     *
     * @return the size of the selected font
     * @see #setSelectedFontSize
     **/
    public int getSelectedFontSize() {
        int fontSize = 1;
        String fontSizeString = getFontSizeTextField().getText();
        while (true) {
            try {
                fontSize = Integer.parseInt(fontSizeString);
                break;
            } catch (final NumberFormatException e) {
                fontSizeString = (String)getFontSizeList().getSelectedValue();
                getFontSizeTextField().setText(fontSizeString);
            }
        }

        return fontSize;
    }

    /**
     * Get the selected font.
     *
     * @return the selected font
     * @see #setSelectedFont
     * @see java.awt.Font
     **/
    public Font getSelectedFont() {
        final Font font =
                new Font(getSelectedFontFamily(), getSelectedFontStyle(),
                        getSelectedFontSize());
        return font;
    }

    /**
     * Set the family name of the selected font.
     *
     * @param name the family name of the selected font.
     **/
    public void setSelectedFontFamily(final String name) {
        final String[] names = getFontFamilies();
        for (int i = 0; i < names.length; i++) {
            if (names[i].toLowerCase().equals(name.toLowerCase())) {
                getFontFamilyList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    /**
     * Set the style of the selected font.
     *
     * @param style the size of the selected font. <code>Font.PLAIN</code>,
     *            <code>Font.BOLD</code>, <code>Font.ITALIC</code>, or
     *            <code>Font.BOLD|Font.ITALIC</code>.
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #getSelectedFontStyle
     **/
    public void setSelectedFontStyle(final int style) {
        for (int i = 0; i < FONT_STYLE_CODES.length; i++) {
            if (FONT_STYLE_CODES[i] == style) {
                getFontStyleList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    /**
     * Set the size of the selected font.
     *
     * @param size the size of the selected font
     * @see #getSelectedFontSize
     **/
    public void setSelectedFontSize(final int size) {
        final String sizeString = String.valueOf(size);
        for (int i = 0; i < m_fontSizeStrings.length; i++) {
            if (m_fontSizeStrings[i].equals(sizeString)) {
                getFontSizeList().setSelectedIndex(i);
                break;
            }
        }
        getFontSizeTextField().setText(sizeString);
        updateSampleFont();
    }

    /**
     * Set the selected font.
     *
     * @param font the selected font
     * @see #getSelectedFont
     * @see java.awt.Font
     **/
    public void setSelectedFont(final Font font) {
        setSelectedFontFamily(font.getFamily());
        setSelectedFontStyle(font.getStyle());
        setSelectedFontSize(font.getSize());
    }

    /**
     * Show font selection m_dialog.
     *
     * @param parent Dialog's Parent component.
     * @return OK_OPTION, CANCEL_OPTION or ERROR_OPTION
     * @see #OK_OPTION
     * @see #CANCEL_OPTION
     * @see #ERROR_OPTION
     **/
    public int showDialog(final Component parent) {
        m_dialogResultValue = ERROR_OPTION;
        JDialog dialog = createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                m_dialogResultValue = CANCEL_OPTION;
            }
        });

        dialog.setVisible(true);
        dialog.dispose();
        dialog = null;

        return m_dialogResultValue;
    }

    private class ListSelectionHandler implements ListSelectionListener {

        private final JTextComponent m_textComponent;

        ListSelectionHandler(final JTextComponent textComponent) {
            m_textComponent = textComponent;
        }

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                final JList list = (JList)e.getSource();
                final String selectedValue = (String)list.getSelectedValue();

                final String oldValue = m_textComponent.getText();
                m_textComponent.setText(selectedValue);
                if (!oldValue.equalsIgnoreCase(selectedValue)) {
                    m_textComponent.selectAll();
                    m_textComponent.requestFocus();
                }
                updateSampleFont();
            }
        }
    }

    private class TextFieldFocusHandlerForTextSelection extends FocusAdapter {

        private final JTextComponent m_textComponent;

        public TextFieldFocusHandlerForTextSelection(
                final JTextComponent textComponent) {
            m_textComponent = textComponent;
        }

        @Override
        public void focusGained(final FocusEvent e) {
            m_textComponent.selectAll();
        }

        @Override
        public void focusLost(final FocusEvent e) {
            m_textComponent.select(0, 0);
            updateSampleFont();
        }
    }

    private class TextFieldKeyHandlerForListSelectionUpDown extends
        KeyAdapter {

        private final JList m_targetList;

        public TextFieldKeyHandlerForListSelectionUpDown(final JList list) {
            m_targetList = list;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            int i = m_targetList.getSelectedIndex();
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                i = m_targetList.getSelectedIndex() - 1;
                if (i < 0) {
                    i = 0;
                }
                m_targetList.setSelectedIndex(i);
                break;
            case KeyEvent.VK_DOWN:
                final int listSize = m_targetList.getModel().getSize();
                i = m_targetList.getSelectedIndex() + 1;
                if (i >= listSize) {
                    i = listSize - 1;
                }
                m_targetList.setSelectedIndex(i);
                break;
            default:
                break;
            }
        }
    }

    private class ListSearchTextFieldDocumentHandler implements
            DocumentListener {

        private final JList m_targetList;

        public ListSearchTextFieldDocumentHandler(final JList targetList) {
            m_targetList = targetList;
        }
        @Override
        public void insertUpdate(final DocumentEvent e) {
            update(e);
        }
        @Override
        public void removeUpdate(final DocumentEvent e) {
            update(e);
        }
        @Override
        public void changedUpdate(final DocumentEvent e) {
            update(e);
        }
        private void update(final DocumentEvent event) {
            String newValue = "";
            try {
                final Document doc = event.getDocument();
                newValue = doc.getText(0, doc.getLength());
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }

            if (newValue.length() > 0) {
                int index =
                        m_targetList.getNextMatch(newValue, 0,
                                Position.Bias.Forward);
                if (index < 0) {
                    index = 0;
                }
                m_targetList.ensureIndexIsVisible(index);

                final String matchedName =
                        m_targetList.getModel().getElementAt(index).toString();
                if (newValue.equalsIgnoreCase(matchedName)) {
                    if (index != m_targetList.getSelectedIndex()) {
                        SwingUtilities.invokeLater(new ListSelector(index));
                    }
                }
            }
        }
        public class ListSelector implements Runnable {

            private final int m_index;

            public ListSelector(final int index) {
                m_index = index;
            }

            @Override
            public void run() {
                m_targetList.setSelectedIndex(m_index);
            }
        }
    }

    private class DialogOKAction extends AbstractAction {

        private static final long serialVersionUID = -3699254666187542959L;

        protected static final String ACTION_NAME = "OK";

        private final JDialog m_dialog;

        protected DialogOKAction(final JDialog dialog) {
            m_dialog = dialog;
            putValue(Action.DEFAULT, ACTION_NAME);
            putValue(Action.ACTION_COMMAND_KEY, ACTION_NAME);
            putValue(Action.NAME, "OK");
        }
        @Override
        public void actionPerformed(final ActionEvent e) {
            m_dialogResultValue = OK_OPTION;
            m_dialog.setVisible(false);
        }
    }

    private class DialogCancelAction extends AbstractAction {

        private static final long serialVersionUID = -4324175976794012874L;

        protected static final String ACTION_NAME = "Cancel";

        private final JDialog m_dialog;

        protected DialogCancelAction(final JDialog dialog) {
            m_dialog = dialog;
            putValue(Action.DEFAULT, ACTION_NAME);
            putValue(Action.ACTION_COMMAND_KEY, ACTION_NAME);
            putValue(Action.NAME, "Cancel");
        }
        @Override
        public void actionPerformed(final ActionEvent e) {
            m_dialogResultValue = CANCEL_OPTION;
            m_dialog.setVisible(false);
        }
    }

    private JDialog createDialog(final Component parent) {
        final Frame frame =
                parent instanceof Frame ? (Frame)parent : (Frame)SwingUtilities
                        .getAncestorOfClass(Frame.class, parent);
        final JDialog dialog = new JDialog(frame, "Select font", true);

        final Action okAction = new DialogOKAction(dialog);
        final Action cancelAction = new DialogCancelAction(dialog);

        final JButton okButton = new JButton(okAction);
        okButton.setFont(DEFAULT_FONT);
        okButton.setMinimumSize(okButton.getPreferredSize());
        okButton.setMaximumSize(okButton.getPreferredSize());

        final JButton cancelButton = new JButton(cancelAction);
        cancelButton.setFont(DEFAULT_FONT);
        cancelButton.setMinimumSize(cancelButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 10, 0, 0);
        buttonsPanel.add(okButton, gc);
        gc.gridx = 1;
        buttonsPanel.add(cancelButton, gc);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 10));

        final ActionMap actionMap = buttonsPanel.getActionMap();
        actionMap.put(cancelAction.getValue(Action.DEFAULT), cancelAction);
        actionMap.put(okAction.getValue(Action.DEFAULT), okAction);
        final InputMap inputMap =
                buttonsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"),
                cancelAction.getValue(Action.DEFAULT));
        inputMap.put(KeyStroke.getKeyStroke("ENTER"),
                okAction.getValue(Action.DEFAULT));

        final JPanel dialogSouthPanel = new JPanel();
        dialogSouthPanel.setLayout(new BorderLayout());
        dialogSouthPanel.add(buttonsPanel, BorderLayout.NORTH);

        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.getContentPane().add(dialogSouthPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    private void updateSampleFont() {
        final Font font = getSelectedFont();
        getSampleTextField().setFont(font);
    }

    private JPanel getFontFamilyPanel() {
        if (m_fontNamePanel == null) {
            m_fontNamePanel = new JPanel();
            m_fontNamePanel.setLayout(new BorderLayout());
            m_fontNamePanel.setBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5));
            m_fontNamePanel.setPreferredSize(new Dimension(180, 130));

            final JScrollPane scrollPane = new JScrollPane(getFontFamilyList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontFamilyTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel("Name:");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontFamilyTextField());
            label.setDisplayedMnemonic('F');

            m_fontNamePanel.add(label, BorderLayout.NORTH);
            m_fontNamePanel.add(p, BorderLayout.CENTER);

        }
        return m_fontNamePanel;
    }

    private JPanel getFontStylePanel() {
        if (m_fontStylePanel == null) {
            m_fontStylePanel = new JPanel();
            m_fontStylePanel.setLayout(new BorderLayout());
            m_fontStylePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                    5));
            m_fontStylePanel.setPreferredSize(new Dimension(140, 130));

            final JScrollPane scrollPane = new JScrollPane(getFontStyleList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontStyleTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel("Effects");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontStyleTextField());
            label.setDisplayedMnemonic('Y');

            m_fontStylePanel.add(label, BorderLayout.NORTH);
            m_fontStylePanel.add(p, BorderLayout.CENTER);
        }
        return m_fontStylePanel;
    }

    private JPanel getFontSizePanel() {
        if (m_fontSizePanel == null) {
            m_fontSizePanel = new JPanel();
            m_fontSizePanel.setLayout(new BorderLayout());
            m_fontSizePanel.setPreferredSize(new Dimension(70, 130));
            m_fontSizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                    5));

            final JScrollPane scrollPane = new JScrollPane(getFontSizeList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontSizeTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel("Size");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontSizeTextField());
            label.setDisplayedMnemonic('S');

            m_fontSizePanel.add(label, BorderLayout.NORTH);
            m_fontSizePanel.add(p, BorderLayout.CENTER);
        }
        return m_fontSizePanel;
    }

    private JPanel getSamplePanel() {
        if (m_samplePanel == null) {
            final Border titledBorder =
                    BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(), "Preview");
            final Border empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
            final Border border =
                    BorderFactory.createCompoundBorder(titledBorder, empty);

            m_samplePanel = new JPanel();
            m_samplePanel.setLayout(new BorderLayout());
            m_samplePanel.setBorder(border);

            m_samplePanel.add(getSampleTextField(), BorderLayout.CENTER);
        }
        return m_samplePanel;
    }

    private JTextField getSampleTextField() {
        if (m_sampleText == null) {
            final Border lowered = BorderFactory.createLoweredBevelBorder();
            m_sampleText = new JTextField("Preview Font");
            m_sampleText.setBorder(lowered);
            m_sampleText.setPreferredSize(new Dimension(300, 50));
        }
        return m_sampleText;
    }

    private String[] getFontFamilies() {
        if (m_fontFamilyNames == null) {
            final GraphicsEnvironment env =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            m_fontFamilyNames = env.getAvailableFontFamilyNames();
        }
        return m_fontFamilyNames;
    }

    private String[] getFontStyleNames() {
        if (m_fontStyleNames == null) {
            int i = 0;
            m_fontStyleNames = new String[4];
            m_fontStyleNames[i++] = "Plain";
            m_fontStyleNames[i++] = "Bold";
            m_fontStyleNames[i++] = "Italic";
            m_fontStyleNames[i++] = "BoldItalic";
        }
        return m_fontStyleNames;
    }
}

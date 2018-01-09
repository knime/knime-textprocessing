/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 * -------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.cooccurrencecounter;

import org.knime.core.node.util.ButtonGroupEnumInterface;


/**
 * Distinct sections that are considered during co-occurrence analysis.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public enum CooccurrenceLevel implements ButtonGroupEnumInterface {

    /**Document section.*/
    DOCUMENT("Document",
        "Compute co-occurrences on the document level (incl. section level)", 6,
            false),
    /**Section section.*/
    SECTION("Section",
        "Compute co-occurrences on the section level (incl. paragraph level)",
            5, false),
    /**Paragraph section.*/
    PARAGRAPH("Paragraph",
        "Compute co-occurrences on the paragraph level (incl. sentences level)",
            4, false),
    /**Sentence section.*/
    SENTENCE("Sentence",
        "Compute co-occurrences on the sentence level (incl. neighbor level)",
            3, true),
    /**Neighbor level.*/
    NEIGHBOR("Neighbor",
        "Compute neighbor co-occurrences  (incl. title section)",
            2, false),
    /**Title section.*/
    TITLE("Title", "Compute co-occurrences only for the title section", 1,
            false);


    private final String m_text;
    private final String m_tooltip;
    private final boolean m_isDefault;
    private final int m_level;

    /**Constructor for class CooccurrenceLevel.
     * @param text the text to display to the user
     * @param tooltip the tool tip for the user
     * @param level the level of detail. Higher levels include lower levels.
     * @param isDefault <code>true</code> if this is the default selection.
     */
    private CooccurrenceLevel(final String text, final String tooltip,
            final int level, final boolean isDefault) {
        m_text = text;
        m_tooltip = tooltip;
        m_level = level;
        m_isDefault = isDefault;
    }

    /**
     * @return the level. Higher level include lower levels.
     */
    public int getLevel() {
        return m_level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return m_text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActionCommand() {
        return name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTip() {
        return m_tooltip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefault() {
        return m_isDefault;
    }

    /**
     * @return the default {@link CooccurrenceLevel}
     */
    public static CooccurrenceLevel getDefault() {
        for (final CooccurrenceLevel level : values()) {
            if (level.isDefault()) {
                return level;
            }
        }
        throw new IllegalStateException(
                "Implementation error: No default co-occurrence level defined");
    }

    /**
     * @param actionCommand the action command to get the co-occurrence level
     * for
     * @return the {@link CooccurrenceLevel}
     */
    public static CooccurrenceLevel getCooccurrenceLevel(
            final String actionCommand) {
        return CooccurrenceLevel.valueOf(actionCommand);
    }
}

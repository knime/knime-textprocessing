/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * Created on 06.07.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.util;

import java.util.List;

import org.knime.ext.textprocessing.data.TextContainer;

/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public final class TextContainers {

    private TextContainers() { }

    /**
     * Creates a string representation for the text contained in the given text containers. The string contains no
     * trailing whitespaces and consists of the concatenated text of the containers. On each container
     * {@link TextContainer#getTextWithWsSuffix()} is called except for the last container
     * {@link TextContainer#getText()} is called and the returned text is concatenated.
     * @param textContainers the list of text containers to create a string representation for.
     * @return the concatenated text as string.
     */
    public static final String getText(final List<? extends TextContainer> textContainers) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (TextContainer tc : textContainers) {
            if (i < textContainers.size() - 1) {
                sb.append(tc.getTextWithWsSuffix());
            } else {
                sb.append(tc.getText());
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Creates a string representation for the text contained in the given text containers. The string may contain
     * trailing whitespaces and consists of the concatenated full text of the containers. On each container
     * {@link TextContainer#getTextWithWsSuffix()} is called and the returned text is concatenated.
     * @param textContainers the list of text containers to create a string representation for.
     * @return the concatenated text as string.
     */
    public static final String getTextWithWsSuffix(final List<? extends TextContainer> textContainers) {
        StringBuilder sb = new StringBuilder();
        for (TextContainer tc : textContainers) {
            sb.append(tc.getTextWithWsSuffix());
        }
        return sb.toString();
    }
}

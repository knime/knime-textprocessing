/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   02.09.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public interface TokenizerFactory {

    /**
     * @return Returns the Tokenizer.
     */
    public Tokenizer getTokenizer();

    /**
     * @return Returns the name of the Tokenizer.
     */
    public String getTokenizerName();

    /**
     * @return Returns the description of the Tokenizer.
     */
    public String getTokenizerDescription();

    /**
     * @return Returns the URL of the tokenizer info page.
     * @since 3.3
     */
    public String getTokenizerDescLink();

    /**
     * Returns the maximum pool size for the {@link TokenizerPool}.
     * Default maximum value provided by the interface is 10.
     * To use the maximum value, the specific implementation of {@link TokenizerFactory} has to
     * override {@link #forceMaxPoolSize()} and set the return value to {@code true}.
     * To set a different maximum pool size, the specific implementation of {@link TokenizerFactory} has to
     * override this method.
     *
     * @return Returns the maximum pool size for the specific tokenizer.
     * @since 3.4
     */
    public default int getMaxPoolSize() {
        return 10;
    }

    /**
     * Returns if the maximum pool size should be used for the {@link TokenizerPool}.
     * Default value provided by the interface is false. Override this method in the specific implementation
     * of {@link TokenizerFactory} if the maximum pool size should be used, no matter what pool size
     * is defined at the preference page.
     *
     * @return Returns if the maximum pool size should be used for the {@link TokenizerPool}.
     * @since 3.4
     */
    public default boolean forceMaxPoolSize() {
        return false;
    }
}

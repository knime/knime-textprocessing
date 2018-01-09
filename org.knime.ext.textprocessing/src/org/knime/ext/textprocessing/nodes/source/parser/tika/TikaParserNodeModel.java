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
 * ---------------------------------------------------------------------
 *
 * History
 *   08.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.util.FileUtil;

/**
 * The node model of the Tika Parser node. This model extends
 * {@link org.knime.ext.textprocessing.nodes.source.parser.tika.AbstractTikaNodeModel} and is streamable.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
final class TikaParserNodeModel extends AbstractTikaNodeModel {

    private SettingsModelString m_pathModel = TikaParserConfig.getPathModel();

    private SettingsModelBoolean m_recursiveModel = TikaParserConfig.getRecursiveModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel = TikaParserConfig.getIgnoreHiddenFilesModel();

    /**
     * Creates a new instance of {@code TikaParserNodeModel}
     */
    TikaParserNodeModel() {
        super(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInputSettingsTo(final NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
        m_ignoreHiddenFilesModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateInputSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedInputSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
        m_ignoreHiddenFilesModel.loadSettingsFrom(settings);
    }

    private boolean filterExt(final List<String> exts, final URL s) {
        final String fileExt = FilenameUtils.getExtension(s.getFile());
        for (final String ext : exts) {
            if (fileExt.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterLocalFiles(final URL s) {
        try {
            final File file = FileUtil.getFileFromURL(s);
            if (file != null) {
                if (m_ignoreHiddenFilesModel.getBooleanValue() && file.isHidden()) {
                    return false;
                }
                if (!file.isFile() || file.isDirectory()) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidSettingsException
     */
    @Override
    protected Iterable<URL> readInput(final RowInput input) throws InvalidSettingsException {
        boolean ext = getTypesModel().getStringValue().equals(TikaParserConfig.EXT_TYPE);
        boolean recursive = m_recursiveModel.getBooleanValue();
        try {
            URL url = FileUtil.toURL(m_pathModel.getStringValue());
            Path localPath = FileUtil.resolveToPath(url);
            if (localPath == null && !url.getProtocol().equalsIgnoreCase("knime")) {
                throw new InvalidSettingsException("Reading from remote directories is not supported.");
            }
            return ext ? FileUtil.listFiles(url,
                u -> filterExt(getFilterModel().getIncludeList(), u) && filterLocalFiles(u), recursive)
                : FileUtil.listFiles(url, u -> filterLocalFiles(u), recursive);
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSettingsException("Directory path cannot be accessed!");
        }
    }

}

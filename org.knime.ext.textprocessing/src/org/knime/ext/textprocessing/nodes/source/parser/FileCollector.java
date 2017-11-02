/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
========================================================================
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
 * -------------------------------------------------------------------
 *
 * History
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides functionality to search a specified directory for files with given extensions. The directory can be searched
 * recursively or not.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class FileCollector {

    private final File m_directory;

    private final List<String> m_extensions;

    private final boolean m_recursive;

    private final boolean m_ignoreHidden;

    private List<File> m_files;

    /**
     * Creates a new instance of <code>FileCollector</code> with given directory, to search for file with given
     * extensions. If <code>recursive</code> is set <code>true</code> the directory will be searched recursively which
     * means that subdirectories will by searched too.
     *
     * @param dir Directory to search for files.
     * @param ext Extensions of file to search for.
     * @param recursive if set <code>true</code> the directory will be
     * @param ignoreHiddenFiles if set <code>true</code> hidden files will not be collected. searched recursively.
     */
    public FileCollector(final File dir, final List<String> ext, final boolean recursive,
        final boolean ignoreHiddenFiles) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getName() + " is not a directory ");
        }

        m_directory = dir;
        m_extensions = ext;
        m_recursive = recursive;
        m_ignoreHidden = ignoreHiddenFiles;

        m_files = new ArrayList<File>();
        collectFiles(m_directory, new FileFilter());
    }

    /**
     * Returns a list with collected files.
     *
     * @return A list with collected files.
     */
    public List<File> getFiles() {
        return Collections.unmodifiableList(m_files);
    }

    /**
     * @return Returns the directory to search in.
     */
    public File getDirectory() {
        return m_directory;
    }

    /**
     * @return Returns the list of file extensions to search for.
     */
    public List<String> getExtensions() {
        return m_extensions;
    }

    /**
     * @return the recursive flag.
     */
    public boolean getRecursive() {
        return m_recursive;
    }

    /**
     * @return the ignore hidden files flag.
     */
    public boolean getIgnoreHidden() {
        return m_ignoreHidden;
    }

    private void collectFiles(final File dir, final FileFilter filter) {
        if (dir.isDirectory()) {
            // ad files to list
            final File[] filesWithExt = dir.listFiles(filter);

            // check for hidden files
            if (m_ignoreHidden) {
                for (final File f : filesWithExt) {
                    if (!f.isHidden()) {
                        m_files.add(f);
                    }
                }
            } else {
                m_files.addAll(Arrays.asList(filesWithExt));
            }

            // go recursively through all sub dirs
            if (m_recursive) {
                final File[] dirs = dir.listFiles();
                for (final File s : dirs) {
                    if (s.isDirectory()) {
                        collectFiles(s, filter);
                    }
                }
            }
        }
    }

    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    class FileFilter implements FilenameFilter {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final File f, final String s) {
            if (m_extensions.size() == 0) {
                return true;
            }
            for (final String ext : m_extensions) {
                if (s.toLowerCase().endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    }
}

/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
========================================================================
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

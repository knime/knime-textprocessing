/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
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
 * Provides functionality to search a specified directory for files with
 * given extensions. The directory can be searched recursively or not.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FileCollector {

    private File m_directory;
    
    private List<String> m_extensions;
    
    private boolean m_recursive;
    
    private List<File> m_files;
    
    /**
     * Creates a new instance of <code>FileCollector</code> with given 
     * directory, to search for file with given extensions. If 
     * <code>recursive</code> is set <code>true</code> the directory will be
     * searched recursively which means that subdirectories will by searched 
     * too.
     * @param dir Directory to search for files.
     * @param ext Extensions of file to search for.
     * @param recursive if set <code>true</code> the directory will be
     * searched recursively.
     */
    public FileCollector(final File dir, final List<String> ext, 
            final boolean recursive) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getName()
                    + " is not a directory ");
        }
        
        m_directory = dir;
        m_extensions = ext;
        m_recursive = recursive;
        
        collectFiles();
    }
    
    /**
     * Returns a list with collected files.
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
     * Collects all the files in directory with given extension.
     */
    private void collectFiles() {
        m_files = new ArrayList<File>();
        recCollectFiles(m_files, m_directory, new FileFilter());
    }
    
    private void recCollectFiles(final List<File> files, final File dir, 
            final FileFilter filter) {
        if (dir.isDirectory()) {
            // ad files to list
            File[] filesWithExt = dir.listFiles(filter);
            files.addAll(Arrays.asList(filesWithExt));
            
            // go recursively through all sub dirs
            if (m_recursive) {
                String[] dirs = dir.list();
                for (String s : dirs) {
                    File f = new File(s);
                    if (f.isDirectory()) {
                        recCollectFiles(files, f, filter);
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
        public boolean accept(final File f, final String s) {
            if (m_extensions.size() == 0) {
                return true;
            }
            
            for (String ext : m_extensions) {
                if (s.toLowerCase().endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    }
}

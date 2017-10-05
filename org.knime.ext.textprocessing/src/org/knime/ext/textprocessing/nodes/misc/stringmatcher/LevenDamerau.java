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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.TermValue;

/**
 * Calculating the Levenshtein-Damerau-distance.
 *
 * The LD-distance is a function to compare two words. There are four possible edit operations. One letter can be
 * deleted, one can be inserted, one can be changed into another or two of them can be swapped. To disable one of the
 * operations you can set them to zero
 *
 * Each operation can be given an individual weight. But mind, because of the optimal algorithm, it is necessary that
 * the weight of 2 switch operations must be equal or larger than the sum of the weight of an insert and a deletion.
 *
 * The method getNearestWord(word) gives a list of all words in the biblist which have the smallest distance to the
 * origin word (this can be more than one)
 *
 * Using the method calculate the distance of the two given strings is returned.
 *
 * @author Iris Adae, University of Konstanz
 *
 */
public class LevenDamerau {

    /** Variable for the cost of one delete-operation. */
    private int m_wd;

    /** Variable for the cost of one insert-operation. */
    private int m_wi;

    /** Variable for the cost of one change-operation. */
    private int m_wc;

    /** Variable for the cost of one switch-operation. */
    private int m_ws;

    /**
     * 1 if the class works with an sortedlist 2 if the class works with the datatable 0 otherwise .
     */
    private int m_sorted;

    /** Variable for the last found minimal distance. */
    private int m_lastdist;

    /** stores the Datatable given, to search words. */
    private DataTable m_bibdata;

    /** the desired colum in the datatable. */
    private int m_bibcol;

    /** if the list is presorted. */
    private ArrayList<ArrayList<char[]>> m_sortedbiblist;

    /** to give notice of the progress. */
    private ExecutionContext m_exec;

    /**
     * standard constructor. initializes all weights with 1
     *
     */
    public LevenDamerau() {
        m_wd = 1;
        m_wi = 1;
        m_wc = 1;
        m_ws = 1;
        m_sorted = 0;
        m_lastdist = Integer.MAX_VALUE;
    }

    /**
     * Constructor. Use this constructor to initialize the class you can get a list of the nearest words in the list to
     * a give one by the method getnearest word.
     *
     * @param data the datatable which includes the word list in which should be searched
     * @param col the column of the datatable
     * @param workincache 1 if the user wishes to work in cache, it is much faster but can cause a
     *            java.lang.OutOfMemoryError
     * @param exec Executioncontext
     * @throws CanceledExecutionException if canceled by user
     */
    public LevenDamerau(final DataTable data, final int col, final boolean workincache, final ExecutionContext exec)
        throws CanceledExecutionException {
        m_wd = 1;
        m_wi = 1;
        m_wc = 1;
        m_ws = 1;
        m_exec = exec;
        m_lastdist = Integer.MAX_VALUE;

        if (workincache) {
            m_sortedbiblist = sort(data, col);
            m_sorted = 1;
        } else {
            m_bibdata = data;
            m_bibcol = col;
            m_sorted = 2;
        }
    }

    /**
     *
     * @return the last found minimal distance
     */
    public int getlastdistance() {
        return m_lastdist;
    }

    /**
     * sets the weight for deleting one letter.
     *
     * @param wd the int weight for one deletion
     */
    public void setweightdelete(final int wd) {
        m_wd = wd;
    }

    /**
     * sets the weight for inserting one letter.
     *
     * @param wi the int weight for one insertion
     */
    public void setweightinsert(final int wi) {
        m_wi = wi;
    }

    /**
     * sets the weight for changing one letter into another.
     *
     * @param wc the int weight for one change
     */
    public void setweightchange(final int wc) {
        m_wc = wc;
    }

    /**
     * sets the weight for switching two letters.
     *
     * @param ws the int weight for one switch
     */
    public void setweightswitch(final int ws) {
        m_ws = ws;
    }

    /**
     * sets the weights for all operation.
     *
     * @param wd the int weight for one deletion
     * @param wi the int weight for one insertion
     * @param wc the int weight for one change
     * @param ws the int weight for one switch
     */
    public void setweight(final int wd, final int wi, final int wc, final int ws) {
        m_wd = wd;
        m_wi = wi;
        m_wc = wc;
        m_ws = ws;
    }

    /**
     * minimumfunction for 4 integers.
     *
     * @param x1 first int value
     * @param x2 second int value
     * @param x3 third int value
     * @param x4 fourth int value
     * @return min the minimum of the inputs
     */
    private int min(final int x1, final int x2, final int x3, final int x4) {
        int min = x1;
        if (x2 < min) {
            min = x2;
        }
        if (x3 < min) {
            min = x3;
        }
        if (x4 < min) {
            return x4;
        }
        return min;
    }

    /**
     * Levenshtein-Damerau distance.
     *
     * @param string1 the first string
     * @param string2 the second string
     * @return the distance of the two strings
     */
    public int calculate(final String string1, final String string2) {
        return calculate(string1.toCharArray(), string2.toCharArray());
    }

    /**
     * Levenshtein-Damerau distance if smaller as k.
     *
     * @param string1 the first string
     * @param string2 the second string
     * @param k the maximal value for the distance
     * @return the minimum of k+1 and the Levenshtein-Damerau distance of the two strings
     */
    public int calculate(final String string1, final String string2, final int k) {
        return calculate(string1.toCharArray(), string2.toCharArray(), k);
    }

    /**
     * Levenshtein-Damerau distance.
     *
     * @param wordfromchar the first string
     * @param wordtochar the second string
     * @return the distance of the two strings
     */
    private int calculate(final char[] wordfromchar, final char[] wordtochar) {

        int max = 0;
        for (char c : wordfromchar) {
            max = c > max ? c : max;
        }
        for (char c : wordtochar) {
            max = c > max ? c : max;
        }
        //         LOGGER.info(max++);
        max++;
        int n = wordfromchar.length;
        int m = wordtochar.length;
        int[][] h = new int[n + 2][m + 2]; // matrix for the
        int[] da = new int[max]; // last position of letter * is stored
        // in da[*]necessary to calculate the switch
        int db, i1, j1, d;
        int inf = n * m_wd + m * m_wi + 1; // maximal distance (delete all  letters and insert all other)

        h[0][0] = inf; // initializes the matrix
        for (int i = 0; i <= n; i++) {
            h[i + 1][1] = i * m_wd;
            h[i + 1][0] = inf;
        }
        for (int j = 0; j <= m; j++) {
            h[1][j + 1] = j * m_wi;
            h[0][j + 1] = inf;
        }
        for (char c = 0; c < max; c++) {
            da[c] = 0;
        }

        for (int i = 1; i <= n; i++) {
            db = 0;
            for (int j = 1; j <= m; j++) {
                //TODO find a better way to get rid of chars above 256
                // okay lets try to use max.
                i1 = da[wordtochar[j - 1]];
                j1 = db;
                if (wordfromchar[i - 1] == wordtochar[j - 1]) {
                    d = 0;
                    db = j;
                } else {
                    d = m_wc;
                }
                h[i + 1][j + 1] = min(h[i][j] + d, // if its cheapest to change  the letter or do nothing
                    h[i + 1][j] + m_wi, // if its cheapest to insert the letter
                    h[i][j + 1] + m_wd, // if its cheapest to delete the letter
                    h[i1][j1] + (i - i1 - 1) // if its cheapest to delete all letters between(origin
                                             // word), then swap, and than  insert the other
                                             // letters(searched word)
                        * m_wd + m_ws + (j - j1 - 1) * m_wi);
            }
            da[wordfromchar[(i - 1)]] = i; // stores the position of the last letter
        }
        return h[n + 1][m + 1]; // the minimal distance
    }

    /**
     * Levenshtein-Damerau distance if smaller as k.
     *
     * @param wordfromchar the first string
     * @param wordtochar the second string
     * @param k the maximal value for the distance
     * @return the minimum of k+1 and the Levenshtein-Damerau distance of the two strings
     */
    private int calculate(final char[] wordfromchar, final char[] wordtochar, final int k) {
        int n = wordfromchar.length;
        int m = wordtochar.length;

        /**
         * if wordfromchar is shorter then word tochar the distance is at least the difference * m_wi
         */
        if (n < m && k < (m - n) * m_wi) {
            return k + 1;
        }

        /**
         * if wordfromchar is longer then word tochar the distance is at least the difference * m_wd
         */
        if (m < n && k < (n - m) * m_wd) {
            return k + 1;
        }

        /**
         * otherwise we use the standard algorithm
         */
        return calculate(wordfromchar, wordtochar);
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance the method uses the datatable given in the
     * initialization
     *
     * @param word the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z if the list were set, otherwise NULL is
     *         returned
     */
    public ArrayList<char[]> getNearestWord(final char[] word) {
        if (m_sorted == 1) {
            return getNearestWord(this.m_sortedbiblist, word);
        } else if (m_sorted == 2) {
            return getNearestWord(this.m_bibdata, this.m_bibcol, word);
        } else {
            return null;
        }
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance
     *
     * @param bibdata an Datatable which contains an column of strings
     * @param bibcol the column of the Datatable, which contains the strings
     *
     * @param word the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z
     */
    private ArrayList<char[]> getNearestWord(final DataTable bibdata, final int bibcol, final char[] word) {
        int k = Integer.MAX_VALUE;

        ArrayList<char[]> nearest = new ArrayList<char[]>(3);

        for (DataRow row : bibdata) {
            DataCell cell = row.getCell(bibcol);
            String cellString;
            if (!cell.isMissing() && cell.getType().isCompatible(TermValue.class)) {
                cellString = ((TermValue)cell).getTermValue().getText();
            } else {
                cellString = cell.toString();
            }
            if (!cell.isMissing()) {
                char[] t = cellString.toCharArray();
                int kneu = calculate(word, t, k);
                if (kneu == k) {
                    nearest.add(t);
                } else if (kneu < k) {
                    nearest.clear();
                    nearest.add(t);
                    k = kneu;
                    // if (k == 0) { return nearest; }
                }
            }
        }
        m_lastdist = k;
        return nearest;
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance
     *
     * @param t an by length sorted ArrayList (like the method sort produces)
     * @param z the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z
     */
    private ArrayList<char[]> getNearestWord(final ArrayList<ArrayList<char[]>> t, final char[] z) {
        if (t.isEmpty()) {
            m_lastdist = Integer.MAX_VALUE;
            return new ArrayList<char[]>();
        }
        int m = z.length;
        ArrayList<char[]> nearest = new ArrayList<char[]>(3);
        int k = m + t.size() + 1;
        for (int i = 1; i <= 2 * m + 1; i++) {
            int n = m + (int)Math.pow(-1, i) * Math.abs(i / 2);
            if (n >= 0 && n < t.size()) {
                /**
                 * if wordfromchar is shorter then word tochar the distance is at least the difference * m_wi if
                 * wordfromchar is longer then word tochar the distance is at least the difference * m_wd
                 */
                if (!(m < n + 1 && k < (m - 1 - n) * m_wi) && !(m < n + 1 && k < (n + 1 - m) * m_wd)) {
                    for (int j = 0; j < t.get(n).size(); j++) {
                        int kneu = calculate(t.get(n).get(j), z);
                        if (kneu == k) {
                            nearest.add(t.get(n).get(j));
                        } else if (kneu < k) {
                            nearest.clear();
                            nearest.add(t.get(n).get(j));
                            k = kneu;
                            // if (k == 0) { return nearest; }
                        }
                    }
                }
            }
        }
        m_lastdist = k;
        return nearest;
    }

    /**
     * sorts one column of the table by stringlength.
     *
     * @param data a DataTable which contains Strings
     * @param col the column of the given DataTable, which you like to sort
     * @return an Arraylist with the following characteristics at the i. position of the list you find an list with all
     *         words of length i
     * @throws CanceledExecutionException
     *
     */
    private ArrayList<ArrayList<char[]>> sort(final DataTable data, final int col) throws CanceledExecutionException {

        int maxlength = 0;
        ArrayList<ArrayList<char[]>> t = new ArrayList<ArrayList<char[]>>(16);

        for (DataRow row : data) {
            m_exec.checkCanceled();

            DataCell cell = row.getCell(col);
            String cellString;
            if (!cell.isMissing() && cell.getType().isCompatible(TermValue.class)) {
                cellString = ((TermValue)cell).getTermValue().getText();
            } else {
                cellString = cell.toString();
            }
            if (!cell.isMissing()) {
                char[] b = cellString.toCharArray();
                int l = b.length;
                while (l >= maxlength) {
                    t.add(new ArrayList<char[]>(100));
                    maxlength++;
                }
                t.get(l).add(b);
            }

        }
        return t;
    }
}

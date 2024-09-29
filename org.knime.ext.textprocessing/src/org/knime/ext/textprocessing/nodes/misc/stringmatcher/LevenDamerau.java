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
    private int m_deleteCost;

    /** Variable for the cost of one insert-operation. */
    private int m_insertCost;

    /** Variable for the cost of one change-operation. */
    private int m_changeCost;

    /** Variable for the cost of one switch-operation. */
    private int m_switchCost;

    /**
     * 1 if the class works with an sortedlist 2 if the class works with the datatable 0 otherwise .
     */
    private int m_sorted;

    /** Variable for the last found minimal distance. */
    private int m_lastDist;

    /** stores the Datatable given, to search words. */
    private DataTable m_dictData;

    /** the desired colum in the datatable. */
    private int m_dictCol;

    /** if the list is presorted. */
    private ArrayList<ArrayList<char[]>> m_groupedDictList;

    /** to give notice of the progress. */
    private ExecutionContext m_exec;

    /**
     * standard constructor. initializes all weights with 1
     *
     */
    public LevenDamerau() {
        m_deleteCost = 1;
        m_insertCost = 1;
        m_changeCost = 1;
        m_switchCost = 1;
        m_sorted = 0;
        m_lastDist = Integer.MAX_VALUE;
    }

    /**
     * Constructor. Use this constructor to initialize the class you can get a list of the nearest words in the list to
     * a give one by the method getnearest word.
     *
     * @param dictData the datatable which includes the word list in which should be searched
     * @param dictCol the column of the datatable
     * @param cacheDictInMemory 1 if the user wishes to work in cache, it is much faster but can cause a
     *            java.lang.OutOfMemoryError
     * @param exec Executioncontext
     * @throws CanceledExecutionException if canceled by user
     */
    public LevenDamerau(final DataTable dictData, final int dictCol, final boolean cacheDictInMemory,
            final ExecutionContext exec) throws CanceledExecutionException {
        m_deleteCost = 1;
        m_insertCost = 1;
        m_changeCost = 1;
        m_switchCost = 1;
        m_exec = exec;
        m_lastDist = Integer.MAX_VALUE;

        if (cacheDictInMemory) {
            m_groupedDictList = groupByLength(dictData, dictCol);
            m_sorted = 1;
        } else {
            m_dictData = dictData;
            m_dictCol = dictCol;
            m_sorted = 2;
        }
    }

    /**
     *
     * @return the last found minimal distance
     */
    public int getLastDistance() {
        return m_lastDist;
    }

    /**
     * sets the weight for deleting one letter.
     *
     * @param wd the int weight for one deletion
     */
    public void setWeightDelete(final int wd) {
        m_deleteCost = wd;
    }

    /**
     * sets the weight for inserting one letter.
     *
     * @param wi the int weight for one insertion
     */
    public void setWeightInsert(final int wi) {
        m_insertCost = wi;
    }

    /**
     * sets the weight for changing one letter into another.
     *
     * @param wc the int weight for one change
     */
    public void setWeightChange(final int wc) {
        m_changeCost = wc;
    }

    /**
     * sets the weight for switching two letters.
     *
     * @param ws the int weight for one switch
     */
    public void setWeightSwitch(final int ws) {
        m_switchCost = ws;
    }

    /**
     * sets the weights for all operation.
     *
     * @param wd the int weight for one deletion
     * @param wi the int weight for one insertion
     * @param wc the int weight for one change
     * @param ws the int weight for one switch
     */
    public void setWeights(final int wd, final int wi, final int wc, final int ws) {
        m_deleteCost = wd;
        m_insertCost = wi;
        m_changeCost = wc;
        m_switchCost = ws;
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
    private static int min(final int x1, final int x2, final int x3, final int x4) {
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
     * @param startWord the first string
     * @param targetWord the second string
     * @return the distance of the two strings
     */
    public int calculate(final String startWord, final String targetWord) {
        return calculate(startWord.toCharArray(), targetWord.toCharArray());
    }

    /**
     * Levenshtein-Damerau distance if smaller as k.
     *
     * @param startWord the first string
     * @param targetWord the second string
     * @param distUpperBound the maximal value for the distance
     * @return the minimum of k+1 and the Levenshtein-Damerau distance of the two strings
     */
    public int calculate(final String startWord, final String targetWord, final int distUpperBound) {
        return calculate(startWord.toCharArray(), targetWord.toCharArray(), distUpperBound);
    }

    /**
     * Levenshtein-Damerau distance.
     *
     * @param startWord the first string
     * @param targetWord the second string
     * @return the distance of the two strings
     */
    private int calculate(final char[] startWord, final char[] targetWord) {

        int max = 0;
        for (char c : startWord) {
            max = c > max ? c : max;
        }
        for (char c : targetWord) {
            max = c > max ? c : max;
        }
        max++;

        final int startWordLength = startWord.length;
        final int targetWordLength = targetWord.length;
        // matrix for the last position of letter * is stored in da[*], necessary to calculate the switch
        // maximal distance (delete all  letters and insert all other)
        final int distUpperBound = startWordLength * m_deleteCost + targetWordLength * m_insertCost + 1;

        final int[][] matrix = new int[startWordLength + 2][targetWordLength + 2];
        matrix[0][0] = distUpperBound; // initializes the matrix
        for (int i = 0; i <= startWordLength; i++) {
            matrix[i + 1][1] = i * m_deleteCost;
            matrix[i + 1][0] = distUpperBound;
        }
        for (int j = 0; j <= targetWordLength; j++) {
            matrix[1][j + 1] = j * m_insertCost;
            matrix[0][j + 1] = distUpperBound;
        }

        final int[] rightmostKnownPosition = new int[max];
        for (char c = 0; c < max; c++) {
            rightmostKnownPosition[c] = 0;
        }

        int db;
        int i1;
        int j1;
        int changeCostHere;
        for (int i = 1; i <= startWordLength; i++) {
            db = 0;
            for (int j = 1; j <= targetWordLength; j++) {
                //TODO find a better way to get rid of chars above 256
                // okay lets try to use max.
                i1 = rightmostKnownPosition[targetWord[j - 1]];
                j1 = db;
                if (startWord[i - 1] == targetWord[j - 1]) {
                    changeCostHere = 0;
                    db = j;
                } else {
                    changeCostHere = m_changeCost;
                }
                matrix[i + 1][j + 1] = min(
                    // if its cheapest to change  the letter or do nothing
                    matrix[i][j] + changeCostHere,
                    // if its cheapest to insert the letter
                    matrix[i + 1][j] + m_insertCost,
                    // if its cheapest to delete the letter
                    matrix[i][j + 1] + m_deleteCost,
                    // if its cheapest to delete all letters between(origin word), then swap, and than  insert the other letters(searched word)
                    matrix[i1][j1] + (i - i1 - 1) * m_deleteCost + m_switchCost + (j - j1 - 1) * m_insertCost);
            }
            rightmostKnownPosition[startWord[i - 1]] = i; // stores the position of the last letter
        }
        return matrix[startWordLength + 1][targetWordLength + 1]; // the minimal distance
    }

    /**
     * Levenshtein-Damerau distance if smaller as k.
     *
     * @param startWord the first string
     * @param targetWord the second string
     * @param distUpperBound the maximal value for the distance
     * @return the minimum of k+1 and the Levenshtein-Damerau distance of the two strings
     */
    private int calculate(final char[] startWord, final char[] targetWord, final int distUpperBound) {
        int n = startWord.length;
        int m = targetWord.length;

        /**
         * if wordfromchar is shorter then word tochar the distance is at least the difference * m_wi
         */
        if (n < m && distUpperBound < (m - n) * m_insertCost) {
            return distUpperBound + 1;
        }

        /**
         * if wordfromchar is longer then word tochar the distance is at least the difference * m_wd
         */
        if (m < n && distUpperBound < (n - m) * m_deleteCost) {
            return distUpperBound + 1;
        }

        /**
         * otherwise we use the standard algorithm
         */
        return calculate(startWord, targetWord);
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance the method uses the datatable given in the
     * initialization
     *
     * @param candidateWord the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z if the list were set, otherwise NULL is
     *         returned
     */
    public ArrayList<char[]> getNearestWord(final char[] candidateWord) {
        if (m_sorted == 1) {
            return getNearestWord(this.m_groupedDictList, candidateWord);
        } else if (m_sorted == 2) {
            return getNearestWord(this.m_dictData, this.m_dictCol, candidateWord);
        } else {
            return null;
        }
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance
     *
     * @param dictTable an Datatable which contains an column of strings
     * @param dictCol the column of the Datatable, which contains the strings
     *
     * @param candidateWord the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z
     */
    private ArrayList<char[]> getNearestWord(final DataTable dictTable, final int dictCol, final char[] candidateWord) {
        int shortestKnownDist = Integer.MAX_VALUE;

        ArrayList<char[]> nearest = new ArrayList<>(3);

        for (DataRow row : dictTable) {
            DataCell cell = row.getCell(dictCol);
            String cellString;
            if (!cell.isMissing() && cell.getType().isCompatible(TermValue.class)) {
                cellString = ((TermValue)cell).getTermValue().getText();
            } else {
                cellString = cell.toString();
            }
            if (!cell.isMissing()) {
                char[] dictWord = cellString.toCharArray();
                int currentDist = calculate(candidateWord, dictWord, shortestKnownDist);
                if (currentDist == shortestKnownDist) {
                    nearest.add(dictWord);
                } else if (currentDist < shortestKnownDist) {
                    nearest.clear();
                    nearest.add(dictWord);
                    shortestKnownDist = currentDist;
                }
            }
        }
        m_lastDist = shortestKnownDist;
        return nearest;
    }

    /**
     * Finds all words, which have the smallest distance to the given.
     *
     * There could be more than one word, which have the smallest distance
     *
     * @param dictWordsByLength an by length sorted ArrayList (like the method sort produces)
     * @param candidateWord the string to be searched in the list t
     * @return an List of all words which have the smallest distance to z
     */
    private ArrayList<char[]> getNearestWord(final ArrayList<ArrayList<char[]>> dictWordsByLength,
            final char[] candidateWord) {
        if (dictWordsByLength.isEmpty()) {
            m_lastDist = Integer.MAX_VALUE;
            return new ArrayList<>();
        }

        int candidateLength = candidateWord.length;
        ArrayList<char[]> nearest = new ArrayList<>(3);
        int longestDictWord = dictWordsByLength.size() - 1;
        int shortestKnownDist = Integer.MAX_VALUE;
        for (int i = 1; i <= 2 * candidateLength + 1; i++) {
            int nextDictWordLength = candidateLength + (int)Math.pow(-1, i) * Math.abs(i / 2);
            if (nextDictWordLength >= 0 && nextDictWordLength <= longestDictWord) {
                if (
                        // if wordfromchar is shorter then word tochar the distance is at least the difference * m_wi
                        !(nextDictWordLength < candidateLength && shortestKnownDist < (candidateLength - nextDictWordLength) * m_insertCost)
                        // if wordfromchar is longer then word tochar the distance is at least the difference * m_wd
                        && !(candidateLength < nextDictWordLength && shortestKnownDist < (nextDictWordLength - candidateLength) * m_deleteCost)) {
                    final ArrayList<char[]> dictWordsOfLength = dictWordsByLength.get(nextDictWordLength);
                    for (int j = 0; j < dictWordsOfLength.size(); j++) {
                        final char[] dictWord = dictWordsOfLength.get(j);
                        int currentDist = calculate(dictWord, candidateWord);
                        if (currentDist == shortestKnownDist) {
                            nearest.add(dictWord);
                        } else if (currentDist < shortestKnownDist) {
                            nearest.clear();
                            nearest.add(dictWord);
                            shortestKnownDist = currentDist;
                        }
                    }
                }
            }
        }
        m_lastDist = shortestKnownDist;
        return nearest;
    }

    /**
     * sorts one column of the table by stringlength.
     *
     * @param dictTable a DataTable which contains Strings
     * @param col the column of the given DataTable, which you like to sort
     * @return an Arraylist with the following characteristics at the i. position of the list you find an list with all
     *         words of length i
     * @throws CanceledExecutionException
     *
     */
    private ArrayList<ArrayList<char[]>> groupByLength(final DataTable dictTable, final int col)
            throws CanceledExecutionException {

        int maxLength = 0;
        ArrayList<ArrayList<char[]>> dictWordsByLength = new ArrayList<ArrayList<char[]>>(16);

        for (DataRow row : dictTable) {
            m_exec.checkCanceled();

            final DataCell cell = row.getCell(col);
            String cellString;
            if (!cell.isMissing() && cell.getType().isCompatible(TermValue.class)) {
                cellString = ((TermValue)cell).getTermValue().getText();
            } else {
                cellString = cell.toString();
            }
            if (!cell.isMissing()) {
                char[] word = cellString.toCharArray();
                int wordLength = word.length;
                while (wordLength >= maxLength) {
                    dictWordsByLength.add(new ArrayList<>(100));
                    maxLength++;
                }
                dictWordsByLength.get(wordLength).add(word);
            }
        }
        return dictWordsByLength;
    }
}

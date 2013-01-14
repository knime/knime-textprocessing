/*
 * ------------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 *
 * History
 *   Jun 10, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.nodes.misc.keywordextractor.keygraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare.TermEvent;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentUtil;
import org.knime.ext.textprocessing.util.FrequencyMap;
import org.knime.ext.textprocessing.util.Maps;
import org.knime.ext.textprocessing.util.SoftDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;
import org.knime.ext.textprocessing.util.UnorderedPair;

/**
 * Extracts keywords from a document according to the method presented in
 * "KeyGraph: Automatic Indexing by Co-occurrence Graph based on Building
 * Construction Metaphor" by Yukio Ohsawa. See the article and the xml node
 * description for a description of how the algorithm works.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public class KeygraphNodeModel extends NodeModel {

    /**
     * How many terms will be used for the "high key" set?
     */
    private SettingsModelIntegerBounded m_nrHighKeyTerms =
            KeygraphNodeDialog.createSetNrHighKeyTermsModel();

    /**
     * How many terms will be used for the "high frequency" set?
     */
    private SettingsModelIntegerBounded m_nrHighFreqTerms =
            KeygraphNodeDialog.createSetNrHighFreqTermsModel();

    /**
     * How many keywords should we extract? If this value is larger than the
     * number of unique terms in a document, fewer keywords will be returned.
     */
    private SettingsModelIntegerBounded m_nrKeywords =
            KeygraphNodeDialog.createSetNrKeywordsModel();

    /**
     * When true, a copy of the input document will be created during internal
     * processing. This copy contains the same elements as the original with the
     * exception that the terms will not have any tags (essentially making
     * .equals() behave like .equalsOnlyWords(). The documents returned by
     * execute() will not be affected - this is solely for internal use.
     */
    private SettingsModelBoolean m_ignoreTermTags =
            KeygraphNodeDialog.createSetIgnoreTermTagsModel();

    /**
     * Which column contains the documents to analyse?
     */
    private SettingsModelString m_documentColumnName =
            KeygraphNodeDialog.createSetDocumentColumnNameModel();

    private NodeLogger m_logger =
            org.knime.core.node.NodeLogger.getLogger(getClass());

    /**
     * One input port, one output port.
     */
    public KeygraphNodeModel() {
        super(1, 1);
    }

    /**
     * For each unique document in the input table, a set of (Term, Double,
     * Document) tuples will be output, where the set of Terms is the keywords
     * that were extracted and the Double value is their keywordness score.
     *
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        Set<Document> documents =
                DocumentUtil.extractUniqueDocuments(inData[0], exec
                        .createSubProgress(0.1), m_documentColumnName
                        .getStringValue());

        Map<Document, Map<Term, Integer>> keywords =
                new HashMap<Document, Map<Term, Integer>>();

        // Process each document
        int i = 1;
        int nbdocs = documents.size();
        ExecutionMonitor subExecDocs = exec.createSubProgress(0.9);
        for (Document doc : documents) {
            exec.checkCanceled();
            ExecutionMonitor subDoc =
                    subExecDocs.createSubProgress(1.0 / nbdocs);

            subExecDocs.setProgress("Processing document " + i + " of "
                    + nbdocs);

            if (m_ignoreTermTags.getBooleanValue()) {
                keywords.put(doc, extractKeywords(DocumentUtil
                        .stripTermTags(doc), subDoc));
            } else {
                keywords.put(doc, extractKeywords(doc, subDoc));
            }

            ++i;
        }

        return new BufferedDataTable[]{buildResultTable(keywords, exec)};
    }

    /**
     * We start by creating a basic graph using the n most frequent terms,
     * linking the n-1 ones with the strongest association. Weak edges (for
     * which there does not exist a path between the two nodes if the edge is
     * removed) are pruned, further isolating potential clusters.
     *
     * The connected subgraphs are then extracted as clusters ("concepts"). For
     * each term in the document, a "key" value is then calculated, which is the
     *  probability of the term occurring when the author has all concepts in
     * mind ( P(UNION_{c in C}(w|c)), c being a cluster and C being the set of
     * all clusters. The top scoring terms are added to the graph.
     *
     * For each high key term, the edge with the strongest column score (where
     * column(w1, w2) = summation for each sentence of min(freq(w1), freq(w2))
     * to each cluster is added (so each high key term becomes connected to
     * each cluster).
     *
     * Each term is then scored based on the summation of the column measure for
     *  each edge connected to it.
     *
     * @param doc the document to analyse
     * @param progress a mean of reporting progress
     * @return keywords and their associated 'keywordness' score
     * @throws CanceledExecutionException when cancelled from the outside
     */
    private final Map<Term, Integer> extractKeywords(final Document doc,
            final ExecutionMonitor progress) throws CanceledExecutionException {
        progress.setProgress(0.0, "Analysing the document");
        TermEvent ev = new TermEvent(doc, m_nrHighFreqTerms.getIntValue());
        Map<Sentence, FrequencyMap<Term>> termFrequencies =
                getTermFrequenciesPerSentence(doc);

        progress.checkCanceled();
        progress.setProgress(0.1, 
                "Adding and linking the high frequency terms");
        List<Term> highFrequencyTerms =
                new ArrayList<Term>(ev.getTopFrequentTerms());
        UndirectedGraph<Term, Integer> keyGraph =
                new UndirectedGraph<Term, Integer>();
        for (Term t : highFrequencyTerms) {
            keyGraph.addNode(t);
        }

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("High frequency set: " + highFrequencyTerms);
        }

        Map<UnorderedPair<Term>, Integer> hfLinks =
                new HashMap<UnorderedPair<Term>, Integer>();
        for (int it1 = 0; it1 < highFrequencyTerms.size(); it1++) {
            progress.checkCanceled();
            Term t1 = highFrequencyTerms.get(it1);

            for (int it2 = it1 + 1; it2 < highFrequencyTerms.size(); it2++) {
                Term t2 = highFrequencyTerms.get(it2);
                int weight = 0;
                for (FrequencyMap<Term> freqs : termFrequencies.values()) {
                    weight += Math.min(freqs.get(t1), freqs.get(t2));
                }

                hfLinks.put(UnorderedPair.makePair(t1, t2), weight);
            }
        }

        // Add the |HF| - 1 top links to the graph
        hfLinks = Maps.getTopValues(hfLinks, highFrequencyTerms.size() - 1);
        for (Entry<UnorderedPair<Term>, Integer> edge : hfLinks.entrySet()) {
            progress.checkCanceled();
            
            UnorderedPair<Term> pair = edge.getKey();
            keyGraph.addEdge(pair.getFirst(), pair.getSecond(), 
                    edge.getValue());
        }

        progress.checkCanceled();
        progress.setProgress(0.2, "Pruning weak edges");
        keyGraph.pruneWeakEdges();

        progress.checkCanceled();
        progress.setProgress(0.3, "Finding clusters");
        Set<Set<Term>> clusters = keyGraph.getConnectedSubgraphs();
        if (m_logger.isDebugEnabled()) {
            m_logger.debug(("Found " + clusters.size() + " clusters: " 
                    + clusters));
        }

        progress.checkCanceled();
        progress.setProgress(0.4, "Finding high key terms to add to the graph");

        // Calculate neighbour values (used for 'key')
        Map<Set<Term>, Integer> neighbourvalues =
                new HashMap<Set<Term>, Integer>();
        for (Set<Term> cluster : clusters) {
            int score = 0;

            for (Entry<Sentence, FrequencyMap<Term>> ent : termFrequencies
                    .entrySet()) {
                progress.checkCanceled();
                
                Sentence sen = ent.getKey();
                FrequencyMap<Term> freqs = ent.getValue();

                Set<Term> senterms = new HashSet<Term>(sen.getTerms());
                int clusterfreq = freqs.getTotalFrequency(cluster);

                for (Term t : senterms) {
                    int termfreq = freqs.get(t);

                    if (cluster.contains(t)) {
                        score += termfreq * (clusterfreq - termfreq);
                    } else {
                        score += termfreq * clusterfreq;
                    }
                }
            }

            neighbourvalues.put(cluster, score);
        }

        // Calculate based values (used for 'key')
        Map<Pair<Term, Set<Term>>, Integer> basedvalues =
                new HashMap<Pair<Term, Set<Term>>, Integer>();
        for (Term t : ev.getTerms()) {
            for (Set<Term> cluster : clusters) {
                progress.checkCanceled();
                
                int based = 0;
                for (FrequencyMap<Term> freqs : termFrequencies.values()) {
                    int termfreq = freqs.get(t);
                    int clusterfreq = freqs.getTotalFrequency(cluster);

                    if (cluster.contains(t)) {
                        based += termfreq * (clusterfreq - termfreq);
                    } else {
                        based += termfreq * clusterfreq;
                    }
                }
                basedvalues.put(new Pair<Term, Set<Term>>(t, cluster), based);
            }
        }

        // Calculate keys
        Map<Term, Double> keys = new HashMap<Term, Double>();
        for (Term t : ev.getTerms()) {
            progress.checkCanceled();
            
            double prob = 1.0;
            for (Set<Term> cluster : clusters) {
                prob *= 1.0 - (double)basedvalues.get(
                        new Pair<Term, Set<Term>>(t,cluster)) 
                        / neighbourvalues.get(cluster);
            }

            keys.put(t, 1 - prob);
        }

        Set<Term> highkey =
                Maps.getTopValues(keys, m_nrHighKeyTerms.getIntValue())
                        .keySet();

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("High key set: " + highkey);
        }

        // Add the high key terms to the graph
        for (Term hk : highkey) {
            keyGraph.addNode(hk);
        }

        // Find new edges to add
        for (Term hk : highkey) {
            progress.checkCanceled();
            
            Map<UnorderedPair<Term>, Integer> columns =
                    new HashMap<UnorderedPair<Term>, Integer>();

            for (Term hf : highFrequencyTerms) {
                int colvalue = 0;
                for (FrequencyMap<Term> senfreq : termFrequencies.values()) {
                    colvalue += Math.min(senfreq.get(hk), senfreq.get(hf));
                }

                columns.put(UnorderedPair.makePair(hk, hf), colvalue);
            }

            // For each cluster,
            // add the highest column connecting the term to the cluster
            for (Set<Term> cluster : clusters) {
                progress.checkCanceled();
                
                int maxWeight = -1;
                UnorderedPair<Term> maxEdge = null;

                for (Term hf : cluster) {
                    UnorderedPair<Term> edge = UnorderedPair.makePair(hk, hf);
                    int colval = columns.get(UnorderedPair.makePair(hk, hf));
                    if (colval > maxWeight) {
                        maxWeight = colval;
                        maxEdge = edge;
                    }
                }

                if (maxEdge != null) {
                    keyGraph.addEdge(maxEdge.getFirst(), maxEdge.getSecond(),
                        maxWeight);
                }
            }
        }

        progress.checkCanceled();
        progress.setProgress(0.8, "Calculating the final keyword score");
        // And now, calculate an actual score for every single node, which is
        // simply the sum of the column score for each edge connected to it
        Map<Term, Integer> scores = new HashMap<Term, Integer>();
        for (Term node : keyGraph.getNodes()) {
            progress.checkCanceled();
            
            int score = 0;
            Set<Term> neighbours = keyGraph.getNeighbours(node);
            // only keep the neighbours from HF
            neighbours.retainAll(highFrequencyTerms);

            for (FrequencyMap<Term> freqs : termFrequencies.values()) {
                int termfreq = freqs.get(node);

                for (Term neighbour : neighbours) {
                    score += Math.min(termfreq, freqs.get(neighbour));
                }
            }

            scores.put(node, score);
        }

        progress.setProgress(1, "Done");
        return Maps.getTopValues(scores, m_nrKeywords.getIntValue());
    }

    /**
     * @param doc the document to analyse
     * @return term frequencies for each sentence
     */
    private Map<Sentence, FrequencyMap<Term>> getTermFrequenciesPerSentence(
            final Document doc) {
        Map<Sentence, FrequencyMap<Term>> freqs =
                new HashMap<Sentence, FrequencyMap<Term>>();

        Iterator<Sentence> senit = doc.sentenceIterator();
        while (senit.hasNext()) {
            Sentence sen = senit.next();

            freqs.put(sen, DocumentUtil.getTermFrequencies(sen));
        }

        return freqs;
    }

    /**
     * Builds a BufferedDataTable from a map of Document->{Keyword, Score}*.
     *
     * @param keywords the (documents, keyword, score) tuples
     * @param exec an execution monitor to report on progress
     * @return a buffered data table with Keyword|Value|Document rows
     */
    private static BufferedDataTable buildResultTable(
            final Map<Document, Map<Term, Integer>> keywords,
            final ExecutionContext exec) throws CanceledExecutionException {
        BufferedDataContainer con =
                exec.createDataContainer(createDataTableSpec());
        DataCellCache docCache = new SoftDataCellCache(
               TextContainerDataCellFactoryBuilder.createDocumentCellFactory());
        
        int rowid = 0;
        for (Entry<Document, Map<Term, Integer>> e : keywords.entrySet()) {
            exec.checkCanceled();
            Document doc = e.getKey();
            for (Entry<Term, Integer> kw : e.getValue().entrySet()) {
                DefaultRow row =
                        new DefaultRow(
                                new RowKey(Integer.toString(rowid)),
                                new DataCell[]{
                                        new TermCell(kw.getKey()),
                                        new DoubleCell(kw.getValue()),
                                        docCache.getInstance(doc)});
                con.addRowToTable(row);
                rowid++;
            }
        }
        con.close();

        return con.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{createDataTableSpec()};
    }

    /**
     * Ensures that the input table spec is valid. This node requires that a
     * document cell be present.
     *
     * @param spec the spec to validate
     * @throws InvalidSettingsException thrown if the spec is not valid
     */
    private final void checkDataTableSpec(final DataTableSpec spec)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save
    }

    /**
     * Creates a DataTableSpec for the node, namely (Term, Double, Document)
     *
     * @return the data table spec
     */
    private static DataTableSpec createDataTableSpec() {
        DataColumnSpecCreator keywords =
                new DataColumnSpecCreator("Keyword", TermCell.TYPE);
        DataColumnSpecCreator chivalues =
                new DataColumnSpecCreator("Score", DoubleCell.TYPE);
        DataColumnSpecCreator docs =
                new DataColumnSpecCreator("Document", DocumentBlobCell.TYPE);
        return new DataTableSpec(keywords.createSpec(), chivalues.createSpec(),
                docs.createSpec());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_ignoreTermTags.saveSettingsTo(settings);
        m_nrHighKeyTerms.saveSettingsTo(settings);
        m_nrKeywords.saveSettingsTo(settings);
        m_nrHighFreqTerms.saveSettingsTo(settings);
        m_documentColumnName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_ignoreTermTags.loadSettingsFrom(settings);
        m_nrHighFreqTerms.loadSettingsFrom(settings);
        m_nrKeywords.loadSettingsFrom(settings);
        m_nrHighKeyTerms.loadSettingsFrom(settings);
        m_documentColumnName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_nrHighFreqTerms.validateSettings(settings);
        m_ignoreTermTags.validateSettings(settings);
        m_nrHighKeyTerms.validateSettings(settings);
        m_nrKeywords.validateSettings(settings);
        m_documentColumnName.validateSettings(settings);
    }
}

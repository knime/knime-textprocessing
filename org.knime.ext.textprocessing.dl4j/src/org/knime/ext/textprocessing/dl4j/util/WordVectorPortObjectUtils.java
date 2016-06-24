/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Utility class for {@link WordVectorPortObject} and {@link WordVectorPortObjectSpec}
 * Serialisation. Also contains utility methods for members of port and spec class. 
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorPortObjectUtils {

	private WordVectorPortObjectUtils() {
		// Utility class
	}
	
	/**
	 * Serializes a {@link WordVectorPortObject} to zip using the specified {@link ZipOutputStream}.
	 * It can be specified if both PortObject and Spec should be written or only one of them.
	 * 
	 * @param portObject the {@link WordVectorPortObject} to write
	 * @param writePortObject whether to write PortObject
	 * @param writeSpec whether to write Spec
	 * @param outStream stream to write to 
	 * @throws IOException 
	 */
	public static void saveModelToZip(WordVectorPortObject portObject, boolean writePortObject, boolean writeSpec, ZipOutputStream outStream)
			throws IOException{
		WordVectorPortObjectSpec spec = (WordVectorPortObjectSpec)portObject.getSpec();
		
		if(outStream == null){
			throw new IOException("OutputStream is null");
		}
		if(writePortObject && !writeSpec){
			savePortObjectOnly(portObject, outStream);
		}
		if(!writePortObject && writeSpec){
			saveSpecOnly(spec, outStream);
		}
		if(writePortObject && writeSpec){
			savePortObjectAndSpec(portObject, spec, outStream);
		}
	}
	
	/**
	 * Reads {@link WordVectorPortObjectSpec} from specified {@link ZipInputStream}.
	 * 
	 * @param inStream stream to read from
	 * @return WordVectorPortObjectSpec
	 * @throws IOException
	 */
	public static WordVectorPortObjectSpec loadSpecFromZip(ZipInputStream inStream) throws IOException{			
		return new WordVectorPortObjectSpec(loadTrainingsMode(inStream));	
	}
	
	/**
	 * Reads {@link WordVectorPortObject} from specified {@link ZipInputStream}.
	 * 
	 * @param inStream stream to read from
	 * @return WordVectorPortObject
	 * @throws IOException
	 */
	public static WordVectorPortObject loadPortFromZip(ZipInputStream inStream) throws IOException{
		return new WordVectorPortObject(loadWordVectors(inStream), null);
	}
	
	/**
	 * Reads {@link WordVectors} from specified {@link ZipInputStream}.
	 * 
	 * @param in stream to read from
	 * @return {@link WordVectors} loaded from stream
	 * @throws IOException
	 */
	public static WordVectors loadWordVectors(ZipInputStream in) throws IOException{
		String whitespaceReplacement = "_Az92_";
		AbstractCache<VocabWord> cache = null;
		InMemoryLookupTable<VocabWord> lookupTable = null;
		ZipEntry entry;
		
		while ((entry = in.getNextEntry())!= null) {
			if(entry.getName().matches("word_vectors")){
				
				//Normally just call WordVectorSerializer.loadTxtVectors(stream, skipFirstLine), however, this results
				//in encoding problems. Method loadTxtVectors can't be called with reader using UTF-8 encoding, which is
				//used for writing method. Thus copy implementation and use InputStreamReader specifying UTF-8 encoding.
				
				cache = new AbstractCache.Builder<VocabWord>().build();

		        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		        String line = "";
		        List<INDArray> arrays = new ArrayList<>();
		        
		        while((line = reader.readLine()) != null) {
		            String[] split = line.split(" ");
		            String word = split[0].replaceAll(whitespaceReplacement, " ");
		            VocabWord word1 = new VocabWord(1.0, word);

		            word1.setIndex(cache.numWords());

		            cache.addToken(word1);

		            cache.addWordToIndex(word1.getIndex(), word);

		            cache.putVocabWord(word);
		            INDArray row = Nd4j.create(Nd4j.createBuffer(split.length - 1));
		            for (int i = 1; i < split.length; i++) {
		                row.putScalar(i - 1, Float.parseFloat(split[i]));
		            }
		            arrays.add(row);
		        }

		        lookupTable = (InMemoryLookupTable<VocabWord>) new InMemoryLookupTable.Builder<VocabWord>()
		                .vectorLength(arrays.get(0).columns())
		                .cache(cache)
		                .build();

		        INDArray syn = Nd4j.create(new int[]{arrays.size(), arrays.get(0).columns()});
		        for (int i = 0; i < syn.rows(); i++) {
		            syn.putRow(i,arrays.get(i));
		        }

		        Nd4j.clearNans(syn);
		        lookupTable.setSyn0(syn);
			}
		}
		return WordVectorSerializer.fromPair(Pair.makePair((InMemoryLookupTable) lookupTable, (VocabCache) cache));
	}
	
	/**
	 * Reads {@link WordVectorTrainingMode} from specified {@link ZipInputStream}.
	 * 
	 * @param in stream to read from
	 * @return {@link WordVectorTrainingMode} loaded from stream
	 * @throws IOException
	 */
	public static WordVectorTrainingMode loadTrainingsMode(ZipInputStream in) throws IOException{
		ZipEntry entry;
		WordVectorTrainingMode mode = null;
		
		while ((entry = in.getNextEntry())!= null) {
			if(entry.getName().matches("word_vector_trainings_mode")){
				String read = readStringFromZipStream(in);
				mode = WordVectorTrainingMode.valueOf(read);
			}
		}		
		return mode;
	}
	
	private static void savePortObjectOnly(WordVectorPortObject portObject, ZipOutputStream out) throws IOException{
		writeWordVectors(portObject.getWordVectors(), out);		
	}
	
	private static void saveSpecOnly(WordVectorPortObjectSpec spec, ZipOutputStream out) throws IOException{
		WordVectorTrainingMode mode = spec.getWordVectorTrainingsMode();
		
		writeWordVectorTrainingsMode(mode, out);
	}
	

	private static void savePortObjectAndSpec(WordVectorPortObject portObject, WordVectorPortObjectSpec spec, ZipOutputStream out) 
			throws IOException{
		savePortObjectOnly(portObject, out);	
		saveSpecOnly(spec,out);	
	}
	
	/**
	 * Writes {@link WordVectorTrainingMode} to specified {@link ZipInputStream}.
	 * 
	 * @param mode the {@link WordVectorTrainingMode} to write
	 * @param out stream to write to
	 * @throws IOException
	 */
	public static void writeWordVectorTrainingsMode(WordVectorTrainingMode mode, ZipOutputStream out) throws IOException {
		ZipEntry entry = new ZipEntry("word_vector_trainings_mode");
		out.putNextEntry(entry);
		// TODO may cause problems with encoding on different os
		out.write(mode.toString().getBytes(Charset.forName("UTF-8")));
	}
	
	/**
	 * Writes {@link WordVectors} to specified {@link ZipInputStream}.
	 * 
	 * @param wordVectors {@link WordVectors} to write
	 * @param out stream to write to
	 * @throws IOException
	 */
	public static void writeWordVectors(WordVectors wordVectors, ZipOutputStream out) throws IOException{
		BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(out,Charset.forName("UTF-8")));
		Word2Vec vec = wordVectorsToWord2Vec(wordVectors);
		ZipEntry entry = new ZipEntry("word_vectors");
		out.putNextEntry(entry);
		
		WordVectorSerializer.writeWordVectors(vec, writer);
	}
	
	private static String readStringFromZipStream(ZipInputStream in) throws IOException{
		StringBuilder stringBuilder = new StringBuilder();
	    byte[] byteBuffer = new byte[1024];
	    int currentRead = 0;
	    
	    while ((currentRead = in.read(byteBuffer, 0, 1024)) >= 0) {
            stringBuilder.append(new String(byteBuffer, 0, currentRead));
	    }
	    
	    return stringBuilder.toString();
	}

	/**
	 * Converts wordVectors to {@link Word2Vec}. Sets {@link WeightLookupTable} and
	 * {@link VocabCache}.
	 * 
	 * @param wordVectors 
	 * @return
	 */
	public static Word2Vec wordVectorsToWord2Vec(WordVectors wordVectors){
		Word2Vec w2v = new Word2Vec();
		w2v.setLookupTable(wordVectors.lookupTable());
		w2v.setVocab(wordVectors.vocab());
		return w2v;
	}
	
	
}

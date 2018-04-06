package org.knime.ext.textprocessing.language.spanish.nodes.tagging.stanford.posmodels;

import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.UniversalDependenciesPOSTag;
import org.knime.ext.textprocessing.language.spanish.TextprocessingSpanishLanguagePack;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModel;

/**
 * This class implements the {@link StanfordTaggerModel} interface to provide the "Spanish UD"
 * part-of-speech model.
 * 
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.4
 */
public class SpanishUDModel implements StanfordTaggerModel {

	private static final String MODELNAME = "Spanish UD";

	private static final String MODELPATH = "models/stanfordmodels/posmodels/spanish-ud.tagger";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getModelName() {
		return MODELNAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getModelPath() {
		return TextprocessingSpanishLanguagePack.resolvePath(MODELPATH).getAbsolutePath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagBuilder getTagBuilder() {
		return new UniversalDependenciesPOSTag();
	}

}

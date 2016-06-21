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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.learn;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.NominalValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.WordVectorLearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.dl4j.settings.impl.WordVectorParameterSettingsModels;

/**
 * <code>NodeDialog</code> for the "WordVectorLearner" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorLearnerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the WordVectorLearner node.
     */
    protected WordVectorLearnerNodeDialog() {
    	LearnerParameterSettingsModels learnerSettingsModels = new LearnerParameterSettingsModels();
    	DataParameterSettingsModels dataSettingsModels = new DataParameterSettingsModels();
    	WordVectorParameterSettingsModels wordVectorSettingsModels = new WordVectorParameterSettingsModels();
    	
    	SettingsModelString trainingModeSettings = (SettingsModelString)wordVectorSettingsModels.createParameter(
				WordVectorLearnerParameter.WORD_VECTOR_TRAINING_MODE);
    	
    	SettingsModelString labelColumnSettings = (SettingsModelString)dataSettingsModels.createParameter(
				DataParameter.LABEL_COLUMN);
    	SettingsModelString documentColumnSettings = (SettingsModelString)dataSettingsModels.createParameter(
				DataParameter.DOCUMENT_COLUMN);
    	
    	trainingModeSettings.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				WordVectorTrainingMode mode = WordVectorTrainingMode.valueOf(trainingModeSettings.getStringValue());				
				switch (mode) {			
				case DOC2VEC:	
					labelColumnSettings.setEnabled(true);
					documentColumnSettings.setEnabled(true);
					break;
				case WORD2VEC:
					labelColumnSettings.setEnabled(false);
					documentColumnSettings.setEnabled(true);
					break;
				default:
					break;
				}				
			}
		});
    	
    	addDialogComponent(new DialogComponentStringSelection(
				trainingModeSettings,
				"WordVector Training Mode",
				EnumUtils.getStringCollectionFromToString(WordVectorTrainingMode.values())
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)learnerSettingsModels.createParameter(
						LearnerParameter.SEED),
				"Seed",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelDoubleBounded)learnerSettingsModels.createParameter(
						LearnerParameter.GLOBAL_LEARNING_RATE),
				"Learning Rate",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelDoubleBounded)wordVectorSettingsModels.createParameter(
						WordVectorLearnerParameter.MIN_LEARNING_RATE),
				"Minimum Learning Rate",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)dataSettingsModels.createParameter(
						DataParameter.BATCH_SIZE),
				"Batch Size",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)dataSettingsModels.createParameter(
						DataParameter.EPOCHS),
				"Epochs",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)learnerSettingsModels.createParameter(
						LearnerParameter.TRAINING_ITERATIONS),
				"Number of Training Iterations",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)wordVectorSettingsModels.createParameter(
						WordVectorLearnerParameter.LAYER_SIZE),
				"Layer Size",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)wordVectorSettingsModels.createParameter(
						WordVectorLearnerParameter.MIN_WORD_FREQUENCY),
				"Minimum Word Frequency",
				4
				));
    	addDialogComponent(new DialogComponentNumberEdit(
				(SettingsModelIntegerBounded)wordVectorSettingsModels.createParameter(
						WordVectorLearnerParameter.WINDOW_SIZE),
				"Window Size",
				4
				));
    	
    	createNewTab("Column Selection");
    	addDialogComponent(new DialogComponentColumnNameSelection(
    			labelColumnSettings,
    			"Label Column",
    			0,
    			false,
    			NominalValue.class
                ));
    	addDialogComponent(new DialogComponentColumnNameSelection(
    			documentColumnSettings,
    			"Document Column",
    			0,
    			true,
    			StringValue.class,
    			DocumentValue.class
                ));
    }
}


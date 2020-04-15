#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
	// provide a list of upstream jobs which should trigger a rebuild of this job
	pipelineTriggers([upstream(
		'knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F')
	)]),
	buildDiscarder(logRotator(numToKeepStr: '5')),
	disableConcurrentBuilds()
])

try {
	knimetools.defaultTychoBuild('org.knime.update.ext.textprocessing')

	workflowTests.runTests(
        dependencies: [
			repositories: [
				'knime-textprocessing',
				'knime-filehandling',
				'knime-datageneration',
				'knime-productivity-oss',
				'knime-jep',
				'knime-streaming'
			]
		]
	)

	stage('Sonarqube analysis') {
		env.lastStage = env.STAGE_NAME
		workflowTests.runSonar()
	}
} catch (ex) {
	currentBuild.result = 'FAILED'
	throw ex
} finally {
	notifications.notifyBuild(currentBuild.result);
}
 /* vim: set ts=4: */

#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
	pipelineTriggers([
		upstream('knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F'))
	]),
	buildDiscarder(logRotator(numToKeepStr: '5')),
	disableConcurrentBuilds()
])


node('maven') {
     stage('Checkout Sources') {
        checkout scm
	}

	try {
		stage('Tycho Build') {
			withMavenJarsignerCredentials {
				sh '''
					export TEMP="${WORKSPACE}/tmp"
					rm -rf "${TEMP}"; mkdir "${TEMP}"
					mvn -Dmaven.test.failure.ignore=true -Dknime.p2.repo=${P2_REPO} clean verify
					rm -rf "${TEMP}"
				'''
			}

			// junit '**/target/test-reports/*/TEST-*.xml'
		}


		if (currentBuild.result != 'UNSTABLE') {
			stage('Deploy p2') {
				p2Tools.deploy("${WORKSPACE}/org.knime.update.ext.textprocessing/target/repository/")
			}
		} else {
			echo "==============================================\n" +
				 "| Build unstable, not deploying p2 artifacts.|\n" +
				 "=============================================="
		}


    } catch (ex) {
		currentBuild.result = 'FAILED'
		throw ex
	} finally {
		notifications.notifyBuild(currentBuild.result);
	}
 }
/* vim: set ts=4: */

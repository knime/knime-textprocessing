#!groovy
def BN = (BRANCH_NAME == 'master' || BRANCH_NAME.startsWith('releases/')) ? BRANCH_NAME : 'releases/2022-09'

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([upstream(
        'knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F')
    )]),
    parameters(workflowTests.getConfigurationsAsParameters() + fsTests.getFSConfigurationsAsParameters()),
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
                'knime-kerberos',
                'knime-datageneration',
                'knime-productivity-oss',
                'knime-jep',
                'knime-streaming',
                'knime-reporting'
            ],
            ius: [
                'org.knime.features.ext.textprocessing.language.arabic.feature.group',
                'org.knime.features.ext.textprocessing.language.chinese.feature.group',
                'org.knime.features.ext.textprocessing.language.spanish.feature.group',
                'org.knime.features.ext.textprocessing.language.german.feature.group',
                'org.knime.features.ext.textprocessing.language.french.feature.group'
            ]
        ]
    )

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}
/* vim: set shiftwidth=4 expandtab smarttab: */

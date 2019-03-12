#!groovy

library "knime-pipeline@$BRANCH_NAME"

properties([
  parameters([
    stringParam(
      name: 'BRANCH_NAME',
      defaultValue: 'build/DEVOPS-35_standalone-knime-core-build',
      description: 'Name of the branch to build.'
    ),
    stringParam(
      name: 'KNIME_TP_P2',
      defaultValue: '$P2_REPO/knime-tp/' + env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME Target Platform P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_CORE_P2',
      defaultValue: '$P2_REPO/knime-core/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME Core P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_BASE_P2',
      defaultValue: '$P2_REPO/knime-base/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME Base P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_EXPRESSIONS_P2',
      defaultValue: '$P2_REPO/knime-expressions/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME Expressions P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_JAVASNIPPET_P2',
      defaultValue: '$P2_REPO/knime-javasnippet/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME Javasnippet P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_DL4J_P2',
      defaultValue: '$P2_REPO/knime-dl4j/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'KNIME DL4J P2 update site url.'
    ),
    stringParam(
      name: 'KNIME_SHARED_P2',
      defaultValue: '$P2_REPO/knime-shared/'+ env.BRANCH_NAME.replaceAll("/", "%252F") + '/repository/',
      description: 'org.knime.update.shared site url.'
    )
  ]),

  pipelineTriggers([
    triggers: [
      [
        $class: 'jenkins.triggers.ReverseBuildTrigger',
        upstreamProjects: "knime-base/" + env.BRANCH_NAME.replaceAll("/", "%2F"), threshold: hudson.model.Result.SUCCESS
      ]
    ]
  ]),

  buildDiscarder(logRotator(numToKeepStr: '5')),
])

node {
  docker.withServer('tcp://proxy1:2375') {
    docker.image('knime/jenkins-slave-ubuntu:1.0.2').inside {
      stage('Checkout Sources') {
        checkout([
          $class: 'GitSCM',
          branches: [[name: '${BRANCH_NAME}']],
          doGenerateSubmoduleConfigurations: false,
          extensions: [
            [$class: 'GitLFSPull'],
            [$class: 'CheckoutOption', timeout: 60],
	    [$class: 'CleanBeforeCheckout'],
            [$class: 'PruneStaleBranch']
          ],
          submoduleCfg: [],
          userRemoteConfigs: [[
            credentialsId: 'bitbucket-jenkins',
            url: 'https://bitbucket.org/KNIME/knime-textprocessing'
          ]]
        ])
      }

      stage('Maven/Tycho Build') {
        withMavenJarsignerCredentials {
          sh '''
            export TEMP="${WORKSPACE}/tmp"
            rm -rf "${TEMP}"
            mkdir "${TEMP}"
            mvn --settings /var/cache/m2/settings.xml clean install
            rm -rf "${TEMP}"
          '''
	}
      }

      stage('Stage Build Artifacts') {
        sh '''
          #!/bin/bash -eux

          if [[ ! -d "/var/cache/build_artifacts/${JOB_NAME}/" ]]; then
            mkdir -p "/var/cache/build_artifacts/${JOB_NAME}/"
          else
            rm -Rf /var/cache/build_artifacts/${JOB_NAME}/*
          fi

          cp -a ${WORKSPACE}/org.knime.update.ext.textprocessing/target/repository/ /var/cache/build_artifacts/${JOB_NAME}
        '''
      }
    }
  }
}

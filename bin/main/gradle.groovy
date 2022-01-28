
void call(String[] stagesToRun) {
    String stageBuild = 'build'
    String stageSonar = 'sonar'
    String stageRun = 'run'
    String stageTestRun = 'test run'
    String stageNexus = 'nexus'

    String[] stages = [
        stageBuild,
        stageSonar,
        stageRun,
        stageTestRun,
        stageNexus
    ]

    if (stagesToRun.size() == 0) {
        stagesToRun = stages
    }

    if (stagesToRun.includes(stageBuild)) {
        stage(stageBuild) {
            CURRENT_STAGE = stageBuild
            sh './gradlew clean build'
        }
    }

    if (stagesToRun.includes(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            String scannerHome = tool 'sonar-scanner'
            withSonarQubeEnv('docker-compose-sonarqube') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
        }
    }

    if (stagesToRun.includes(stageRun)) {
        stage(stageRun) {
            CURRENT_STAGE = stageRun
            sh './gradlew bootRun &'
            sleep 20
        }
    }

    if (stagesToRun.includes(stageTestRun)) {
        stage(stageTestRun) {
            CURRENT_STAGE = stageTestRun
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    if (stagesToRun.includes(stageNexus)) {
        stage(stageNexus) {
            CURRENT_STAGE = stageNexus
            nexusPublisher nexusInstanceId: 'nexus3-docker',
        nexusRepositoryId: 'ejemplo-gradle',
        packages: [
            [
                $class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '', extension: '', filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar']
                ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
        }
    }
}

return this

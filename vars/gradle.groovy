
void call(String[] stagesToRun, String pipelineType) {

    figlet 'Gradle'
    figlet pipelineType

    if (pipelineType == 'CI') {
        runCi(stagesToRun)
    } else if (pipelineType == 'CD') {
        runCd(stagesToRun)
    } else {
        throw new Exception('PipelineType Inválido: ' + pipelineType)
    }
}

void runCd(String[] stagesToRun) {
    String downloadNexus = 'downloadNexus'
    String runDownloadedJar = 'runDownloadedJar'
    String rest = 'rest'
    String nexusCD = 'nexusCD'

    String[] stages = [
        downloadNexus,
        runDownloadedJar,
        rest,
        nexusCD
    ]

    String[] currentStages = []

    if (stagesToRun.size() == 1 && stagesToRun[0] == '') {
        currentStages = stages
    } else {
        currentStages = stagesToRun
    }

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    if (currentStages.contains(downloadNexus)) {
        stage(downloadNexus) {
            CURRENT_STAGE = downloadNexus
            figlet CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: 'nexus3-docker-user', variable: 'NEXUS_CREDENTIALS')]) {
                sh 'curl -u ${NEXUS_CREDENTIALS} "http://nexus.localhost/repository/test-repo/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
            }
        }
    }

    if (currentStages.contains(runDownloadedJar)) {
        stage(runDownloadedJar) {
            CURRENT_STAGE = runDownloadedJar
            figlet CURRENT_STAGE
            sh 'java -jar DevOpsUsach2020-0.0.1.jar &'
            sleep 20
        }
    }

    if (currentStages.contains(rest)) {
        stage(rest) {
            CURRENT_STAGE = rest
            figlet CURRENT_STAGE
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    if (currentStages.contains(nexusCD)) {
        stage(nexusCD) {
            CURRENT_STAGE = nexusCD
            figlet CURRENT_STAGE
            nexusPublisher nexusInstanceId: NEXUS_INSTANCE_ID,
            nexusRepositoryId: NEXUS_REPOSITORY,
            packages: [
                [
                    $class: 'MavenPackage',
                    mavenAssetList: [
                        [classifier: '', extension: '', filePath: 'DevOpsUsach2020-0.0.1.jar']
                    ],
                    mavenCoordinate: [
                        artifactId: 'DevOpsUsach2020',
                        groupId: 'com.devopsusach2020',
                        packaging: 'jar',
                        version: '1.0.0'
                    ]
                ]
            ]
        }
    }
}

void runCi(String[] stagesToRun) {
    String stageBuild = 'buildAndTest'
    String stageSonar = 'sonar'
    String stageRun = 'runJar'
    String stageTestRun = 'rest'
    String stageNexus = 'nexusCI'

    String[] stages = [
        stageBuild,
        stageSonar,
        stageRun,
        stageTestRun,
        stageNexus
    ]

    String[] currentStages = []

    if (stagesToRun.size() == 1 && stagesToRun[0] == '') {
        currentStages = stages
    } else {
        currentStages = stagesToRun
    }

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    if (currentStages.contains(stageBuild)) {
        stage(stageBuild) {
            CURRENT_STAGE = stageBuild
            figlet CURRENT_STAGE
            sh './gradlew clean build'
        }
    }

    if (currentStages.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            figlet CURRENT_STAGE
            String scannerHome = tool 'sonar-scanner'
            withSonarQubeEnv('docker-compose-sonarqube') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
        }
    }

    if (currentStages.contains(stageRun)) {
        stage(stageRun) {
            CURRENT_STAGE = stageRun
            figlet CURRENT_STAGE
            sh './gradlew bootRun &'
            sleep 20
        }
    }

    if (currentStages.contains(stageTestRun)) {
        stage(stageTestRun) {
            CURRENT_STAGE = stageTestRun
            figlet CURRENT_STAGE
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    if (currentStages.contains(stageNexus)) {
        stage(stageNexus) {
            CURRENT_STAGE = stageNexus
            figlet CURRENT_STAGE
            nexusPublisher nexusInstanceId: NEXUS_INSTANCE_ID,
            nexusRepositoryId: NEXUS_REPOSITORY,
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

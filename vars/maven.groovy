
void call() {
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
    } else if (stages.intersect(stagesToRun).size() == stagesToRun.size()) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + stagesToRun.join(', '))
    }

    if (stagesToRun.contains(stageBuild)) {
        stage(stageBuild) {
            CURRENT_STAGE = stageBuild
            sh './mvnw clean compile -e'
            sh './mvnw clean test -e'
            sh './mvnw clean package -e'
        }
    }

    if (stagesToRun.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            def scannerHome = tool 'sonar-scanner'
            withSonarQubeEnv('docker-compose-sonarqube') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-maven -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
        }
    }

    if (stagesToRun.contains(stageRun)) {
        stage(stageRun) {
            CURRENT_STAGE = stageRun
            sh './mvnw spring-boot:run &'
            sleep 20
        }
    }

    if (stagesToRun.contains(stageTestRun)) {
        stage(stageTestRun) {
            CURRENT_STAGE = stageTestRun
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    if (stagesToRun.contains(stageNexus)) {
        stage(stageNexus) {
            CURRENT_STAGE = stageNexus
            nexusPublisher nexusInstanceId: 'nexus3-docker',
        nexusRepositoryId: 'ejemplo-maven',
        packages: [
            [
                $class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '', extension: '', filePath: 'build/DevOpsUsach2020-0.0.1.jar']
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


def call() {
    stage('build & unit test') {
        CURRENT_STAGE = 'build & unit test'
        sh './gradlew clean build'
    }
    stage('sonar') {
        CURRENT_STAGE = 'sonar'
        def scannerHome = tool 'sonar-scanner'
        withSonarQubeEnv('docker-compose-sonarqube') {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.sources=src -Dsonar.java.binaries=build"
        }
    }
    stage('run') {
        CURRENT_STAGE = 'run'
        sh './gradlew bootRun &'
        sleep 20
    }
    stage('test run') {
        CURRENT_STAGE = 'test run'
        sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
    }
    stage('nexus') {
        CURRENT_STAGE = 'nexus'
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

return this

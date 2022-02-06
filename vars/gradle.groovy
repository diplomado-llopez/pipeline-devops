
void call(String pipelineType) {

    figlet 'Gradle'
    figlet pipelineType

    if (pipelineType.contains('CI-')) {
        runCi()
    } else if (pipelineType == 'CD') {
        runCd()
    } else {
        throw new Exception('PipelineType Inválido: ' + pipelineType)
    }
}

void runCd() {
    String gitDiff         = "gitDiff"
    String nexusDownload   = 'nexusDownload'
    String run             = "run"
    String test            = "test"
    String gitMergeMaster  = 'gitMergeMaster'
    String gitMergeDevelop = 'gitMergeDevelop'
    String gitTagMaster    = 'gitTagMaster'

 
        String[] stages = [
        gitDiff,
        nexusDownload,
        run,
        test,
        gitMergeMaster,
        gitMergeDevelop,
        gitTagMaster
    ]

    String[] currentStages = []

        currentStages = stages

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    // gitDiff
    if (currentStages.contains(gitDiff)) {
        stage(gitDiff) {
            CURRENT_STAGE = gitDiff
            figlet CURRENT_STAGE
            // TODO: definir stage
        }
    }

    // nexusDownload
    if (currentStages.contains(nexusDownload)) {
        stage(nexusDownload) {
            CURRENT_STAGE = nexusDownload
            figlet CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: env.NEXUS_CRED, variable: 'NEXUS_CREDENTIALS')]) {
                sh 'curl -u ${NEXUS_CREDENTIALS} "${env.NEXUS_URL}/repository/${env.NEXUS_REPO_NAME}/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
            }
        }
    }
    
    // run
    if (currentStages.contains(run)) {
        stage(run) {
            CURRENT_STAGE = run
            figlet CURRENT_STAGE
            sh 'java -jar DevOpsUsach2020-0.0.1.jar &'
            sleep 20
        }
    }
    
    // test
    if (currentStages.contains(test)) {
        stage(test) {
            CURRENT_STAGE = test
            figlet CURRENT_STAGE
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    // gitMergeMaster
    if (currentStages.contains(gitMergeMaster)) {
        stage(gitMergeMaster) {
            CURRENT_STAGE = gitMergeMaster
            figlet CURRENT_STAGE
            // TODO: definir stage
            // def git = new helpers.Git()
            // git.merge("${env.GIT_LOCAL_BRANCH}",'main')
            // println "${env.STAGE_NAME} realizado con exito"

        }
    }
    
    // gitMergeDevelop
    if (currentStages.contains(gitMergeDevelop)) {
        stage(gitMergeDevelop) {
            CURRENT_STAGE = gitMergeDevelop
            figlet CURRENT_STAGE
            // TODO: definir stage
            // def git = new helpers.Git()
            // git.merge("${env.GIT_LOCAL_BRANCH}",'develop')
            // println "${env.STAGE_NAME} realizado con exito"
        }
    }
    
    // gitTagMaster
    if (currentStages.contains(gitTagMaster)) {
        stage(gitTagMaster) {
            CURRENT_STAGE = gitTagMaster
            figlet CURRENT_STAGE
            // TODO: definir stage
            // git.tag(env.GIT_LOCAL_BRANCH)
            // println "${env.STAGE_NAME} realizado con exito"
        }
    }
}

void runCi() {
    String stageBuild = 'buildAndTest'
    String stageSonar = 'sonar'
    String stageRun = 'runJar'
    String stageTestRun = 'rest'
    String stageNexus = 'nexusCI'
    String stageCreateRelease = 'gitCreateRelease'
    String[] stages = []

    if (pipelineType == 'CI-Feature'){
        stages = [
            stageBuild,
            stageRun,
            stageSonar,
            stageNexus
        ]
    }else if (pipelineType == 'CI-Develop')
    {
        stages = [
            stageBuild,
            stageRun,
            stageSonar,
            stageNexus,
            stageCreateRelease 
        ]
    }

    String[] currentStages = stages
 
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
    if (currentStages.contains(stageRun)) {
        stage(stageRun) {
            CURRENT_STAGE = stageRun
            figlet CURRENT_STAGE
            sh './gradlew bootRun &'
            sleep 20
        }
    }
    if (currentStages.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            figlet CURRENT_STAGE
            String scannerHome = tool 'sonar-scanner'
            withSonarQubeEnv( env.SONAR_SERVER_NAME ) {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-gradle -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
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
            nexusPublisher nexusInstanceId: env.NEXUS_INSTANCE_ID,
            nexusRepositoryId: env.NEXUS_REPO_NAME,
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

    if (currentStages.contains(stageCreateRelease)) {
        stage(stageCreateRelease) {
            CURRENT_STAGE = stageCreateRelease
            figlet CURRENT_STAGE
            // TODO: definir stage
            def git = new helpers.Git()
            String version = 'v1.2.1'
            git.release(version)
             println "${env.STAGE_NAME} realizado con exito"
        }
    }
}

return this

void call( String buildTool = "maven" ) {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = ''
        }
// Para el Lab este parametro se debe identitificar por la rama
/*
        parameters {
            string defaultValue: '', description: 'Stages a ejecutar', name: 'stage'
        }
*/
        stages {
            stage('pipeline') {
                steps {
                    script {
                        if (buildTool == 'maven') {
                            maven.call(getPipelineType())
                        } else {
                            gradle.call(getPipelineType())
                        }
                    }
                }
            }
        }
        post {
            success {
                slackSend(color: '#00FF00', message: '[gamboa][' + env.JOB_NAME + '][' + buildTool + '] Ejecución Exitosa.')
            }
            failure {
                slackSend(color: '#FF0000', message: '[gamboa][' + env.JOB_NAME + '][' + buildTool + '] Ejecución Fallida en Stage [' + env.CURRENT_STAGE + '].')
            }
        }
    }
}

/*
String[] getStepsToRun() {
    String[] stepsToRun = params.stage.split(';')
    return stepsToRun
}
*/
String getPipelineType() {
    if (env.GIT_BRANCH.contains('feature-'))
        return 'CI-Feature'
    else if (env.GIT_BRANCH.equals('develop'))
        return 'CI-Develop'
    else if (env.GIT_BRANCH.contains('release-'))
        return 'CD'
}

return this

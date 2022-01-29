void call() {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = 'inicio'
        }

        parameters {
            choice choices: ['gradle', 'maven'], description: 'indicar la herramienta de construcción', name: 'buildTool'
            string defaultValue: '', description: 'Stages a ejecutar', name: 'stage'
        }
        stages {
            stage('pipeline') {
                steps {
                    script {
                        String[] mySteps = params.stage.split(';')
                        // si es '', mySteps[0] = ''
                        // si 'test' solo un valor mySteps[0] = 'valor'
                        // si 'buid;test' solo un valor mySteps[0] = 'build'

                        if (params.buildTool == 'maven') {
                            maven.call(mySteps)
                        } else {
                            gradle.call(mySteps)
                        }
                    }
                }
            }
        }
        post {
            success {
                slackSend(color: '#00FF00', message: '[gamboa][' + env.JOB_NAME + '][' + params.buildTool + '] Ejecución Exitosa.')
            }
            failure {
                slackSend(color: '#FF0000', message: '[gamboa][' + env.JOB_NAME + '][' + params.buildTool + '] Ejecución Fallida en Stage [' + CURRENT_STAGE + '].')
            }
        }
    }
}

return this

def call() {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = 'inicio'
        }

        parameters {
            choice choices: ['gradle', 'maven'], description: 'indicar la herramienta de construcción', name: 'buildTool'
        }
        stages {
            stage('pipeline') {
                steps {
                    script {
                        if (buildTool != null) {
                            def buildToolGroovy = buildTool + '.groovy'
                            def ejecucion = load buildToolGroovy
                            ejecucion.call()
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
                slackSend(color: '#FF0000', message: '[gamboa][' + env.JOB_NAME + '][' + buildTool + '] Ejecución Fallida en Stage [' + CURRENT_STAGE + '].')
            }
        }
    }
}

return this

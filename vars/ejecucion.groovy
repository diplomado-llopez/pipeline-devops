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
                        String [] steps = params.stage.split(';')
                        
                        for( String values : str ) {
                            println(values)
                        }

                        if (params.buildTool == 'maven') {
                            maven.call(steps)
                        } else {
                            gradle.call(steps)
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

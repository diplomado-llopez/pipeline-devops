package helpers

def merge(String ramaOrigen, String ramaDestino) {

    withCredentials([gitUsernamePassword(credentialsId: 'jenkins-gh-ganvoa', gitToolName: 'Default')]) {
        sh "git fetch --all"

        checkout(ramaOrigen)

        checkout(ramaDestino)

        sh """
            git merge ${ramaOrigen}
            git push origin ${ramaDestino}
        """
    }
}

def tag(String rama) {

}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout -b ${rama}; git pull origin ${rama}"
}

return this
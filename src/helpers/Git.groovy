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

def tag(String version) {
    sh "git checkout origin/main; git tag ${version}"
}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout ${rama}; git pull origin ${rama}"
}

def release(String version) {

    withCredentials([gitUsernamePassword(credentialsId: 'girhub-credentials-pass',
                 gitToolName: 'default')]){
                         sh "git reset --hard HEAD; git checkout -b release-${version}; git push origin release-${version}"
                         tag(version)
                 }

}

return this
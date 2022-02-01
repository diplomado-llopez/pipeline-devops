package helpers

def merge(String ramaOrigen, String ramaDestino) {

    sh "git fetch --all"

    checkout(ramaOrigen)

    checkout(ramaDestino)

    withCredentials([usernamePassword(credentialsId: 'jenkins-gh-ganvoa', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh '''
            printf "machine github.com\nlogin $GIT_USERNAME\n password $GIT_PASSWORD" >> ~/.netrc
            git merge ${ramaOrigen}
            git push origin ${ramaDestino}
        '''
    }
}

def tag(String rama) {

}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout ${rama}; git pull origin ${rama}"
}

return this
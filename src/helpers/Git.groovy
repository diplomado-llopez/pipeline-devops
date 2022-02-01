package helpers

def merge(String ramaOrigen, String ramaDestino) {

    sh "git fetch --all"

    checkout(ramaOrigen)

    checkout(ramaDestino)

    sh """
        git merge ${ramaOrigen}
        git push origin ${ramaDestino}
    """
}

def tag(String rama) {

}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout ${rama}; git pull origin ${rama}"
}

return this
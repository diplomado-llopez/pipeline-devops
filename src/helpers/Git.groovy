package helpers

def merge(String ramaOrigen, String ramaDestino) {

    checkout(ramaOrigen)

    checkout(ramaDestino)

    sh """
        git merge ${ramaOrigen}
        git commit -m "Merge de rama ${ramaOrigen}"
        git push origin ${ramaDestino}
    """
}

def tag(String rama) {

}

def checkout(String rama) {
    sh "git reset --hard HEAD; git checkout -b origin/${rama} ${rama}; git pull origin ${rama}"
}

return this
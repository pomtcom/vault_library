package org.demo

def checkOutFrom(repo) {
  git url: "git@github.com:jenkinsci/${repo}"
}

def echoHelloWorld(){
    print 'THIS IS ECHO HELLO WOLRD from object library' ;
}
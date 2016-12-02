#!/bin/bash

#download coreNLP and install it into the local repository

mvn install:install-file -Dfile=stanford-corenlp-3.7.0.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp -Dversion=3.7.0 -Dpackaging=jar

mvn install:install-file -Dfile=stanford-corenlp-3.7.0-models.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp -Dversion=3.7.0 -Dclassifier=models -Dpackaging=jar

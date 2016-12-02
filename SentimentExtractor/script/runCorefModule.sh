#!/bin/bash

input_dir=$1
output_dir=$2

java -cp SentimentExtractor-1.0-SNAPSHOT-jar-with-dependencies.jar project.nlp.sentimentextract.cli.CoreferenceRunner -d ${input_dir} -o ${output_dir}

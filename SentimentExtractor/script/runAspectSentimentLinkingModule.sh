#!/bin/bash

input_dir=$1
output_dir=$2
is_pos='Y'

java -cp SentimentExtractor-1.0-SNAPSHOT-jar-with-dependencies.jar project.nlp.sentimentextract.cli.MovieReviewsRunner -d ${input_dir} -o ${output_dir} -pos ${is_pos}

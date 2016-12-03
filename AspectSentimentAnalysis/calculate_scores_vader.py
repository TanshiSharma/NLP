'''
Created on Nov 26, 2016

@author: Minu
'''
from __future__ import print_function

import glob
import ntpath
from os import walk
import sys

from nltk.corpus import sentiwordnet as swn
from nltk.sentiment import SentimentAnalyzer
from nltk.sentiment.vader import SentimentIntensityAnalyzer

from task2_tool import * 
import task2_tool


movie_score = dict()
    
def analyze_movie_score(movie_name, score_map, count_map):
    weighted_score = 0.0
    total_count = 0.0
    for aspect in score_map.keys():
        if count_map.get(aspect,0) != 0:
            weighted_score += score_map[aspect]
            total_count += count_map.get(aspect,0)
    return ((weighted_score/total_count)+0.5)*10
    
def getAspectTuple(count_map, score_map, aspect_name):
    return ( "s: "+str(score_map.get(aspect_name,0)), "c: "+str(count_map.get(aspect_name,0)));


def analyze_review_score(review):
    analyzer = SentimentIntensityAnalyzer()
    scentance = ""
    for sentiment in review["sentiment"].split(" "):
        scentance += sentiment.split("/")[0]+" "
    sentiment = analyzer.polarity_scores(scentance)
    return (sentiment)["compound"];

# task2_dir = "C:\\PERSONAL\\STUDY\\544_NLP\\Project\\aspect_sentiment_linking_output\\data\\output_linking"
task2_dir = sys.argv[1]
contents = list(get_data(task2_dir));

movie_filenames = sorted(glob.glob(os.path.join(task2_dir, "*.csv")))

with open('results.csv', 'wb') as csvfile:
    fieldnames = ['movie_name', 'movie_score','imp_aspect','movie_cast','movie_music','movie_director','movie_story','movie_scene']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    
    for movie_filename in movie_filenames:
        most_imp_aspect = ""
        highest_count = 0
        review_list = get_data_from_filename(movie_filename)
        score_map = dict()
        count_map = dict()
        
        for review in review_list:
            aspect = review["aspect"]
            score = analyze_review_score(review)
            if score and score != 0:
                count_map[aspect] = count_map.get(aspect, 0) + 1
                score_map[aspect] = score_map.get(aspect, 0) + score
        
        movie_score[movie_filename] = analyze_movie_score(movie_filename, score_map, count_map)
        maximum = max(count_map, key=count_map.get)
        
        writer.writerow({'movie_name': ntpath.basename(movie_filename)
                         , 'movie_score': movie_score[movie_filename]
                         ,'imp_aspect':maximum
                         ,'movie_cast':getAspectTuple(count_map, score_map,"MOVIE_CAST")
                         , 'movie_director':getAspectTuple(count_map, score_map,"MOVIE_DIRECTOR") 
                         ,'movie_story':getAspectTuple(count_map, score_map,"MOVIE_STORY")
                         , 'movie_music':getAspectTuple(count_map, score_map,"MOVIE_MUSIC")
                         , 'movie_scene':getAspectTuple(count_map, score_map,"MOVIE_SCENE")})
    
    print("MOVIE_SCORE: "+ str(movie_score[movie_filename]))

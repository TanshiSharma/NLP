'''
Created on Nov 26, 2016

@author: Minu
'''
from os import walk
import sys
import task2_tool
from task2_tool import * 
import glob
from nltk.corpus import sentiwordnet as swn
import ntpath


movie_score = dict()

# getting the AFFIN list as a Map. Gives a score between -5 and 5. Needs to be normalized
afinn = dict(map(lambda (k, v): (k, int(v)),
                     [ line.split('\t') for line in open("AFINN-111.txt") ]))

def convert_to_sentinet_pos(pos):
    # n- noun v- verb a- adjective s- adjective satellite r- adverb 
    return {
            "NN":"n",
            "JJ":"a",
            "RB":"r",
            "VBZ":"v",
            "VBN":"v",
            "JJR":"a",
            "VBD":"v",
            "PRP":"n",
            "NNP":"n",
            "NNS":"n",
            "JJS":"a",
            "RBR":"r",
            "RBS":"r",
            }.get(pos,"0")
    
def analyze_sentinet_score(word, pos):
    if pos == "0":
        return None
    
#     SentiWordNet does not accept words with special characters
    if set('[~!@#$%^&*(.?)_+{}":;\']+$').intersection(word):
        return None
    
    try:
        sentiment = swn.senti_synsets(word, pos)#swn.senti_synsets(word+'.'+pos+'.01')
    except ValueError:
        print(ValueError)
        return None
    
    if sentiment:
        pos_score = sentiment[0].pos_score()
        neg_score = sentiment[0].neg_score()
        
        if pos_score > neg_score:
            return pos_score
        else:
            return neg_score

def analyze_movie_score(movie_name, score_map, count_map):
    weighted_score = 0.0
    total_count = 0.0
    for aspect in score_map.keys():
#         print("Aspect: "+aspect+" Score: "+ str(score_map[aspect]))
        if count_map.get(aspect,0) != 0:
            weighted_score += score_map[aspect]
            total_count += count_map.get(aspect,0)
    return ((weighted_score/total_count)+1)*5
    
def getAspectTuple(count_map, score_map, aspect_name):
    return ( "s: "+str(score_map.get(aspect_name,0)), "c: "+str(count_map.get(aspect_name,0)));


def analyze_review_score(review):
    net_sentiment = 0
    review_useful = False
    word_no = 0
    for sentiment in review["sentiment"].split(" "):
        word = sentiment.split("/")[0]
        pos = sentiment.split("/")[1]
        affin_score_4_word = afinn.get(word.lower(), None)
        sentinet_score_4_word = analyze_sentinet_score(word, convert_to_sentinet_pos(pos))
#         Taking the average score obtained from both the methods
#         if affin_score_4_word !=0 and sentinet_score_4_word:
#             word_score = (affin_score_4_word + sentinet_score_4_word)/2
        if sentinet_score_4_word and sentinet_score_4_word!= 0:
            net_sentiment += sentinet_score_4_word
            review_useful = True
            word_no += 1
        elif affin_score_4_word and affin_score_4_word!= 0:
            net_sentiment += affin_score_4_word/10.0
            review_useful = True
            word_no += 1
    if review_useful == True:
        return net_sentiment/word_no
    return None

# task2_dir = "C:\\PERSONAL\\STUDY\\544_NLP\\Project\\aspect_sentiment_linking_output\\data\\output_linking"
task2_dir = sys.argv[1]
contents = list(get_data(task2_dir));

movie_filenames = sorted(glob.glob(os.path.join(task2_dir, "*.csv")))

with open('results.csv', 'wb') as csvfile:
    fieldnames = ['movie_name', 'movie_score', 'imp_aspect','movie_general','movie_cast','movie_music','movie_director','movie_story','movie_scene']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    
    for movie_filename in movie_filenames:
        review_list = get_data_from_filename(movie_filename)
        score_map = dict()
        count_map = dict()
        
        for review in review_list:
            aspect = review["aspect"]
            score = analyze_review_score(review)
            if score and score != 0:
                count_map[aspect] = count_map.get(aspect, 0) + 1
                score_map[aspect] = score_map.get(aspect, 0) + score
#                 if count_map[aspect] == 100:
#                     break                
        
        movie_score[movie_filename] = analyze_movie_score(movie_filename, score_map, count_map)
        maximum = max(count_map, key=count_map.get)
        
        writer.writerow({'movie_name': ntpath.basename(movie_filename) , 'movie_score': movie_score[movie_filename]
                         ,'imp_aspect': maximum
                         ,'movie_general':getAspectTuple(count_map, score_map,"MOVIE_GENERAL")
                         ,'movie_cast':getAspectTuple(count_map, score_map,"MOVIE_CAST")
                         , 'movie_director':getAspectTuple(count_map, score_map,"MOVIE_DIRECTOR") 
                         ,'movie_story':getAspectTuple(count_map, score_map,"MOVIE_STORY")
                         , 'movie_music':getAspectTuple(count_map, score_map,"MOVIE_MUSIC")
                         , 'movie_scene':getAspectTuple(count_map, score_map,"MOVIE_SCENE")})
    
    print("MOVIE_SCORE: "+ str(movie_score[movie_filename]))

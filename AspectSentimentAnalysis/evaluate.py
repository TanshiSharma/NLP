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

""" We will be considering predicted scores of 6 and above to be positive. 
Also we consider imdb scores of 7 and above as positive  """

movie_score = dict()
true_scores = dict()

correctly_classified_as_pos = 0
correctly_classified_as_neg = 0
classified_as_pos = 0
classified_as_neg = 0
belongs_in_pos = 0
belongs_in_neg = 0

movie_not_found = 0

pred_file = "C:\\PERSONAL\\STUDY\\544_NLP\\Project\\results_nltk.csv"
# pred_file = sys.argv[1]
pred_list = get_data_from_filename(pred_file)

true_file = "C:\\PERSONAL\\STUDY\\544_NLP\\Project\\movie_list_with_director.csv"
# true_file = sys.argv[2]
true_list = get_data_from_filename(true_file)

for movie in true_list:
    true_scores[movie["titleId"]] = movie["reviewScore"] 

for movie_filename in pred_list:
    pred_score = round(float(movie_filename["movie_score"]),1)
    true_score = float(true_scores.get(movie_filename["movie_name"][:-4],0))
    if true_score == 0:
        continue
    if true_score >= 7:
        if pred_score >= 7:
            classified_as_pos += 1
            correctly_classified_as_pos += 1
        else:
            classified_as_neg += 1
        belongs_in_pos += 1
    else:
        if pred_score < 7:
            classified_as_neg += 1
            correctly_classified_as_neg += 1
        else:
            classified_as_pos += 1
        belongs_in_neg += 1
        
print("correct pos" + str(correctly_classified_as_pos))
print("correct neg" +str(correctly_classified_as_neg))
print("classify pos" + str(classified_as_pos))
print("classify neg" + str(classified_as_neg))
print("is pos" + str(belongs_in_pos))
print("is neg" + str(belongs_in_neg))

#EVALUATION
precision_pos = float(correctly_classified_as_pos) /float(classified_as_pos)
precision_neg = float(correctly_classified_as_neg) / float(classified_as_neg)
recall_pos = float(correctly_classified_as_pos) /float( belongs_in_pos)
recall_neg = float(correctly_classified_as_neg) / float(belongs_in_neg)
fscore_pos = float((2 * precision_pos * recall_pos) / (precision_pos + recall_pos))
fscore_neg = float((2 * precision_neg * recall_neg) / (precision_neg + recall_neg))


classified_as_pos = 0
classified_as_neg = 0
belongs_in_pos = 0
belongs_in_neg = 0

print("Precision    Recall    F1")
print("Pos" + str(round(precision_pos,2)) + "     " + str(round(recall_pos,2)) + "    " + str(round(fscore_pos,2)))
print("Neg" + str(round(precision_neg,2)) + "     " + str(round(recall_neg,2)) + "    " + str(round(fscore_neg,2)))
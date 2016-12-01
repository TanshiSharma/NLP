import nltk.metrics
from nltk.corpus import movie_reviews
import os
import csv


top_words = []


def word_feats(words):

    return dict([(word, True) for word in words])


def words_feats(words):
    return dict([(word, True) if word in top_words else (word,False) for word in words.split()])



def train():
    #get all negative movie ids
    negids = movie_reviews.fileids('neg')
    posids = movie_reviews.fileids('pos')
    print(type((word_feats(movie_reviews.words(fileids=['neg/cv000_29416.txt'])), 'neg')))
    #convert into feature set
    negfeats = [(word_feats(movie_reviews.words(fileids=[f])), 'neg') for f in negids]
    posfeats = [(word_feats(movie_reviews.words(fileids=[f])), 'pos') for f in posids]

    trainfeats = negfeats + posfeats
    print(type(trainfeats[0]))
    
    classifier = nltk.NaiveBayesClassifier.train(trainfeats)
    return classifier


def find_top_words():
    #classifier.show_most_informative_features(10)
    all_words = [word for word in movie_reviews.words()]
    all_words = nltk.FreqDist(all_words)
    top_words = list(all_words.keys())[:50000]
    return top_words


def test(classifier):
    count = 0
    with open('Naive Bayes results.csv','w') as out:
        out.write('File Name,IMDB,Classified\n')
    for dirs, subdir, files in os.walk('D:/Projects/NLP/imdb/'):
        for file in files:
            path = os.path.join(dirs, file)
            directory = dirs.split('/')
            category  = directory[len(directory)-1]
            with open(path, 'r',encoding='utf-8') as infile:
                reader = csv.reader(infile)
                for test_sentence in reader:
                    try:
                        # Tokenize the line.
                        doc = test_sentence[0].lower().split()
                        score = test_sentence[1]
                        if float(score)>=7:
                            posneg = 'pos'
                        else:
                            posneg = 'neg'
                        featurized_doc = {i: (i in doc) for i in top_words}
                        tagged_label = classifier.classify(featurized_doc)
                        with open('Naive Bayes results.csv', 'a') as out:
                            out.write(category + "," + posneg + ',' + tagged_label + "\n")
                    except IndexError:
                        print(path)

            count += 1
            if count%50 == 0:
                print(count)
    return


if __name__ == '__main__':
    top_words = find_top_words()
    print('classify')
    classifiers = train()
    print('test')
    test(classifiers)

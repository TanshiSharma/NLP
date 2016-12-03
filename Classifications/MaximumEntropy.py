import nltk.metrics
from nltk.corpus import movie_reviews
import os
import csv


top_words = []


def word_feats(words):
    return dict([(word, True) for word in words])



def train():
    #get all negative movie ids
    negids = movie_reviews.fileids('neg')
    posids = movie_reviews.fileids('pos')
    print(type((word_feats(movie_reviews.words(fileids=['neg/cv000_29416.txt'])), 'neg')))
    #convert into feature set
    negfeats = [(word_feats(movie_reviews.words(fileids=[f])), 'neg') for f in negids]
    posfeats = [(word_feats(movie_reviews.words(fileids=[f])), 'pos') for f in posids]

    negcutoff = len(negfeats)*3/4
    poscutoff = len(posfeats)*3/4


    trainfeats = negfeats + posfeats
    print(len(trainfeats[0]))
    #testing_set = negfeats[int(negcutoff):] + posfeats[int(poscutoff):]
    #classifier = nltk.MaxentClassifier.train(trainfeats)
    print(trainfeats[0])
    algorithm = nltk.classify.MaxentClassifier.ALGORITHMS[0]
    classifier = nltk.MaxentClassifier.train(trainfeats, algorithm,max_iter=3)

    return classifier


def find_top_words():
    #classifier.show_most_informative_features(10)
    all_words = [word for word in movie_reviews.words()]
    all_words = nltk.FreqDist(all_words)
    top_words = list(all_words.keys())[:50000]
    return top_words


def test(classifier):
    count = 0
    with open('Maximum Entropy results.csv','w') as out:
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
                        with open('Maximum Entropy results.csv', 'a') as out:
                            out.write(category + "," + posneg + ',' + tagged_label + "\n")
                    except IndexError:
                        print(path)

            count += 1
            if count%50 == 0:
                print(count)
    return


def classification_accuracy(movie):
    review = dict()
    total = dict()
    correct = dict()
    with open('Maximum Entropy results.csv','r') as infile:
        reader = csv.reader(infile)
        next(reader)
        for row in reader:
            name = row[0]
            #imdb = row[1]
            naive = row[2]
            if name in total:
                total[name]+=1
            else:
                total[name] = 1
            if naive == 'pos':
                if name in correct:
                    correct[name] +=1
                else:
                    correct[name] = 1
    for name in total:
        review[name] = float(correct[name]/total[name])*100

    with open('Maximum entropy accuracy.csv','w') as out:
        out.write('Movie Name,Positive reviews, Negative Reviews\n')
        for data in total:
            if review[data] > (100-review[data]):
                val = 'Positive'
            else:
                val = 'Negative'
            out.write(data.replace('reviews_','') + ',' + str(review[data]) + ',' + str(100 - review[data]) + ',' + val +'\n')

    return


def movie_names():
    names = dict()
    score = dict()
    with open('movie_list_with_director.csv','r') as infile:
        reader = csv.reader(infile)
        next(reader)
        for row in reader:
            name = row[3].strip()
            cal = row[0].strip()
            name = name.replace(',','')
            title = row[4].strip()
            names[title] = name
            cal = float(cal)
            score[title] = cal
    return score

def moviescore():
    movie_score = dict()
    with open('D:/Projects/NLP/movie_list_with_director.csv','r') as infile:
        reader = csv.reader(infile)
        next(reader)

        for row in reader:
            score = row[0].strip()
            if float(score) >= 7:
                sentiment = 'pos'
            else:
                sentiment = 'neg'
            name = 'reviews_' + row[4].strip()
            movie_score[name] = sentiment
    #print(movie_score)
    return movie_score


if __name__ == '__main__':
    top_words = find_top_words()
    print('movie score')
    #score = moviescore()
    movie = movie_names()
    print('classify')
    classifiers = train()
    print('test')
    #test(classifiers)
    classification_accuracy(movie)
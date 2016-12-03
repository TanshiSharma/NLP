from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.ensemble import RandomForestClassifier
from nltk.corpus import stopwords
import os
import re
import json


def review_to_word_list(review):
    review_text = re.sub("[^a-zA-Z]"," ", review)
    words = review_text.lower().split()
    words = [w for w in words if not w in stops]
    return words


def get_train_data(directory_path):
    train_data = []
    label_data = []
    for directory,_, files in os.walk(directory_path):
        for file_name in files:
            file_path = os.path.join(directory,file_name)
            file = open(file_path, 'r',encoding='utf-8')
            review_text = file.read()
            if 'neg' in file_path:
                label = 0
            else:
                label = 1
            train_data.append(' '.join(review_to_word_list(review_text)))
            label_data.append(label)
    return train_data, label_data


def get_test_data(directory_path, model, vectorizer, output_file):
    out_file = open(output_file, 'w')
    out_file.write('File Name, Num positive classified reviews, Num negative classified review\n')
    for directory,_, files in os.walk(directory_path):
        for file_name in files:
            test_data = []
            test_label = []
            file_path = os.path.join(directory,file_name)
            file = open(file_path, 'r')
            review_text_list = json.loads(file.read())
            for entry in review_text_list:
                review = entry['review']
                test_data.append(' '.join(review_to_word_list(review)))
            test_x = vectorizer.transform(test_data)
            predicted_values = model.predict(test_x)
            positive, negative, match_count, mismatch_count = 0,0,0,0
            for index in range(len(predicted_values)):
                if predicted_values[index]==1:
                    positive += 1
                else:
                    negative += 1
            title_id = file_name.split('.')[0].split('_')[1]
            pos = float(positive*100)/(positive+negative)
            neg = float(negative*100)/(positive+negative)
            if pos > neg:
                val = 'Positive'
            else:
                val = 'Negative'
            out_file.write('%s, %.2f, %.2f, %s\n' %(title_id, pos, neg, val))
    out_file.close()
    print('done')


stops = set(stopwords.words("english"))
stops.add('br')

train_data, train_label = get_train_data('data/train')
vectorizer = TfidfVectorizer( max_features = 40000, ngram_range = ( 1, 3 ), sublinear_tf = True )
train_x= vectorizer.fit_transform(train_data)

# Random forest
forest = RandomForestClassifier(n_estimators = 100)
forest = forest.fit(train_x, train_label)
get_test_data('data/test_reviews', forest, vectorizer, 'random_forest.csv')




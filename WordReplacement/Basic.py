import json
import csv
import os
import collections


title_path_dict={}

def create_review_title_map():
    review_directory_path='/home/tanshi/Downloads/CSCI544_Final_Project/scrapyIMDB/data/reviews'
    global title_path_dict

    for files in os.listdir(review_directory_path):
        files_title=files.split('_')[1].split('.')[0]
        review_path=os.path.join(review_directory_path,files)

        if files_title in title_path_dict:
            continue
        else:
            title_path_dict[files_title]=review_path

#
def read_meta_data():
    global title_path_dict
    csv_file=open('/home/tanshi/Downloads/CSCI544_Final_Project/scrapyIMDB/data/movie_list_with_director.csv','r')
    csv_reader=csv.reader(csv_file)

    for rows in csv_reader:
        if rows[0]=='reviewScore':
            continue
        else:
            movie_actor_list=rows[5].split(',')
            movie_director_list=rows[4].split(',')
            movie_title=rows[3]

        if movie_title in title_path_dict:

            create_replace_name_map(movie_actor_list,movie_director_list,title_path_dict[movie_title],movie_title)

        else:
            print(movie_title)


def create_replace_name_map(actor_list,director_list,reviews_file_path,title_id):


    actor_dict=collections.OrderedDict()
    director_dict=collections.OrderedDict()
    general_dict={' storyline ':' MOVIE_STORY ',' story ':' MOVIE_STORY ',' tale ':' MOVIE_STORY ',' romance ':' MOVIE_STORY ',' dialog':' MOVIE_STORY ',' script ':' MOVIE_STORY ','storyteller':' MOVIE_STORY ',' ending ':' MOVIE_STORY ',\
                  ' storytelling ':' MOVIE_STORY ',' revenge ':' MOVIE_STORY ',' betrayal ':' MOVIE_STORY ',' plot ':' MOVIE_STORY ',' writing ':' MOVIE_STORY ',' twist ':' MOVIE_STORY ',' drama ':' MOVIE_STORY ', \
                  ' scene ': ' MOVIE_SCENE ', ' scenery ': ' MOVIE_SCENE ', ' animation ': ' MOVIE_SCENE ', ' violence ': ' MOVIE_SCENE ',' screenplay ': ' MOVIE_SCENE ', ' action ': ' MOVIE_SCENE ', ' special effect ': ' MOVIE_SCENE ',\
                  ' stunt ': ' MOVIE_SCENE ', ' actor ':' MOVIE_ACTOR ',' actors ':' MOVIE_ACTOR ',' Actor ':' MOVIE_ACTOR ',' actions ':' MOVIE_SCENE',' Action ':' MOVIE_SCENE ',' shot': ' MOVIE_SCENE ', 'visual': ' MOVIE_SCENE ', 'props': ' MOVIE_SCENE ', 'camera': ' MOVIE_SCENE ', 'graphic': ' MOVIE_SCENE ', 'lyric': ' MOVIE_MUSIC ', 'sound': ' MOVIE_MUSIC ', \
                  ' music ': ' MOVIE_MUSIC ',' audio ': ' MOVIE_MUSIC ', ' musical ': ' MOVIE_MUSIC ', ' title track ': ' MOVIE_MUSIC ',' sound effect ': ' MOVIE_MUSIC ', ' sound track ': ' MOVIE_MUSIC ' }


    general_dict=collections.OrderedDict(general_dict)


    for actor in actor_list:
        if actor in actor_dict:
            continue
        else:
            actor_dict[actor]=' MOVIE_CAST '
            actor_name=actor.split(' ')
            for names in actor_name:
                actor_dict[names]=' MOVIE_CAST '


    for director in director_list:
        if director in director_dict:
            continue
        else:
            director_dict[director]=" MOVIE_DIRECTOR "

            director_name=director.split(' ')

            for names in director_name:
                director_dict[names]=" MOVIE_DIRECTOR "


    print("json loading")
    print(reviews_file_path)
    review=json.load(open(reviews_file_path))


    output_list=[]
    output_filepath = 'data/processed_%s.json' % (title_id)
    output = open(output_filepath, 'w')
    punct_list=['!','#',':','$','%','&','(',')','.','*','+','-','.','/',';','<','=','>','?','@','[',',','^','_','`','{','|','}','~',']']
    for reviews in review:

        reviews['review']=reviews['review'].replace('\n',' ')
        reviews['review'] = reviews['review'].replace('<br>', '')
        reviews['review'] = reviews['review'].replace(',', ' ,')
        for punctuations in punct_list:
            reviews['review']=reviews['review'].replace(punctuations,' %s '%punctuations)


        for key,value in director_dict.items():
            reviews['review']=reviews['review'].replace(key,value)


        for key,value in actor_dict.items():
            reviews['review']=reviews['review'].replace(key,value)

        for key,value in general_dict.items():
            reviews['review']=reviews['review'].replace(key,value)
            reviews['review']=reviews['review'].replace(key,value)
        output_list.append(reviews)


    output.write(json.dumps(output_list))
    output.write('\n')



    output.close()


create_review_title_map()
read_meta_data()
#create_replace_name_map(['Hugo Weaving','Natalie Portman','Rupert Graves','Stephen Rea'],['James McTeigue'],'/home/tanshi/Downloads/CSCI544_Final_Project/scrapyIMDB/data/reviews_tt0434409.json','tt0434409')
print(len(title_path_dict))

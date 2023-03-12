#后处理，包括数据格式的处理和空值处理

import pandas as pd
from config import Config
if __name__ =="__main__":
    # time_table = pd.read_csv(Config.time_table_path).astype(str)
    # time_table['movie_time'] = time_table['movie_time'].map(lambda x: '-'.join(x.split('/')))
    # time_table.to_csv("time.csv",index=False)
    
    #写movie_table
    # movie_table = pd.read_csv(Config.movie_table_path).astype(str)
    # movie_table['movie_time'] = movie_table['movie_time'].map(lambda x: '-'.join(x.split('/')))
    # movie_table.to_csv("movie.csv",index=False)
    
    # actor_table = pd.read_csv(Config.actor_table_path).astype(str)
    # actor_table.drop(columns=['actor_id'],inplace=True)
    # actor_table.to_csv("actor.csv",index=False)
    
    # director_table = pd.read_csv(Config.director_table_path).astype(str)
    # director_table.drop(columns=['director_id'],inplace=True)
    # director_table.to_csv(Config.director_table_path,index=False)
    
    # style_table = pd.read_csv(Config.style_table_path).astype(str)
    # style_table.drop(columns=['style_id'],inplace=True)
    # style_table.to_csv(Config.style_table_path,index=False)
    
    # score_table = pd.read_csv(Config.score_table_path).astype(str)
    # #score_table = score_table.fillna(0,inplace=True)
    # score_table = score_table.dropna(axis=0, how='any')
    # score_table.to_csv("score_table.csv",index=False)
    
    score_table = pd.read_csv(Config.score_table_path).astype(str)
    score_table = score_table[(score_table['movie_score']!='nan') & (score_table['positive_rate']!='nan') &(score_table['negative_rate']!='nan')  ] 
    score_table.to_csv("score_table.csv",index=False)
    
    
    actor_table = pd.read_csv(Config.actor_table_path).astype(str)
    actor_table = actor_table[(actor_table['actor_name']!='nan') & (actor_table['movie_id']!='nan') &(actor_table['is_star']!='nan')  ]
    actor_table.to_csv("actor_table.csv",index=False)
    
    director_table = pd.read_csv(Config.director_table_path).astype(str)
    director_table = director_table[(director_table['director']!='nan') & (director_table['movie_id']!='nan') ]
    director_table.to_csv("director_table.csv",index=False)
    
    style_table = pd.read_csv(Config.style_table_path).astype(str)
    style_table = style_table[(style_table['movie_style']!='nan') & (style_table['movie_id']!='nan') ]
    style_table.to_csv("style_table.csv",index=False)
    
    # print(time_table['movie_time'])
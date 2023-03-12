import pandas as pd
from config import Config
import numpy as  np
from tqdm import tqdm
def makeBigTable(data):
    rows = data.shape[0]  # 电影总数
    
    rename_dict = {'release_year':'year','release_month':'month','release_day':'day',\
                    'title':'movie_name','asin_all':'movie_asin','release_time':'movie_time'}
    data.rename(columns=rename_dict, inplace = True)
    data['movie_time'] = data['movie_time'].map(lambda x: '-'.join(x.split('/')))   #将时间格式统一
    
    data['time_id'] = [i for i in range(1,rows+1)]
    data['style_id'] = [i for i in range(1,rows+1)]
    data['score_id'] = [i for i in range(1,rows+1)]
    data['director_id'] = [i for i in range(1,rows+1)]
    data['review_id'] = [i for i in range(1,rows+1)]
    data['movie_id'] = [i for i in range(1,rows+1)]
    return data    
    
def makeTimeTable(data):
    rows = data.shape[0]  # 电影总数
    time_data = data.reindex(columns = ['time_id','movie_name','year','month','day','movie_time'])  
    time_data['season'] = [i for i in range(1,rows+1)]
    time_data['weekday'] = [i for i in range(1,rows+1)]
    for index,row in time_data.iterrows():
        time_data.loc[index,'season']= float(time_data.loc[index,'month']) // 3 + 1
        time_data.loc[index,'weekday']= float(time_data.loc[index,'day']) % 7 + 1
    time_data.to_csv(Config.time_table_path,index=False)
    
def makeFormatTable(data):
    t_format = pd.DataFrame(columns=['movie_id', 'movie_format'])
    for index,row in data.iterrows():
        format_list = data.loc[index,'format'].split(',')
        for format in format_list:
            t_format = t_format.append({'movie_id':data.loc[index,'movie_id'],'movie_format':format},ignore_index=True)
    t_format.to_csv(Config.format_table_path,index=False)
    
def makeStyleTable(data):
    t_style = pd.DataFrame(columns=['style_id', 'movie_style','movie_id'])
    for index,row in data.iterrows():
        style_list = data.loc[index,'style'].split(',')
        for style in style_list:
            t_style = t_style.append({'style_id':data.loc[index,'style_id'],\
                                      'movie_style':style,\
                                      'movie_id':data.loc[index,'movie_id']},ignore_index=True)
    t_style.to_csv(Config.style_table_path,index=False)

def makeDirectorTable(data):
    t_director = pd.DataFrame(columns=['director_id', 'director','movie_id'])
    for index,row in data.iterrows():
        director_list = data.loc[index,'directors'].split(',')
        for director in director_list:
            t_director = t_director.append({'director_id':data.loc[index,'director_id'],\
                                            'director':director,\
                                            'movie_id':data.loc[index,'movie_id']},ignore_index=True)
    t_director.to_csv(Config.director_table_path,index=False)

def makeActorTable(data):
    t_actor = pd.DataFrame(columns=[ 'actor_name','movie_id','is_star'])
    for index,row in data.iterrows():
        #先处理star list
        star_list = data.loc[index,'stars'].split(',')
        for star in star_list:
            t_actor = t_actor.append({
                                      'actor_name':star,\
                                      'movie_id':data.loc[index,'movie_id'],\
                                      'is_star':1},ignore_index=True)
        #再处理actor list
        actor_list = data.loc[index,'actors'].split(',')
        for actor in actor_list:
            if actor not in star_list:  #不在star list中的普通演员可以添加，避免重复
                t_actor = t_actor.append({
                                          'actor_name':actor,\
                                          'movie_id':data.loc[index,'movie_id'],\
                                          'is_star':0},ignore_index=True)
        
    t_actor.to_csv(Config.actor_table_path,index=False)
    
def makeMovieTable(data):  #
    movie_data = data.reindex(columns = ['movie_name','movie_asin','format_num','time_id',\
                                        'movie_time','movie_id'])
    #缺少review_num,movie_score
    movie_data.to_csv(Config.movie_table_path,index=False)
    return movie_data
def makeScoreTableAndCompleteMovieTable(movie_data):
    tmp_df = makeScoreTable(movie_data)  #tmp_df有列：['movie_id', 'review_num', 'movie_score', 'positive_rate','negative_rate']
    makeCompleteMovieTable(movie_data,tmp_df)
    
def makeScoreTable(movie_data):
    asin_col = movie_data['movie_asin']
    movie_id_col = movie_data['movie_id']
    
    #初始化tmp_df，movie_id为movie_data中的movie_id，其他列为0
    tmp_df = pd.DataFrame({'movie_id':movie_id_col,'review_num':0,'movie_score_sum':0,'positive_num':0,'negative_num':0})
    with open(Config.review_data_path, encoding='ISO-8859-1') as f:
        data = f.read()  #读取整个文件
        print("review file is read done")
        all_reviews_list = data.split('\n\n')
        
        for single_review_data in tqdm(all_reviews_list,desc="iterate review data"):
            review_list = single_review_data.split('\n')
            try:
                asin_list = list(filter(lambda ele : "product/productId" in ele, review_list))
                asin = asin_list[0].split(':')[-1].strip()  #直接选取第一个
                review_helpfulness_list = list(filter(lambda ele : "review/helpfulness" in ele, review_list))
                try:
                    review_helpfulness = eval(review_helpfulness_list[0].split(':')[-1].strip())  #review/helpfulness
                except ZeroDivisionError:
                    review_helpfulness = 0
                review_score_list = list(filter(lambda ele : "review/score" in ele, review_list))
                review_score = float(review_score_list[0].split(':')[-1].strip())  #review/score
            except Exception as e:  #出错直接跳过
                print(e.args)
                print("asin:",asin)
                print("review_helpfulness:",review_helpfulness)
                print("review_score:",review_score)
                continue
            
            #处理tmp_df
            countRow = 0
            for ele in asin_col.values:  #扫描每一行
                if asin in ele:          #如果asin在movie_asin的该行中，用countRow记录该行的index
                    tmp_df.loc[countRow,'review_num'] = tmp_df.loc[countRow,'review_num'] + 1
                    tmp_df.loc[countRow,'movie_score_sum'] = tmp_df.loc[countRow,'movie_score_sum'] + review_score
                    if review_helpfulness > 0.5:
                        tmp_df.loc[countRow,'positive_num'] = tmp_df.loc[countRow,'positive_num'] + 1
                    else:
                        tmp_df.loc[countRow,'negative_num'] = tmp_df.loc[countRow,'negative_num'] + 1
                countRow = countRow + 1
    """"读取review过程中，需要拿到review_num，movie_score
    review_num：直接计数，扫描到该asin对应的movie，直接在对应列加1
    movie_score:直接计数加分，扫描到该asin对应的movie，直接在对应列加1，得到总的review_score的和，然后除以review_num作为movie_score
    positive_rate：将正面评价（review_helpfulness>0.5）的加和起来，得到总的positive_num的和，然后除以review_num作为positive_rate
    negative_rate：同理"""

    #tmp_df改名
    tmp_df.rename(columns={'movie_score_sum':'movie_score','positive_num':'positive_rate','negative_num':'negative_rate'},inplace=True)
    tmp_df[['movie_score','positive_rate','negative_rate']] = tmp_df[['movie_score','positive_rate','negative_rate']].div(tmp_df['review_num'],axis=0)
    #tmp_df有列：['movie_id', 'review_num', 'movie_score', 'positive_rate','negative_rate']
    t_score = tmp_df[['movie_id','movie_score','positive_rate','negative_rate']]
    t_score.to_csv(Config.score_table_path,index=False)

    return tmp_df


def makeCompleteMovieTable(movie_data,tmp_df):
    movie_data = pd.merge(movie_data,tmp_df,on='movie_id',how='left')
    movie_data.drop(columns=['positive_rate','negative_rate'],inplace=True)
    movie_data.to_csv(Config.movie_table_path,index=False)
    
if __name__=="__main__":
    
    print("start")
    ori_data = pd.read_csv(Config.input_data_path, encoding='ISO-8859-1').astype(str)
    data = makeBigTable(ori_data)
    makeTimeTable(data)
    makeFormatTable(data)
    makeStyleTable(data)
    makeDirectorTable(data)
    makeActorTable(data)
    
    ori_data = pd.read_csv(Config.input_data_path, encoding='ISO-8859-1').astype(str)
    data = makeBigTable(ori_data)
    makeMovieTable(data)
    print("movie data read OK!!")
    ori_movie_data = pd.read_csv(Config.movie_table_path, encoding='ISO-8859-1').astype(str)
    makeScoreTableAndCompleteMovieTable(ori_movie_data)
    print("end")
    
    
    
    

import os

class Config:
    curPath = os.path.abspath(__file__)
    rootPath = os.path.split(curPath)[0]
    
    input_data_path = os.path.join(rootPath,"final_movie_info_overall_whole.csv")
    review_data_path = os.path.join(rootPath,"movies.txt")
    
    time_table_path = os.path.join(rootPath,"table_output/time_table.csv")
    format_table_path = os.path.join(rootPath,"table_output/format_table.csv")
    style_table_path = os.path.join(rootPath,"table_output/style_table.csv")
    director_table_path = os.path.join(rootPath,"table_output/director_table.csv")
    actor_table_path = os.path.join(rootPath,"table_output/actor_table.csv")
    movie_table_path = os.path.join(rootPath,"table_output/movie_table.csv")
    review_table_path = os.path.join(rootPath,"table_output/review_table.csv")
    score_table_path = os.path.join(rootPath,"table_output/score_table.csv")
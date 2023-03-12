def transformData(df):
    all_columns = df.columns    
    print('==========================================================')
    print(all_columns)
    res_list=df.collect()
    print('==========================================================')
    print(type(res_list))
    print('==========================================================')
    result=splitRowData(all_columns,res_list)
    return result

import pandas as pd
import json
if __name__=="__main__":
    data = {
    'one':pd.Series([1,2,3],index = ['a','b','c']),
    'two':pd.Series([1,2,3,4],index = ['a','b','c','d'])
    }
    df = pd.DataFrame(data)
    print(df.values)
    df_dict = df.to_dict()
    json_str = json.dumps(df_dict)
    print(json_str)
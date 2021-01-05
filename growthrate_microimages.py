import numpy as np
import pandas as pd
import matplotlib.pyplot as pl

# read cell profiler data
cellprofilerinput = pd.read_csv('C:\\Users\\catie\\Documents\\code\\Bingalls\\cell profiler\\Cellprofiler outpt\\IdentifyPrimaryObjects_Red_img12.csv')

# grab only the relevant columns
data = cellprofilerinput[['ImageNumber','ObjectNumber','AreaShape_MajorAxisLength']]

# scale all the cells in image 1 by 1/(1800*12.33) to convert to micrometers from pixels
img1 = data[data['ImageNumber']==1]['AreaShape_MajorAxisLength'].apply(lambda x: x/(1800 * 12.33))

# scale all the cells in image 2, also get rid of all cells past 137 
# (since they are new cells and didn't grow from the previous image)
img2 = data[(data['ImageNumber']==2) & (data['ObjectNumber']<137)]['AreaShape_MajorAxisLength'].apply(lambda x: x/(1800 * 12.33)).reset_index(drop=True)
                                                                                           
# 1800 is a conversion from 30 mins to seconds and 12.33 is converting from pixels to micrometers
# (it is the geometric mean of the conversions for the x and y coordinates of the images - 13.76 and 11.04)                                                                                  

# subtract the size of the cells in image 1 from the cells in image 2
diff=img2.subtract(img1)

# convert data to pandas dataframe format
diff=diff.to_frame()

# rename column which was previously containing raw cell lengths
diff=diff.rename(columns={'AreaShape_MajorAxisLength':'GrowthRate'})

# throw out cells which have bad measurements
diff=diff[diff['GrowthRate']>0.0001]

# print the mean and standard deviation in the growth rate
print(diff['GrowthRate'].mean())
print(diff['GrowthRate'].std())



import numpy as np 
import matplotlib.pyplot as pl 
import pandas as pd 

# load cell profiler output
cpinput = pd.read_csv('C:\\Users\\catie\\Documents\\code\\Bingalls\\EditedObjects.csv')

# grab only rows relevant to image 1
img1 = cpinput[cpinput['ImageNumber']==1]

# grab the needed columns -> position of cell, orientation, and length
img = img1[['AreaShape_Center_X','AreaShape_Center_Y','AreaShape_Orientation','AreaShape_MajorAxisLength']]

#---- Endpoints follow the equation x1 = x_center + R(orientation)*yhat*length/2, x2 = x_center - R(orientation)*yhat*length/2
# where R is a 2d rotation matrix {{cos(x),sin(x)},{-sin(x),cos(x)}}
#---- In the documentation for cellprofiler the orientation is supposed to be in degrees measured from the x axis, however
# the output we got has orientation measured in radians from the y axis.
img.insert(4,'x1_x',img['AreaShape_Center_X']+img['AreaShape_MajorAxisLength'].apply(lambda x: x/2)*(img['AreaShape_Orientation'].apply(lambda x: np.sin(x))),True)
img.insert(5,'x1_y',img['AreaShape_Center_Y']+img['AreaShape_MajorAxisLength'].apply(lambda x: x/2)*(img['AreaShape_Orientation'].apply(lambda x: np.cos(x))),True)

img.insert(6,'x2_x',img['AreaShape_Center_X']-img['AreaShape_MajorAxisLength'].apply(lambda x: x/2)*(img['AreaShape_Orientation'].apply(lambda x: np.sin(x))),True)
img.insert(7,'x2_y',img['AreaShape_Center_Y']-img['AreaShape_MajorAxisLength'].apply(lambda x: x/2)*(img['AreaShape_Orientation'].apply(lambda x: np.cos(x))),True)

# Grab new columns from the array
positions=img[['x1_x','x1_y','x2_x','x2_y']]

print(positions.head())

# Transpose data for use in bsim which requires 4 rows of cell data, one column per cell
data = positions.transpose()

# Save data to .csv file (excel compatible)
data.to_csv('C:\\Users\\catie\\Documents\\code\\Bingalls\\EditedObjects_processed.csv')
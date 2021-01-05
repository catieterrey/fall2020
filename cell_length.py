import numpy as np
import pandas as pd 
import matplotlib.pyplot as pl

posns = np.genfromtxt('C:\\Users\\catie\\Documents\\code\\Bingalls\\oufti\\ICsframe5.csv',delimiter=',')
print(posns.shape)
sizes=(((posns[0,:]-posns[2,:])**2 + (posns[1,:]-posns[3,:])**2)**0.5)
pl.hist(sizes)
pl.axvline(sizes.mean(), color='k', linestyle='dashed', linewidth=1)
pl.xlabel('length (pixels)')
pl.ylabel('number of bacteria')
pl.show()
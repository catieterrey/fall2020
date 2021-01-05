# Contents

In the main folder, there is a subdirectory called bsim-master as well as three python scripts.

cell_length.py computes the length (in pixels) of all of the cells in an image from the oufti excel output.

growthrate_microimages.py computes the average growth rate of all the cells (and its variance).
The script only uses two images, but could be extended easily to average over more images.

processcellprofiler.py takes an output csv file from CellProfiler and determines the endpoints of each cell. It then
outputs the results in an excel sheet. To use this excel sheet as an input for Java, remove the first column and first row 
and then replace the input path on line 145 of NeighbourInteractions.java.

The folder bsim-master contains a copy of BSim2.0 along with the new java code developed this term.

bsim-master/bsim-master/bsim-master takes you to the main BSim2.0 directory. 
Inside this folder there are a few important things. 

The first of which is the examples folder, which contains the simulation Fall2020. In this folder you will find
NeighbourInteractions.java and Bacterium.java.

Second, there is the src folder. The src folder contains all of the BSim2.0 source code. In src/bsim/capsule you will find
the BSimCapsuleBacterium.java class which is used in our simulations.

Finally, in the bsim-master/bsim-master/bsim-master folder there is also a python script called run_bsim.py which
compiles and runs our simulation. This can be modified so that parameters can be given to the java program from the script.

# Dependencies

To get everything running, you will first need to install IntelliJ IDEA. Once you have done this
you must clone this repository, open the bsim-master folder in IDEA, and then follow the instructions here:
https://github.com/CellSimulationLabs/bsim. Once all of these instructions have been followed, 
select NeighbourInteractions from the drop down menu beside the play button on the top right of IDEA.
Then press play to run a simulation.

Note that there are a few file paths which have to be replaced in the code. The first of which is in NeighbourInteractions.java
on line 145. Replace the path to the csv file with the one on your computer.

run_bsim.py requires openjdk-14.0.2. The install path has to be replaced in the script on line 18.
Note that openjdk-14.0.2 is already installed by IDEA to C:\Users\username\.jdks\openjdk-14.0.2





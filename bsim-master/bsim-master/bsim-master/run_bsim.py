import numpy as np
import subprocess

filepath = 'C:\\Users\\catie\\Documents\\code\\Bingalls\\bsim-master\\bsim-master\\bsim-master\\'
antpath = 'C:\\apache-ant-1.10.1\\apache-ant-1.10.1\\bin\\'
processingpath = 'C:\\processing-3.5.4\\core\\library\\'

# Compiles the java code
subprocess.call(antpath+'ant compile',shell=True,cwd = filepath)

# Runs the java code.
# you must use openjdk-14.0.2 in order to run bsim due to compatibility issues with Processing
# This command is structured like:
# path_to_java\\bin\\java -cp path_to_packages;...;path_to_packages path_to_class
# path_to_java is the path to openjdk-14.0.2, which needs to be installed
# path_to_packages is a path to one of the bsim library files, contained in the lib folder
# path_to_class is the path to the java file that runs the simulation (it should have a function called run())
subprocess.call('C:\\Users\\catie\\.jdks\\openjdk-14.0.2\\bin\\java -cp '+\
            '.;../../lib/core.jar;../../lib/vecmath.jar;../../lib/objimport.jar;../../lib/bsim-osp.jar;../../lib/jcommander-1.49-SNAPSHOT.jar'+
            ' Fall2020/NeighbourInteractions',shell=True,cwd = filepath+'dist\\build')





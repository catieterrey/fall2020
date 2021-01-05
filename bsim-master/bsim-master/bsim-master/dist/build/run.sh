#!/bin/bash
#SBATCH --time=01:00:00
#SBATCH --account=def-bingalls
#SBATCH --mem=4G
#SBATCH --mail-user=caterrey@uwaterloo.ca
#SBATCH --mail-type=ALL
module load java
Xvfb :1 -screen 0 1024x768x24 </dev/null &
export DISPLAY=":1"
java -cp .:../../lib/core.jar:../../lib/vecmath.jar:../../lib/objimport.jar:../../lib/bsim-osp.jar:../../lib/jcommander-1.49-SNAPSHOT.jar Fall2020.NeighbourInteractions

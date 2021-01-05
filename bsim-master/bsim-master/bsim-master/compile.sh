#!/bin/bash
#SBATCH --time=00:15:00
#SBATCH --account=def-bingalls
#SBATCH --mem=4G
#SBATCH --mail-user=caterrey@uwaterloo.ca
#SBATCH --mail-type=ALL
module load java
ant compile
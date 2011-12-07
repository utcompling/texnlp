#!/bin/bash

###############################################################################
## Copyright (C) 2007 Elias Ponvert, The University of Texas at Austin
## 
## This library is free software; you can redistribute it and#or
## modify it under the terms of the GNU Lesser General Public
## License as published by the Free Software Foundation; either
## version 2.1 of the License, or (at your option) any later version.
## 
## This library is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU Lesser General Public License for more details.
## 
## You should have received a copy of the GNU Lesser General Public
## License along with this program; if not, write to the Free Software
## Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
##############################################################################

# Script for cotraining evaluation of TexNLP taggers
# Author: Elias Ponvert <ponvert@mail.utexas.edu>
# Date: 3 June 08

# Set up path variables

if [ -z "$TEXNLP_DIR" ] ; then
    echo "You need to set TEXNLP_DIR"
    exit 1
fi

TEXNLP_BIN=$TEXNLP_DIR/bin
TAG_BIN=$TEXNLP_BIN/ttag.sh
SCORE_BIN=$TEXNLP_BIN/tscore.sh

DATA_DIR=$TEXNLP_DIR/data
TEMP_DIR=$DATA_DIR/temp

# Parameter defaults
MODEL1=HMM
MODEL2=CANDC
ROUNDS=2
EM_ITER=

SEED_DATA=
EVAL_DATA=
DEV_DATA=
RAW_DATA=
COMMENT=

# Retrieve script options
SHORTOPTS="hc1:2:d:e:i:j:m:n:s:r:"
LONGOPTS="help,clean,model1:,model2:,dev:,eval:,iter:,mem:,comment:,rounds:,seed:,raw:"
GETOPTS=`getopt -o $SHORTOPTS -l $LONGOPTS -n 'texnlp-cotrain.bash' -- "$@"`
eval set -- "$GETOPTS"

while true ; do
    case $1 in
        -h|--help)
            echo "Usage: cotrain.sh options"
            echo "  -1, --model1   First model  (HMM, MEL, MEMM)"
            echo "  -2, --model2   Second model (HMM, MEL, MEMM)"
            echo "  -c, --clean    Clean up (remove temporary dirs)"
            echo "  -d, --dev      Untagged data for cotraining"
            echo "  -e, --eval     Evaluation data"
            echo "  -i, --iter     Number of iterations to run EM (for HMM)"
            echo "  -j, --mem      RAM to allocate to the Java taggers"
            echo "  -m, --comment  Comment string to include in output dirname"
            echo "  -n, --rounds   Number of cotraining rounds"
            echo "  -s, --seed     Seed data"
            echo "  -r, --raw      Raw data"
            echo "  -h, --help     Print help"
            exit 0
            ;;
        -c|--clean)  
            rm -r $TEMP_DIR
            exit 0
            ;;
        -1|--model1)  MODEL1=$2            ; shift 2 ;;
        -2|--model2)  MODEL2=$2            ; shift 2 ;;
        -d|--dev)     DEV_DATA="-d $2"     ; shift 2 ;;
        -e|--eval)    EVAL_DATA="-e $2"    ; shift 2 ;;
        -i|--iter)    EM_ITER="-i $2"      ; shift 2 ;;
        -j|--mem)     JAVA_MEM_FLAG=-Xmx$2 ; shift 2 ;;
        -m|--comment) COMMENT=$2           ; shift 2 ;;
        -n|--rounds)  ROUNDS=$2            ; shift 2 ;;
        -s|--seed)    SEED_DATA="-t $2"    ; shift 2 ;;
        -r|--raw)     RAW_DATA="-r $2"     ; shift 2 ;;
        --) shift ; break ;;
        *) echo "Error processing options" ; exit 1 ;;
    esac 
done

# Load TexNLP environment
. texnlp-env

# Make the temporary directory
mkdir -p $TEMP_DIR

# Set up templates for the other directories
WORK_DIR_TEMPLATE=$TEMP_DIR/$MODEL1-$MODEL2
if [ $COMMENT ] 
then
    WORK_DIR_TEMPLATE=$WORK_DIR_TEMPLATE-$COMMENT
fi

# Output experimental header
echo "====================================="
echo TexNLP Cotraining experiment $(date)
echo 
echo Model 1        = $MODEL1
echo Model 2        = $MODEL2
echo Seed           = $SEED_DATA
echo Raw            = $RAW_DATA
echo Eval           = $EVAL_DATA
echo
echo Parameters
echo EM Iterations  = $EM_ITER
echo Cotrain rounds = $ROUNDS

for ((i=0; i < ROUNDS ; i++)) 
do
  WORK_DIR=$WORK_DIR_TEMPLATE-${i}a

  echo "====================================="
  echo Round $((i))a
  echo 
  echo Model = $MODEL1
  echo
  echo $TAG_BIN -m $MODEL1 $EVAL_DATA $SEED_DATA $RAW_DATA $DEV_DATA $MACHINE_LAB $EM_ITER -o $WORK_DIR
  eval $TAG_BIN -m $MODEL1 $EVAL_DATA $SEED_DATA $RAW_DATA $DEV_DATA $MACHINE_LAB $EM_ITER -o $WORK_DIR

  MACHINE_LAB="-l $WORK_DIR/dev.tagged.txt"
  WORK_DIR=$WORK_DIR_TEMPLATE-${i}b

  echo
  echo "====================================="
  echo Round $((i))b
  echo 
  echo Model = $MODEL2
  echo
  echo $TAG_BIN -m $MODEL2 $EVAL_DATA $SEED_DATA $RAW_DATA $DEV_DATA $MACHINE_LAB $EM_ITER -o $WORK_DIR
  eval $TAG_BIN -m $MODEL2 $EVAL_DATA $SEED_DATA $RAW_DATA $DEV_DATA $MACHINE_LAB $EM_ITER -o $WORK_DIR
  echo

  MACHINE_LAB="-l $WORK_DIR/dev.tagged.txt"

done
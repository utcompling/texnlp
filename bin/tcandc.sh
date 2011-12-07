#!/bin/sh

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

if [ -z $CANDC ] 
then 
    echo Please set the CANDC environment variable to where you have the CANDC tools installed. 1>&2
    exit 1
fi

# Clear variables that will be used in program invocations later
DEV_FILE= EVAL_FILE= MODEL_DIR= OUTPUT_DIR= OFMT= SMOOTH= 

# Parse command line options
SHORTOPTS="hd:e:m:o:f:pcs:s:r:a"
LONGOPTS="dev:,eval:,model:,output-dir:,ofmt:,just-pos-output,cattag,smooth:,raw:,multitag"
GETOPTS=`getopt -o $SHORTOPTS -l $LONGOPTS -n 'tcandc.sh' -- "$@"`
eval set -- "$GETOPTS"

while true ; do
    case $1 in
        -h|--help) cat <<EOF ; exit 0 ;;
Usage: candc.sh options TRAIN_FILE
Options:
  -h,--help           show this help message and exit
  -d,--dev FILE       develop on FILE
  -e,--eval FILE      evaluate on FILE
  -m,--model DIR      save model to DIR
  -o,--output-dir DIR save tagger output to DIR
  -f,--ofmt FORMAT    use output format FORMAT
  -p,--just-pos-input input only has POS tags (no supertags)
  -c,--cattag         tag categories (rather than POS tags, ie, do supertagging)
  -s,--smooth EPS     add EPS to all counts
  -r,--raw            input file for tagging is raw (doesn't have tags)
  -a,--multitag       output multitags on development file
EOF
        -d|--dev)            DEV_FILE=$2       ; shift 2 ;;
        -e|--eval)           EVAL_FILE=$2      ; shift 2 ;;
        -m|--model)          MODEL_DIR=$2      ; shift 2 ;;
        -o|--output-dir)     OUTPUT_DIR=$2     ; shift 2 ;;
        -f|--ofmt)           OFMT=$2           ; shift 2 ;;
        -p|--just-pos-input) JUST_POS=1        ; shift ;; 
        -c|--cattag)         CATTAG=1          ; shift ;;
        -s|--smopth)         SMOOTH=--sigma $2 ; shift 2 ;; 
        -r|--raw)            RAW=1             ; shift ;;
        -a|--multitag)       MULTITAG=1        ; shift ;;
        --)                  shift ; break ;;
        *)                   echo Error processing options 1>&2 ; exit 1 ;;
    esac
done

TRAIN_BIN=train_pos
# IFMT="--ifmt \"%w|%p|%? \\\\n\""
if [ $CATTAG ] ; then
    TRAIN_BIN=train_super
    IFMT="--ifmt \"%w|%p|%s \\\\n\""

elif [ $JUST_POS ] ; then
    IFMT="--ifmt \"%w|%p \\\\n\""
fi

if [ -e $MODEL_DIR ] ; then
    echo "A file with the same name as the desired dir, $MODEL_DIR, already exists" 1>&2
    exit 1
fi

mkdir -p $MODEL_DIR

if [ -z $@ ] ; then 
    echo "Error: Training file required" 1>&2 
    exit 1 
fi
TRAIN_FILE=$1 ; shift 

# Run C&C training        
echo "Training..."
eval $CANDC/bin/$TRAIN_BIN --model $MODEL_DIR --comment \"Foo\" --input $TRAIN_FILE --solver bfgs $SMOOTH $IFMT

# Fix C&C model so that it works with full PTB training
if [ -z $CATTAG ] ; then 
    tfix_candc.sh $MODEL_DIR
fi

if [ $MULTITAG ] ; then
    TEST_BIN=mpos
    OFMT=crazy

else
    TEST_BIN=pos
    OFMT=
fi

if [ $RAW ] ; then
    IFMT=
else 
    IFMT="--ifmt \"%w|%? \\\\n\""
fi

COLUMN=1    

RESULT_FILE=`tempfile`

if [ $EVAL_FILE ] ; then 
    # The original python script printed "Tagging eval file: $RESULT_FILE" -- is that right?
    echo "-----------------------------------------------"
    echo "Tagging eval file: $EVAL_FILE"
    eval $CANDC/bin/$TEST_BIN --model $MODEL_DIR --input $EVAL_FILE $IFMT > $RESULT_FILE
    eval tscore_tags.sh -m $RESULT_FILE -g $EVAL_FILE -t $TRAIN_FILE -f Pipe -c $COLUMN
    

if [ $DEV_FILE ] ; then
    # The original python script printed "Tagging devel file: $RESULT_FILE" -- is that right?
    eval $CANDC/bin/$TEST_BIN --model $MODEL_DIR --maxwords 5000 --input $DEV_FILE $IFMT $OFMT > $RESULT_FILE
fi
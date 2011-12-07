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

# Interface to C&C tagger designed to have the same protocal as Tag.java

if [ -z $CANDC ] 
then 
    echo Please set the CANDC environment variable to where you have the CANDC tools installed. 1>&2
    exit 1
fi

# Clear variables that will be used in program invocations later
DEV_FILE= EVAL_FILE= MODEL_DIR= OUTPUT_DIR= OFMT= SMOOTH= 

CAT_CUTOFF=10

# Parse command line options
SHORTOPTS="a:b:c:d:e:g:hi:jl:m:no:p:r:s:t:ux"
LONGOPTS="mpi:,beta:,td-cutoff:,dev:,eval:,tag-dict:,help,iterations:,dirichlet-emission,dirichlet-transition,labeled:,model:,multitag,out:,prior:,raw:,tagset:,train:,unconstrained,fix"
GETOPTS=`getopt -o $SHORTOPTS -l $LONGOPTS -n 'csupertag.sh' -- "$@"`
eval set -- "$GETOPTS"

echo "got here"

while true ; do
    case $1 in
        -h|--help) cat <<EOF ; exit 0 ;; 
usage: csupertag.sh options
 -a,--mpi <arg>              
 -b,--beta <arg>             
 -c,--td-cutoff <arg>        category cutoff (def 10)
 -d,--dev <arg>              the development file
 -e,--eval <arg>             the evaluation file
 -g,--tag-dict <arg>         
 -h,--help                   print help
 -i,--iterations <arg>       
 -j,--dirichlet-emission     
 -k,--dirichlet-transition   
 -l,--labeled <arg>          the machine labeled file
 -m,--model <arg>
 -n,--multitag               
 -o,--out <arg>              the output directory
 -p,--prior <arg>            use prior with value specified (what this
                             means depends on the model)
 -r,--raw <arg>
 -s,--tagset <arg>
 -t,--train <arg>            the training file
 -x,--fix                    fix candc model
 -u,--unconstrained
EOF
        -a|--mpi)                           shift 2 ;;
        -b|--beta)                          shift 2 ;;
        -c|--td-cutoff)    CAT_CUTOFF=$2  ; shift 2 ;;
        -d|--dev)          DEV_FILE=$2    ; shift 2 ;;
        -e|--eval)         EVAL_FILE=$2   ; shift 2 ;;
        -g|--tag-dict)                      shift 2 ;;
        -i|--iterations)                    shift 2 ;;
        -j|--dirichlet-emission)            shift   ;;
        -k|--dirichlet-transition)          shift   ;;
        -l|--labeled)      TRAIN_FILE2=$2 ; shift 2 ;;
        -m|--model)                         shift 2 ;;
        -n|--multitag)                      shift ;;
        -o|--out)          OUTPUT_DIR=$2  ; shift 2 ;;
        -p|--prior)        SIGMA=$2       ; shift 2 ;;
        -r|--raw)                           shift 2 ;;
        -s|--tagset)                        shift 2 ;;
        -t|--train)        TRAIN_FILE1=$2 ; shift 2 ;;
        -x|--fix)          FIX_CANDC=1 ;    shift   ;;
        -u|--unconstrained)                 shift 2 ;;
        --)                  shift ; break ;;
        *)                   echo Error processing option $1 1>&2 ; exit 1 ;;
    esac
done

if [ $SIGMA ] ; then
    SMOOTH="--sigma $SIGMA"
fi

TRAIN_BIN=train_super
IFMT="--ifmt \"%w|%p|%s \\\\n\""

# Make train file from combined (gold standard) training and machine labelled
TRAIN_FILE=`tempfile`
cat $TRAIN_FILE1 $TRAIN_FILE2 > $TRAIN_FILE


# Make the model directory on the fly
MODEL_DIR=`tempfile`
rm $MODEL_DIR
mkdir -p $MODEL_DIR

# Make the output directory if not called for by the user
if [ -z $OUTPUT_DIR ] ; then
    OUTPUT_DIR=`tempfile`
    rm $OUTPUT_DIR

elif [ -e $OUTPUT_DIR ] ; then
    echo $OUTPUT_DIR already exists, please choose a different directory 1>&2
    exit 1
fi

mkdir -p $OUTPUT_DIR


# Run C&C training        
echo "Training..."
CMD="$CANDC/bin/$TRAIN_BIN --super-category_cutoff $CAT_CUTOFF --model $MODEL_DIR --comment \"Foo\" --input $TRAIN_FILE --solver bfgs $SMOOTH $IFMT"
echo $CMD && eval $CMD

# Fix C&C model so that it works with full PTB training
if [ $FIX_CANDC ] ; then 
    echo "Fixing $MODEL_DIR"
    tfix_candc.sh $MODEL_DIR
fi

if [ $MULTITAG ] ; then
    TEST_BIN=msuper
    OFMT=crazy

else
    TEST_BIN=super
    OFMT=
fi

if [ $RAW ] ; then
    IFMT=
else 
    IFMT="--ifmt \"%w|%p|%? \\\\n\""
fi

COLUMN=2

    
if [ $EVAL_FILE ] ; then 
    RESULT_FILE=$OUTPUT_DIR/eval.tagged.txt

    # The original python script printed "Tagging eval file: $RESULT_FILE" -- is that right?
    echo "-----------------------------------------------"
    echo "Tagging eval file: $EVAL_FILE"
    CMD="$CANDC/bin/$TEST_BIN --super-category_cutoff $CAT_CUTOFF --model $MODEL_DIR --input $EVAL_FILE $IFMT > $RESULT_FILE"
    echo $CMD && eval $CMD
    tscore_tags.sh -m $RESULT_FILE -g $EVAL_FILE -t $TRAIN_FILE -f Pipe -c $COLUMN
fi
    

if [ $DEV_FILE ] ; then
    RESULT_FILE=$OUTPUT_DIR/dev.tagged.txt

    ORIG_IFMT=$IFMT
    IFMT="--ifmt \"%w|%p \\\\n\""
#     if head -n 1 $DEV_FILE | grep "|" > /dev/null ; then
#         echo ok
#     else
#         ORIG_IFMT=$IFMT
#         IFMT="--ifmt \"%w \\\\n\""
#     fi

    # The original python script printed "Tagging devel file: $RESULT_FILE" -- is that right?
    CMD="$CANDC/bin/$TEST_BIN --super-category_cutoff $CAT_CUTOFF --model $MODEL_DIR --maxwords 5000 --input $DEV_FILE $IFMT $OFMT > $RESULT_FILE"
    echo $CMD && eval $CMD

    if [ "$ORIG_IFMT" ] ; then
        IFMT=$ORIG_IFNT
    fi
fi

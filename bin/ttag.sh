#!/bin/sh
if echo $@ | grep "\-m CANDC_SUPER" > "/dev/null" || \
   echo $@ | grep "\--model CANDC_SUPER" > "/dev/null" 
then
    csupertag.sh $@

elif echo $@ | grep "\-m CANDC" > "/dev/null" || \
     echo $@ | grep "\--model CANDC" > "/dev/null"
then 
    ctag.sh $@

else
    . texnlp-env
    $JAVA_CMD texnlp.apps.Tag $@
fi

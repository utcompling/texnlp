#!/bin/sh
mv unknowns BACKUP_unknowns
#mv number_unknowns BACKUP_number_unknowns
head -n 3 tagdict > header.tmp 
tail +4 tagdict > tags.tmp  
cat tags.tmp | awk '{print $2}' | sort | uniq > list.tmp
cat header.tmp list.tmp > unknowns 
#echo NUM > numlist.tmp 
#cat header.tmp numlist.tmp > number_unknowns 
rm *tmp

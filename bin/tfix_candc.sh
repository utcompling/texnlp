#!/bin/sh
cd $1
mv unknowns BACKUP_unknowns
head -n 3 tagdict > header.tmp 
tail -n +4 tagdict | cut -d " " -f 2 | sort | uniq > list.tmp
cat header.tmp list.tmp > unknowns 
#mv number_unknowns BACKUP_number_unknowns
#echo NUM > numlist.tmp 
#cat header.tmp numlist.tmp > number_unknowns 
rm *tmp 
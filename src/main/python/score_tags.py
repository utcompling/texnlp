###############################################################################
## Copyright (C) 2007 Jason Baldridge, The University of Texas at Austin
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
import sys
import gzip
import fnmatch

gold_tag_filename = sys.argv[1]
model_tag_filename = sys.argv[2]

if fnmatch.fnmatch(gold_tag_filename,"*.gz"):
    gold_tag_file = gzip.open(gold_tag_filename)
else:
    gold_tag_file = file(gold_tag_filename,"r")

if fnmatch.fnmatch(model_tag_filename,"*.gz"):
    model_tag_file = gzip.open(model_tag_filename)
else:
    model_tag_file = file(model_tag_filename,"r")

gold_lines = gold_tag_file.readlines()
model_lines = model_tag_file.readlines()

word_correct = 0
word_total = 0
sentence_correct = 0
sentence_total = 0

errors = {}
all_correct = True

for linenumber in range(len(gold_lines)):

    gold_line = gold_lines[linenumber].strip()
    model_line = model_lines[linenumber].strip()

    if (model_line == ""):

        if (gold_line != ""):
            print "Something wrong -- different length on sentence for gold and model."
            print "Gold:",gold_line
            print "Model:",model_line

        if all_correct:
            sentence_correct += 1
        sentence_total +=1
        all_correct = True

    else:
        gitems = gold_line.split()
        mitems = model_line.split()

        gtag = gitems[1]
        mtag = mitems[1]

        if gtag == mtag:
            word_correct += 1
        else:
            all_correct = False
            errors[(gtag,mtag)] = errors.get((gtag,mtag),0)+1

        word_total += 1


    gold_line = gold_tag_file.readline().strip()
    model_line = model_tag_file.readline().strip()

word_accuracy = (word_correct/float(word_total))*100.0
sentence_accuracy = (sentence_correct/float(sentence_total))*100.0
print "Word accuracy: %2.3f (%d/%d)" % (word_accuracy, word_correct, word_total)
print "Sent accuracy: %2.3f (%d/%d)" % (sentence_accuracy, sentence_correct, sentence_total)

to_sort = []
for (gtag,mtag) in errors:
    to_sort.append((errors[(gtag,mtag)],gtag,mtag))

to_sort.sort(lambda x,y:cmp(y[0],x[0]));

print "\nMost common errors:"
print "Err\tGold\tModel\n---------------------"
for i in range (0,min(5,len(to_sort))):
    print "\t".join([str(x) for x in to_sort[i]])

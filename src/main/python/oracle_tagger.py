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

train_filename = sys.argv[1]
dict_filename = sys.argv[2]
test_filename = sys.argv[3]

if fnmatch.fnmatch(train_filename,"*.gz"):
    train_file = gzip.open(train_filename)
else:
    train_file = file(train_filename,"r")

if fnmatch.fnmatch(dict_filename,"*.gz"):
    dict_file = gzip.open(dict_filename)
else:
    dict_file = file(dict_filename,"r")

if fnmatch.fnmatch(test_filename,"*.gz"):
    test_file = gzip.open(test_filename)
else:
    test_file = file(test_filename,"r")

tag_set = set({})
tag_dict = {}
for line in dict_file:
    line = line.strip()
    if len(line) > 0:
	word, tag = line.split()
	if tag_dict.has_key(word):
	    tag_dict[word].add(tag)
	else:
	    tag_dict[word] = set([tag])
        tag_set.add(tag)
        #tag_counts[tag] = tag_counts.get(tag, 0) + 1

print len(tag_set)
total = 0
max = 0
for word in tag_dict:
    total += len(tag_dict[word])
    if len(tag_dict[word]) > max:
        max = len(tag_dict[word])

print max
print float(total)/len(tag_dict)

#num_tags = len(tag_set)
#
#tag_counts = {}
#for line in train_file:
#    line = line.strip()
#    if len(line) > 0:
#	word, tag = line.split()
#        if word in tag_dict:
#            for tag in tag_dict[word]:
#                tag_counts[tag] = tag_counts.get(tag, 0.0) + 1.0/len(tag_dict[word])
#        #else:
#        #    for tag in tag_set:
#        #        tag_counts[tag] = tag_counts.get(tag, 0.0) + 1.0/num_tags
#            
#
#most_freq_in_tag_dict = {}
#for word in tag_dict:
#    most_freq_tag = ""
#    highest = 0
#    for tag in tag_dict[word]:
#        if tag_counts[tag] > highest:
#            most_freq_tag = tag
#            highest = tag_counts[tag]
#    most_freq_in_tag_dict[word] = most_freq_tag
#
#most_freq_tag = ""
#highest = 0
#for tag in tag_counts:
#    if tag_counts[tag] > highest:
#	most_freq_tag = tag
#	highest = tag_counts[tag]
#
#for line in test_file:
#    line = line.strip()
#    if len(line) > 0:
#	word, tag = line.split()
#	if word in tag_dict:
#	    print word + "\t" + most_freq_in_tag_dict[word]
#	    #print word + "\t" + tag
#	    #if tag in tag_dict[word]:
#	    #    print word + "\t" + tag
#	    #else:
#            #	print word + "\tKWUT:"+tag
#	else:
#	    print word + "\t" + most_freq_tag
#	    #print word + "\t" + tag
#	    #print word + "\tUWUT:"+tag
#	    
#    else:
#	print

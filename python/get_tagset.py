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

tag_filename = sys.argv[1]
cutoff = int(sys.argv[2])

if fnmatch.fnmatch(tag_filename,"*.gz"):
    tag_file = gzip.open(tag_filename)
else:
    tag_file = file(tag_filename,"r")


tag_counts = {}

for line in tag_file.readlines():

    items = line.strip().split()

    if len(items) > 0:
        tag_counts[items[1]] = tag_counts.get(items[1], 0) + 1

for tag in tag_counts:
    if tag_counts[tag] > cutoff:
        print tag
    

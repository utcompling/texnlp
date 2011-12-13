#!/usr/bin/python

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

import os
import sys
import optparse
import tempfile


## Check that CANDC environment variable is set and get it
candc = ''
if os.environ.has_key('CANDC'):
    candc = os.environ['CANDC']
else:
    print "Please set the CANDC environment variable to where you have the CANDC tools installed."
    exit(1)


## Get options

opt_parser = optparse.OptionParser()
opt_parser.add_option("-d", "--dev", action="store", default='',
		  help="develop on FILE",
		  metavar="DIR")
opt_parser.add_option("-e", "--eval", action="store", default='',
		  help="evaluate on FILE",
		  metavar="DIR")
opt_parser.add_option("-m", "--model", action="store", default='models',
		  help="save model to DIR",
		  metavar="DIR")
opt_parser.add_option("-o", "--output-dir", action="store", default='output',
		  help="save tagger output to DIR",
		  metavar="DIR")
opt_parser.add_option("-f", "--ofmt", action="store", default='',
		  help="use output format FORMAT",
		  metavar="FORMAT")
opt_parser.add_option("-p", "--just-pos-input", action="store_true", default=False,
                      help="Tag categories (rather than POS tags, ie, do supertagging)")
opt_parser.add_option("-c", "--cattag", action="store_true", default=False,
                      help="Tag categories (rather than POS tags, ie, do supertagging)")
opt_parser.add_option("-v", "--verbose", action="store_true", default=False,
                      help="be verbose")
opt_parser.add_option("-s", "--smooth", type="float", default=0.0,
                      help="add EPS to all counts",
                      metavar="EPS")
opt_parser.add_option("-r", "--raw", action="store_true", default=False,
                      help="input file for tagging is raw (doesn't have tags)")
opt_parser.add_option("-a", "--multitag", action="store_true", default=False,
                      help="output multitags on development file")
    

(options, args) = opt_parser.parse_args()

verbose = options.verbose

model_dir = options.model
if os.path.isfile(model_dir):
    raise OSError("A file with the same name as the desired dir, " \
		  "'%s', already exists." % model_dir)
elif not(os.path.isdir(model_dir)):
    os.makedirs(model_dir)

evalfile = options.eval;
devfile = options.dev;

smooth = ""
if options.smooth > 0.0:
    smooth = "--sigma %f" % options.smooth


#output_dir = options.output_dir
#if os.path.isfile(output_dir):
#    raise OSError("A file with the same name as the desired dir, " \
#		  "'%s', already exists." % output_dir)
#elif not(os.path.isdir(output_dir)):
#    os.makedirs(output_dir)


## Process files

trainfile = args[0]

# Train the tagger
executable = "train_pos"
inputformat = '--ifmt "%w|%p|%? \\n"'
column = 1
if options.cattag:
    executable = "train_super"
    inputformat = '--ifmt "%w|%p|%s \\n"'
    column = 2

if options.just_pos_input:
    inputformat = '--ifmt "%w|%p \\n"'
    
print("%s/bin/%s --model %s --comment \"Foo\" --input %s --solver bfgs %s %s"
          % (candc, executable, model_dir, trainfile, smooth, inputformat))
os.system("%s/bin/%s --model %s --comment \"Foo\" --input %s --solver bfgs %s %s"
          % (candc, executable, model_dir, trainfile, smooth, inputformat))

# Fix CandC model so that it works without full PTB training
if not(options.cattag):
    os.system("afix_candc.sh %s" % model_dir);

executable = "pos"
inputformat = '--ifmt "%w|%?|%? \\n"'
outputformat = '--ofmt "%w\\t%p\\n\\n\\n"'
if options.raw:
    inputformat = ''
if options.cattag:
    executable = "super"
    inputformat = '--ifmt "%w|%p \\n"'
    #inputformat = '--ifmt "%w|%p|%? \\n"'
    outputformat = '--ofmt "%w\\t%s\\n\\n\\n"'

if options.ofmt == "candc":
    inputformat = '--ifmt "%w|%p|%s \\n"'
    outputformat = '--ofmt "%w|%p|%s \\n"'

if (options.multitag):
    executable = "mpos"
    outputformat = ''

if (evalfile != ""):
    _,resultfile = tempfile.mkstemp()

    print "-----------------------------------------------"
    print "Tagging eval file: ", resultfile

    print ("%s/bin/%s --model %s --input %s %s > %s"
           % (candc, executable, model_dir, evalfile, inputformat, resultfile))

    os.system("%s/bin/%s --model %s --input %s %s > %s"
              % (candc, executable, model_dir, evalfile, inputformat, resultfile))

    os.system("ascore_tags.sh -m %s -g %s -t %s -f Pipe -c %s"
              % (resultfile, evalfile, trainfile, column))

if (devfile != ""):
    _,resultfile = tempfile.mkstemp()

    print "-----------------------------------------------"
    print "Tagging devel file: ", resultfile

    cmd = "%s/bin/%s --model %s --maxwords 5000 --input %s %s %s > %s" \
          % (candc, executable, model_dir, devfile, inputformat, outputformat, resultfile)

    print cmd
    os.system(cmd)





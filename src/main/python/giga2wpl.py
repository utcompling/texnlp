import sys
import os
import re
import gzip
import fnmatch

whitespaceRE = re.compile("\s")
commaRE = re.compile(",")
nonAlphaRE = re.compile("[^A-Za-z]")
nonAlphaNumCaptureRE = re.compile("([^A-Za-z0-9])")

splitwordRE = re.compile("^(\w+\.?)(n't|'s|'re|'d|'ve|'ll)$")

frontcharsRE = re.compile("^(\$|'|\"|``|\()(.+)$")
endcharsRE = re.compile("^(.+)(''|\"|\)|,|\.|\?|!|%|;|:)$")

abbrevRE = re.compile("(Mr|Mrs|Ms|Dr|Sr|Jr|Assoc|Co|No|St|Sen|vs|Inc|Corp|Ltd|Rev|Lt|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec|Mon|Tue|Tues|Wed|Thu|Thur|Fri|Sat|Sun)(\.)$")
acronymRE = re.compile("^(?:([A-Za-z]\.))+$")

eosRE = re.compile(r"[\.?!]")  

def tokenize_word(word, front, back):
    #print "-- ", word, front, back
    if len(word) == 0:
        return

    if word == "...":
        front.append(word)
        return

    matches = acronymRE.match(word)
    if matches:
        front.append(word)
        return
    
    matches = abbrevRE.match(word)
    if matches:
        front.append(word)
        return

    matches = frontcharsRE.match(word)
    if matches:
        front.append(matches.group(1))
        return tokenize_word(matches.group(2),front,back)

    matches = endcharsRE.match(word)
    if matches:
        back.append(matches.group(2))
        return tokenize_word(matches.group(1),front,back)

    if word[-1] == "'":
        back.append("'")
        return tokenize_word(word[:-1],front,back)

    if word[-1] == "`":
        back.append("`")
        return tokenize_word(word[:-1],front,back)

    matches = splitwordRE.match(word)
    if matches:
        front.append(matches.group(1))
        back.append(matches.group(2))
        return

    
    front.append(word)
    return

gigaword_dir = "/groups/corpora/english-gigaword-LDC2003T05/cdrom0"

directories = []
#for name in os.listdir(gigaword_dir):
for name in ["nyt"]:
    full_path_name = gigaword_dir+"/"+name
    if os.path.isdir(full_path_name):
        directories.append(full_path_name)

should_print = False
for directory_name in directories:
    files = os.listdir(directory_name)
    for file in files:
        if fnmatch.fnmatch(file,"*.gz"):
            file_reader = gzip.open(directory_name+"/"+file)
            line = file_reader.readline()
            last = ""
            while line:
                line = line.strip()
                #print "**",line
                if line == "<TEXT>":
                    should_print = True
                elif line == "</TEXT>":
                    should_print = False
                else:
                    if should_print:
                        if line == "<P>":
                            last = ""
                        elif line == "</P>":
                            print 
                        else:
                            for word in whitespaceRE.split(line.strip()):
                                word = word.strip()
                                if word != "":
                                    if last == "." or last == "?" or last == "!":
                                        print
                                        
                                    front_toks = []
                                    back_toks = []
                                    tokenize_word(word, front_toks, back_toks)

                                    for i in front_toks:
                                        last = i
                                        print i
                                    
                                    back_toks.reverse()
                                    for i in back_toks:
                                        last = i
                                        print i

                line = file_reader.readline()



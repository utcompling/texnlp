import sys
import os
import re
import gzip
import fnmatch
import sexpr_parse

whitespaceRE = re.compile("\s")
nonAlphaRE = re.compile("[^A-Za-z]")
lexRE = re.compile("<L\s(\S+)\s(\S+)\s\S+\s(\S+)\s\S+>")

ccgbank_dir = "/groups/corpora/ccgbank-LDC2005T13/data/AUTO"

just0_directories = [ "00" ]
just0_to_5_directories = [ "00", "01", "02", "03", "04", "05" ]
just19_directories = [ "19" ]
train_directories = ["0"+str(x) for x in range(10)] + ["1"+str(x) for x in range(9)]
dev_directories = [ "19", "20", "21"]
test_directories = [ "22", "23", "24"]

#source_directories = just0_directories
#source_directories = train_directories
#source_directories = dev_directories
source_directories = test_directories

directories = []

#for name in os.listdir(ccgbank_dir):
for name in source_directories:
    full_path_name = ccgbank_dir+"/"+name
    if os.path.isdir(full_path_name):
        directories.append(full_path_name)

for directory_name in directories:
    files = os.listdir(directory_name)
    for file in files:
        if fnmatch.fnmatch(file,"*.auto"):
            file_reader = open(directory_name+"/"+file)
            info = file_reader.readline().strip()

            while info:

                tree = file_reader.readline().strip()

                for match in lexRE.finditer(tree):
                    # For TexNLP, print supertags
                    #print match.group(3) + "\t" + match.group(1)
                    # For TexNLP, print supertags
                    print match.group(3) + "\t" + match.group(2)

                    # For C&C
                    #print match.group(3) + "|" + match.group(2) + "|" + match.group(1),

                print
                
                
                info = file_reader.readline().strip()

                

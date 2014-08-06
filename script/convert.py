"""
  Copyright 2014 Matthias Frey
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
   
      http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 
"""

""" Convert ICDAR/structure.xml format to CSV
"""


import re
import sys
import csv
import os 
import xml
from xml.dom.minidom import parse, parseString


def getText(node):
    """ Get textual content of an xml node.
    """
    text = []
    for n in node.childNodes:
        if n.nodeType == n.TEXT_NODE: text.append(n.data)
    return ''.join(text)

def utf_8_encoder(unicode_csv_data):
    for line in unicode_csv_data:
        yield line.encode('utf-8')

def convertFile(src, dest):
    with open(src) as f, open(dest, "w+") as outfile:
        dom = parse(f)
        for table in dom.getElementsByTagName('table'):
            grid={}
            for c in table.getElementsByTagName('cell'):
                x = int(c.getAttribute('start-col'))
                y = int(c.getAttribute('start-row'))
                
                contents = ""
                for content in c.getElementsByTagName('content'):
                    contents += getText(content)
                    
                grid.setdefault(y, {})[x] = contents    
               

            """ Align in grid according to start-row and start-col.
            """               
            if len(grid) == 0 or len(grid[0]) == 0: continue
            max_rows = len(grid)
            max_cols = max([len(r) for r in grid.values()])
            normgrid = []
            for j in range(0, max_rows):
                normrow=[grid[j][i] \
                         if j in grid and i in grid[j] \
                         else '' 
                         for i in range(0, max_cols)]
                normgrid.append(normrow)
                
            wr = csv.writer(outfile)
            
            for row in normgrid:
                wr.writerow([v.encode('utf-8') for v in row])
    
            wr.writerow([])
            wr.writerow([])
            wr.writerow([])
            
            
try:
    path = sys.argv[1]
except IndexError:
    print "Specify a file or directory to convert"
    quit()

if os.path.isdir(path):
    print "Converting files in ", path, "ending in '-str.xml'"
    for fname in [f for f in os.listdir(path) if f[-8:]=='-str.xml']:
        try:
            src = os.path.join(path, fname)
            basepart = re.match('.*([eus]{2}-0[0-9]{2}).*', os.path.basename(src)).groups()[0]
            dest = os.path.join(os.path.dirname(src), basepart + ".csv")
            convertFile(src, dest)
            print "Converted", src, dest
        except xml.parsers.expat.ExpatError as e:
            print ("Expat error for {}".format(src))
            print e
            
elif os.path.isfile(path):
    src = path
    basepart = re.match('.*([eus]{2}-0[0-9]{2}).*', os.path.basename(src)).groups()[0]
    dest = os.path.join(os.path.dirname(src), basepart + ".csv")
    convertFile(src, dest)
    print "Converted", src, dest
else:
    print path, " does not exist"

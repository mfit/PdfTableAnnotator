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

"""    
    
 
    CSV-compare
    -----------
    
    Compare table data stored in CSV (comma seperated values) format.
    
"""


import re
import csv
import sys
import os


def _pr_list(l1, l2, replace_chars = '[\n ]'):
    """ Calculate precision and recall regarding elements of a list.
    
        When a 1:1 match cannot be achieved, the list pointers will be 
        moved forward until a match occurs (first of list A, then of list B). 
        The closest match will count, and matching will continue from those
        list positions onwards.
        
        The replace_chars parameter is used to remove characters from the 
        strings before comparing. The default will remove newlines and spaces.
    """
    def _fnext(l, item):
        item = re.sub(replace_chars, '', item).strip()
        
        for i, txt in enumerate(l):
            txt = re.sub(replace_chars, '', txt).strip()
            if txt == item:
                return i
        return -1
    
    if len(l2)==0 or len(l1)==0:
        return 0, 0
    
    i = 0
    j = 0
    match = 0
    while len(l1)>i and len(l2)>j:
        t1 = re.sub(replace_chars, '', l1[i]).strip()
        t2 = re.sub(replace_chars, '', l2[j]).strip()
        if t1 == t2:
            match += 1
            i += 1
            j += 1
        else:
            ii = _fnext(l1[i:], l2[j])
            jj = _fnext(l2[j:], l1[i])
            if ii>=0 and (ii<jj or jj<0): i+=ii
            elif jj>=0: j+=jj
            else:
                i+=1
                j+=1
            
    return float(match)/len(l2), float(match)/len(l1) 


def clean_table(tab):
    """ Remove trailing empty cells resulting from the way some
        spreadsheet application output csv for multi table documents.
    """
    if len(tab) == 0: 
        return []
    n_empty=[]
    for row in tab:
        for n, val in enumerate(reversed(row)):
            if val!='':
                break 
        n_empty.append(n)
    strip_cols = min(n_empty)
    cleaned = []
    for row in tab:
        cleaned.append(row[0:len(row)-strip_cols])
    return cleaned


def compare_tables(tab1, tab2):
    """ Compare two tables (2dim lists).
    """
    
    info = {'rows_a':len(tab1),
            'rows_b':len(tab2),
            'rows_match': 1 if len(tab1) == len(tab2) else 0,
            }
    
    sizesA = [len(l) for l in tab1]
    sizesB = [len(l) for l in tab2]
    
    info['dim_match'] = 1 if sizesA == sizesB else 0
    info['size_a'] = sum(sizesA)
    info['size_b'] = sum(sizesA)
    
    if len(sizesA)>0 and len(sizesB)>0:
        info['cols_match'] = 1 if min(sizesA) == max(sizesA) and \
            min(sizesB) == max(sizesB) and min(sizesA) == min(sizesB) else 0
        
    # 'flatten' tables
    cellsA = []
    cellsB = []
    for r in tab1: cellsA += [c for c in r]
    for r in tab2: cellsB += [c for c in r]

    info['p'], info['r'] = _pr_list(cellsA, cellsB)
    info['F1'] = F1(info['p'], info['r'])
    
    return info
        

def compare_files_pr(file1, file2):
    """ Calculate simple P/R .
        Compare lists of cells, left to right , top to bottom.
    """
    cells = [[], []]
    for i, fname in enumerate([file1, file2]):
        with file(fname) as csvfile:
            rd = csv.reader(csvfile, delimiter=',', quotechar='"')
            for r in rd:
                cells[i] += [c for c in r]

    return _pr_list(*cells)


def compare_files(file1, file2):
    """ Compare two csv files.
    """
    
    groundtruth = read_tables_from_file(file1)
    try:
        compare = read_tables_from_file(file2)
    except:
        compare = []
    
    tbs = [groundtruth, compare]
    
    finfo = {'tabcount_a': len(tbs[0]),
             'tabcount_b': len(tbs[1]),
             'tabcount_match': len(tbs[0]) == len(tbs[1]),
             }
    
    finfo['tables']=[]
    for n in range(0, len(tbs[0])):
        if finfo['tabcount_match']:
            comp_info = compare_tables(tbs[0][n], tbs[1][n])
        else:
            if n < len(tbs[1]):
                comp_info = compare_tables(tbs[0][n], tbs[1][n])
            else:
                comp_info = compare_tables(tbs[0][n], [[]])
        comp_info['n']=n
        finfo['tables'].append(comp_info)
    
    return finfo  


def output_compareinfo_csv(file, info, fields=['p', 'r', 'F1']):
    """ Pre-format a row that holds measures about similarity of a table
        to the ground truth.
    """
    lines = []
    tabmatch = 1 if info['tabcount_match'] else 0
    for tinfo in info['tables']:
        lines.append([file, str(tabmatch)] + [str(tinfo[k]) for k in fields])
    return lines


def F1(p, r):
    """ Calculate F1 score from precision and recall.
        Returns zero if one of p, r is zero.
    """
    return (2*p*r/(p+r)) if p != 0 and r != 0 else 0


def read_tables_from_file(csvfile):
    """ Opens csvfile, returns all tables found.
        Guesses csv format (delimiter, etc.)
        Splits data into different tables at newline (or empty row).
        Returns list of tables.
    """ 
    tables=[]
    table_id = 0
    with file(csvfile) as f:
        sniffer = csv.Sniffer()
        dialect = sniffer.sniff(f.next())
        rd = csv.reader(f, delimiter=dialect.delimiter, 
                        quotechar=dialect.quotechar)
        for r in rd:
            if len(tables) <= table_id:
                tables.append([])
            
            # Begin next table if there is an empty line
            if r == [] or sum([len(v) for v in r]) == 0:
                if len(tables[table_id])>0:
                    table_id+=1
            else:
                tables[table_id].append(r)
      
    return [clean_table(t) for t in tables if t!=[]]



if __name__ == '__main__':
    """ Script usage.
    """
    
    fields = [
              #'rows_a', 'rows_b',
              #'size_a', 'size_b',
              'n',
              'rows_match', 'cols_match', 'dim_match',
              'p', 'r', 'F1',]
    limitchar = ' & '
    
    if len(sys.argv) < 3:
        print "Specify two (csv-)files or directories"
        quit(-1)
    
    # Params 1 + 2 are files or directories
    file1 = sys.argv[1]
    file2 = sys.argv[2]
    srcinfo = [os.path.basename(file1), os.path.basename(file2)]
    
    # 3rd parameter becomes 'tooldef' (text cols to name rows),  
    # and 4th parameter tells whether to print headers    
    tooldef = sys.argv[3].split('-') if len(sys.argv) > 3 else ['na', 'na']
    print_headers = len(sys.argv) > 4 and sys.argv[4] in ["1", "y", "yes"]
        
    if print_headers:
        print ','.join(['name', 'tool', 'src1', 'src2', 
                        'filename', 'tabsmatch',] + fields)
    
    if os.path.isfile(file1) and os.path.isfile(file2):
        inf = compare_files(file1, file2)
        lines = output_compareinfo_csv(file1, inf, fields)
        for l in lines:
            print ','.join(tooldef + srcinfo + l)
            
        
    elif os.path.isdir(file1) and os.path.isdir(file2):
        for f in [path for path in os.listdir(file1) if path[-4:]=='.csv']:
            if os.path.isfile(file2 + '/' + f):
                inf = compare_files(file1 + '/' + f, file2 + '/' + f)
                lines = output_compareinfo_csv(f, inf, fields)
                for l in lines:
                    print ','.join(tooldef + srcinfo + l)
            else:
                print ','.join(['','',] + srcinfo + ['', "Missing {} for {} {}".format(f, *tooldef)])
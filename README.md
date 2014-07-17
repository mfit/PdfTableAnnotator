PdfTableAnnotator
=================

A tool to create table ground truth in PDF documents

Installation 
-------------
    Install using maven.
    mvn install
    

Start Web-Application
---------------------

1. start server with
    $ mvn jetty:run

2a. go to 
    http://localhost:8080
     
    This shows the contents of the default repository, which is found in 
    ./resources/repos/test. See "command line usage" for how to add files to
    the repositories. 

2b. OR: To open any PDF on the harddisk, point the browser to 
    http://localhost:8080/annotate?file=path/to/pdf , the path is relative
    to a basedir setting ("/" by default; can be changed in the
    file config.ini).


Command line usage
---------------------
   
Import documents:
    mvn exec:java -Dexec.args="import /path/to/pdfs/ repository_name"
    e.g.:
        mvn exec:java -Dexec.args="import /home/user/mypdfs testdocs"
        
        
        

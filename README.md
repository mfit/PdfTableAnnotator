PdfTableAnnotator
=================

A tool to create table ground truth in PDF documents.

Additional usage info see [usage.pdf](usage.pdf) .

Installation
-------------
Install using maven.

    mvn install


Start Web-Application
---------------------

1. start server with

    mvn jetty:run

2a. go to

    http://localhost:8080

Please note that PDF documents need to be imported beforehand (see 'Command line usage'),
otherwise no docuemnts will show up in the select box.

This shows the contents of the default repository, which is found in
./resources/repos/intro. See "command line usage" for how to add files to
the repositories.

2b. OR: To open any PDF on the harddisk, point the browser to

    http://localhost:8080/annotate?file=path/to/pdf

The path is relative to the basedir setting ("/" by default; can be changed in the
    file config.ini).


Command line usage
---------------------

Import documents:

    mvn exec:java -Dexec.args="import /path/to/pdfs/ repository_name"

E.g:

    mvn exec:java -Dexec.args="import /home/user/mypdfs intro"




The XSLT and XQuery files come from the Data Access Working Group examples.

See http://www.w3.org/TR/rdf-sparql-XMLres/

-----------------------

Run with:

XSLT saxon:
[[java -cp "$CP" net.sf.saxon.Transform   "$@"
[[# Either in.xml xsl.xsl
[[# Or     -a in.xml (use PI) 

xslt output.xml result2-to-html.xsl  > r2.html


XQuery saxon:
[[java -cp "$JAR" net.sf.saxon.Query  "$@"result-to-html.xq
result-to-html.xsl


[[ -s does not seem to work on all versions
xquery -s output.xml  result2-to-html.xq > r2-xq.html


-----------------------

Version1 are the files from the 
http://www.w3.org/TR/2004/WD-rdf-sparql-XMLres-20041221/
working draft
 
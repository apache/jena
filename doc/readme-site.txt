To Do
-----

Local navigation in #leftnav

Need a new section: <div id="localnav">...</div>
which may be null.

Copy of sourceforge logo??


The Jena Web Site
=================

This file describes the process for making the Jena website.

You can directly edit the .html files - the content can be extracted later.

There are some scripts to help - you still need to check the output; they
do not check the validity of the HTML input or output.

Process
-------

There is a template HTML file, "template.html", which contains the page
structure.  Within this there are certain known sections that are replaced
by content.  The "merge" program takes the template and a file of content
and produces a HTML file.

The content is in the form of sections (<div id-"...">...</div>) for well
know section names.  The content file can just have these fragments of HTML
or can be a whole page, so you can run the process on final web site files,
if these have been editted directly.

The sections are:
"header"    The area at the top.
"trail"     The navigation at the top of the content section.
"content"   The main text.
"footer"    The area at the bottom.

In addition, the input file can have a <title></title> section and <meta/>
metadata fields.  These are inserted into the output file.

You don't have to supply all the sections.  They default to what's in the
template file.

After making the HTML files, check them, and export to the Jena web site.


Helper Scripts
--------------

merge   - Takes a content file and makes an HTML web page.

produce - Runs merge on each file in the website
          Knows the pages that make up the site.
          Knows that content files are *.content

release - Ship the website off to jena.sourceforge.net (if you
          have ssh access)

linkcheck - check relative links

(PS "find . -path ./javadoc -prune -o -name \*html -print | xargs linkcheck" 
finds and checks all the HTML files not in javadoc.)



Notes
-----

Colours:
The light blue colour is: rgb(202,223,244) / #CADFF4
"Jena red" is rgb(216, 32 0) / #D82000


----------------
$Id: readme-site.txt,v 1.3 2003-08-26 16:27:48 andy_seaborne Exp $

RDQL
====

    Andy Seaborne
    andy.seaborne@hp.com

Current home:

    http://www.hpl.hp.com/semweb/

RDQL is a query engine for Jena.  It provides an SQL-like query language
for RDF data.

Command Line
============

Try:
    java -cp ... jena.rdfquery

Type of Model Source
====================

If not specified some other way (e.g. the command line), the FROM clause in
a query will identify an RDF model.  Additional, if the model URI ends
".nt" it is assumed to be an N-Triple file; if it is .rdf, it assumed to be
XML; if it is .bdb, it is assumed to be a file that is a BerkeleyDB
database in the same directory as the BerkeleyDB environment files.

SQL Persistent Storage
======================

Jena includes a flexible SQL store that can be used for a variety of
different storage layouts.  A number of database systesm can be used
including Postgres, MySQL and Oracle.  To use this, you will need JDBC set
up on your machine for the appropriate database system.

RDQL can be used to query such databases.  The source is named via the "jdbc:" 
URI.


Test
====

# Internal test
java -cp ...  jena.rdfquery --test
# Query validation
java -cp ...  jena.rdfquery --test <test description file>

The normal test file is testing/RDQL/_control_ The test run should be in
the main directory (the single top level directory of the zip file).

Support & Feedback
==================

I would like to hear any comments you have about RDQL.

RDQL, like the rest of Jena, comes with no commercial support.  I will do
what I can to answer any questions and fix any bugs.  We use the mailing
list jena-dev@yahoogroups.com (http://yahoogroups.com/group/jena-dev)
for Jena support.

# SQL driver file for Postgresql for the plain hash ID version.
# Notionally the plain hash ID should be generic enough to just use
# but the Postgres syntax for column aliases requires "AS" whereas most others
# don't and many actively disallow it. So we need to re do all the list operations.
#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
create index rdf_literals_idx on rdf_literals(literal);

#-------------------------------------------------------------------
# Drop all RDF procedures from a database
cleanDBprocedures
drop index rdf_literals_idx;

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.MODEL=?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.PREDICATE = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.MODEL = ?


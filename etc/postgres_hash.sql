# SQL driver file for Postgresql for the plain hash ID version.
# Changed "BLOB" to "OID".
# Convert selects to "narrow" selects.
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE RDF_LAYOUT_INFO (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   VAL VARCHAR(250)
);;
CREATE TABLE RDF_RESOURCES (
   ID CHAR(24) NOT NULL PRIMARY KEY,
   NAMESPACE CHAR(24) NOT NULL,
   LOCALNAME VARCHAR(250) NOT NULL
);;
CREATE INDEX RDF_IDX_RESOURCES_NAME ON RDF_RESOURCES(NAMESPACE, LOCALNAME);;
CREATE TABLE RDF_NAMESPACES (
    ID CHAR(24) NOT NULL PRIMARY KEY,
    URI varchar(250) NOT NULL
);;
CREATE INDEX RDF_IDX_NAMESPACES ON RDF_NAMESPACES(URI);;
CREATE TABLE RDF_LITERALS (
    ID CHAR(24) NOT NULL PRIMARY KEY,
    LANGUAGE VARCHAR(250),
# The biggest subset of the literal that the database can index
    LITERAL_IDX VARCHAR(32000) NOT NULL,
# The full literal, null if the LITERAL_IDX covers the whole literal
    LITERAL OID,
    INT_OK SMALLINT,            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT
);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;
CREATE INDEX RDF_IDX_INT_LITERALS ON RDF_LITERALS(INT_LITERAL);;
CREATE TABLE RDF_STATEMENTS (
    SUBJECT CHAR(24) NOT NULL,
    PREDICATE CHAR(24) NOT NULL,
    OBJECT CHAR(24),
    OBJECT_ISLITERAL SMALLINT,
    MODEL CHAR(24),
    ISREIFIED SMALLINT
#    STATEMENT_ID CHAR(24)
);;
CREATE INDEX RDF_IDX_SP ON RDF_STATEMENTS(SUBJECT, PREDICATE);;
CREATE INDEX RDF_IDX_O ON RDF_STATEMENTS(OBJECT, OBJECT_ISLITERAL);;
CREATE TABLE RDF_MODELS (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   ID CHAR(24)
);;

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.MODEL=S.MODEL    /* just a dummy to allow concatenation by ConstraintsGeneric */

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, S.PREDICATE, S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?




# SQL driver file tweaks for Mysql
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE RDF_LAYOUT_INFO (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   VAL VARCHAR(250)
);;
CREATE TABLE RDF_RESOURCES (
   ID ${id} NOT NULL PRIMARY KEY AUTO_INCREMENT,
   NAMESPACE ${id} NOT NULL,
   LOCALNAME VARCHAR(250) NOT NULL
);;
CREATE INDEX RDF_IDX_RESOURCES_NAME ON RDF_RESOURCES(NAMESPACE, LOCALNAME);;
CREATE TABLE RDF_NAMESPACES (
    ID ${id} NOT NULL PRIMARY KEY AUTO_INCREMENT,
    URI varchar(250) NOT NULL
);;
CREATE INDEX RDF_IDX_NAMESPACES ON RDF_NAMESPACES(URI);;
CREATE TABLE RDF_LITERALS (
    ID ${id} NOT NULL PRIMARY KEY AUTO_INCREMENT,
    LANGUAGE VARCHAR(250),
# The biggest subset of the literal that the database can index
    LITERAL_IDX VARCHAR(250) NOT NULL,
# The full literal, null if the LITERAL_IDX covers the whole literal
    LITERAL BLOB,
    INT_OK SMALLINT,            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT
);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;
CREATE INDEX RDF_IDX_INT_LITERALS ON RDF_LITERALS(INT_LITERAL);;
CREATE TABLE RDF_STATEMENTS (
    SUBJECT ${id} NOT NULL,
    PREDICATE ${id} NOT NULL,
    OBJECT ${id},
    OBJECT_ISLITERAL SMALLINT,
    MODEL ${id},
    ISREIFIED SMALLINT
#    STATEMENT_ID ${id}
);;
CREATE INDEX RDF_IDX_SP ON RDF_STATEMENTS(SUBJECT, PREDICATE);;
CREATE INDEX RDF_IDX_O ON RDF_STATEMENTS(OBJECT, OBJECT_ISLITERAL);;
CREATE TABLE RDF_MODELS (
   NAME VARCHAR(120),
   ID ${id} NOT NULL PRIMARY KEY AUTO_INCREMENT
);;
CREATE INDEX RDF_IDX_MODELS ON RDF_MODELS(NAME);;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# Generators to index the main tables
# Database specific ... not implemented here

#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
# Database specific ... not implemented here

#-------------------------------------------------------------------
# Insert a new namespace
insertNamespace
INSERT INTO RDF_NAMESPACES(URI) VALUES(?)

#-------------------------------------------------------------------
# Insert a new/duplicate resource
insertResource
INSERT INTO RDF_RESOURCES(LOCALNAME, NAMESPACE) VALUES(?,?)

#-------------------------------------------------------------------
# Insert a literal
insertLiteral
INSERT INTO RDF_LITERALS(LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_OK) VALUES (?,?,?,?,0)

#-------------------------------------------------------------------
# Insert a literal and an integer translation of it
insertLiteralInt
INSERT INTO RDF_LITERALS(LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_LITERAL, INT_OK) VALUES (?,?,?,?,?,1)

#-------------------------------------------------------------------
# Insert a statement
insertStatement
INSERT INTO RDF_STATEMENTS(SUBJECT, PREDICATE, OBJECT, OBJECT_ISLITERAL, MODEL) VALUES(?,?,?,?,?)

#-------------------------------------------------------------------
# Add a model/id map to the database
insertModelID
INSERT INTO RDF_MODELS (NAME) VALUES (?)

#-------------------------------------------------------------------
# List all the namespaces in the database mentioned in predicates
listNamespaces
SELECT DISTINCT URI  from
    RDF_NAMESPACES, RDF_STATEMENTS, RDF_RESOURCES
         WHERE PREDICATE = RDF_RESOURCES.ID AND NAMESPACE = RDF_NAMESPACES.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database - special version to support constraint commands
listConstraint
SELECT DISTINCT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP, RDF_LITERALS
WHERE S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# Return the ID of a namespace string, if it exists
getNamespaceID
SELECT ID FROM RDF_NAMESPACES WHERE STRCMP(URI,?) = 0

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralID
SELECT ID FROM RDF_LITERALS WHERE STRCMP(LITERAL_IDX,?) = 0 AND STRCMP(LANGUAGE,?) = 0

#-------------------------------------------------------------------
# Return the ID of the resource, if it exists
getResourceID
SELECT ID FROM RDF_RESOURCES WHERE STRCMP(LOCALNAME,?) = 0 AND STRCMP(NAMESPACE,?) = 0

#-------------------------------------------------------------------
# Drop all RDF procedures from a database
cleanDBprocedures
# Database specific ... not implemented here

#-------------------------------------------------------------------
# Drop all RDF tables from a database
cleanDBtables
DROP TABLE RDF_RESOURCES;;
DROP TABLE RDF_NAMESPACES;;
DROP TABLE RDF_LITERALS;;
DROP TABLE RDF_STATEMENTS;;
DROP TABLE RDF_LAYOUT_INFO;;
DROP TABLE RDF_MODELS;;




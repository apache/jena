# SQL driver file for a generic RDF table format which supports hash indexing
# with 24 char hashes (base64 encode of MD5 hash).
# Any generic format driver file needs to define the following operations:
# Initialize and cleanup database (statement groups):
#    initDBtables     initDBgenerators  initDBprocedures
#    cleanDBtables    cleanDBgenerators cleanDBprocedures
# Standard operations:
#    insertResource   insertLiteral     insertLiteralInt  insertStatement
#    getNamespace     getNamespaceID    listNamespaces
#    getResource      getResourceID	getLiteral        getLiteralID
#    listSubjects     deleteStatement
# Statement search combinations:
#    list  listS listP  listO  listSP  listPO  listSO  listSPO
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
CREATE TABLE RDF_LITERALS (
    LANGUAGE VARCHAR(250),
    LITERAL VARCHAR(32000),     /* longvarchar? clob? */
    INT_OK SMALLINT,            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT        /* doesn't seem to support booleans */
);;
CREATE TABLE RDF_LITERALS (
    ID CHAR(24) NOT NULL PRIMARY KEY,
    LANGUAGE VARCHAR(250),
# The biggest subset of the literal that the database can index
    LITERAL_IDX VARCHAR(32000) NOT NULL,
# The full literal, null if the LITERAL_IDX covers the whole literal
    LITERAL BLOB,
    INT_OK SMALLINT,            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT
);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;
CREATE TABLE RDF_STATEMENTS (
    SUBJECT CHAR(24) NOT NULL,
    PREDICATE CHAR(24) NOT NULL,
    OBJECT CHAR(24),
    OBJECT_ISLITERAL SMALLINT,
    MODEL INTEGER,
    ISREIFIED SMALLINT
);;
CREATE INDEX RDF_IDX_SP ON RDF_STATEMENTS(SUBJECT, PREDICATE);;
CREATE INDEX RDF_IDX_O ON RDF_STATEMENTS(OBJECT, OBJECT_ISLITERAL);;
#Do not index on models - throws some query planners complete off the scent
#CREATE INDEX RDF_IDX_M ON RDF_STATEMENTS(MODEL);;
CREATE TABLE RDF_MODELS (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   ID INTEGER
);;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# blank - hash version doesn't use generators

#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
# blank - generic hash version doesn't use stored procedures

#-------------------------------------------------------------------
# Insert a new namespace
insertNamespace
INSERT INTO RDF_NAMESPACES(ID, URI) VALUES(?,?)

#-------------------------------------------------------------------
# Insert a new/duplicate resource
insertResource
INSERT INTO RDF_RESOURCES(ID, LOCALNAME, NAMESPACE) VALUES(?,?,?)

#-------------------------------------------------------------------
# Insert a literal
insertLiteral
INSERT INTO RDF_LITERALS(ID, LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_OK) VALUES (?,?,?,?,?,0)

#-------------------------------------------------------------------
# Insert a literal and an integer translation of it
insertLiteralInt
INSERT INTO RDF_LITERALS(ID, LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_LITERAL, INT_OK) VALUES (?,?,?,?,?,?,1)

#-------------------------------------------------------------------
# Insert a statement
insertStatement
INSERT INTO RDF_STATEMENTS(SUBJECT, PREDICATE, OBJECT, OBJECT_ISLITERAL, MODEL) VALUES(?,?,?,?,?)

#-------------------------------------------------------------------
# List all statements in the model
listSubjects
SELECT DISTINCT ID, LOCALNAME, NAMESPACE FROM RDF_RESOURCES, RDF_STATEMENTS WHERE RDF_STATEMENTS.SUBJECT = RDF_RESOURCES.ID

#-------------------------------------------------------------------
# Return the namespace string corresponding to a given id
getNamespace
SELECT URI FROM RDF_NAMESPACES WHERE ID = ?

#-------------------------------------------------------------------
# Return the ID of a namespace string, if it exists
getNamespaceID
SELECT ID FROM RDF_NAMESPACES WHERE URI = ?

#-------------------------------------------------------------------
# Return the namespace ID and local name of a resource
getResource
SELECT LOCALNAME, NAMESPACE FROM RDF_RESOURCES WHERE ID = ?

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralID
SELECT ID FROM RDF_LITERALS WHERE LITERAL_IDX = ? AND LANGUAGE = ?

#-------------------------------------------------------------------
# Return the ID of the resource, if it exists
getResourceID
SELECT ID FROM RDF_RESOURCES WHERE LOCALNAME = ? AND NAMESPACE = ?

#-------------------------------------------------------------------
# List all the namespaces in the database mentioned in predicates
listNamespaces
SELECT URI FROM RDF_NAMESPACES WHERE ID IN
  (SELECT DISTINCT NAMESPACE FROM RDF_RESOURCES, RDF_STATEMENTS WHERE RDF_STATEMENTS.PREDICATE = RDF_RESOURCES.ID)

#-------------------------------------------------------------------
# Return the details of a literal
getLiteral
SELECT LITERAL, LITERAL_IDX, LANGUAGE, WELL_FORMED FROM RDF_LITERALS WHERE ID = ?

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
# but just used for testing of the statement is there so only returns a token item
checkStatement
SELECT SUBJECT FROM RDF_STATEMENTS WHERE SUBJECT = ? AND PREDICATE = ? AND OBJECT = ? AND OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# Delete the statement which matche the given subject, predicate object
deleteStatement
DELETE FROM RDF_STATEMENTS
WHERE SUBJECT = ? AND PREDICATE = ? AND OBJECT = ? AND OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# Drop all RDF procedures from a database
cleanDBprocedures
# blank - generic hash version doesn't use stored procedures

#-------------------------------------------------------------------
# Drop all RDF tables from a database
cleanDBtables
DROP TABLE RDF_RESOURCES;;
DROP TABLE RDF_NAMESPACES;;
DROP TABLE RDF_LITERALS;;
DROP TABLE RDF_STATEMENTS;;
DROP TABLE RDF_LAYOUT_INFO;;
DROP TABLE RDF_MODELS;;

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
# blank - hash version doesn't use generators

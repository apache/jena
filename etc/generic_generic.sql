# SQL driver file for a generic RDF table format.
# Any generic format driver file needs to define the following operations:
# Initialize and cleanup database (statement groups):
#    initDBtables     initDBgenerators  initDBprocedures
#    cleanDBtables    cleanDBgenerators cleanDBprocedures
# Standard operations:
#    getNamespace     getNamespaceID    allocateNamespaceID  insertNamespace
#    getResource      getResourceID	allocateResourceID   insertResource
#    getLiteral       getLiteralID      allocateLiteralID    insertLiteral insertLiteralInt
#    listSubjects     listNamespaces    deleteStatement      insertStatement
# Optional stored procedures for Proc versions
#    insertResourceProc   insertLiteralProc     insertLiteralIntProc
#    insertStatementProc  insertNamespaceProc
#
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
   ID ${id} NOT NULL PRIMARY KEY,
   NAMESPACE ${id} NOT NULL,
   LOCALNAME VARCHAR(250) NOT NULL
);;
CREATE INDEX RDF_IDX_RESOURCES_NAME ON RDF_RESOURCES(NAMESPACE, LOCALNAME);;
CREATE TABLE RDF_NAMESPACES (
    ID ${id} NOT NULL PRIMARY KEY,
    URI varchar(250) NOT NULL
);;
CREATE INDEX RDF_IDX_NAMESPACES ON RDF_NAMESPACES(URI);;
CREATE TABLE RDF_LITERALS (
    ID ${id} NOT NULL PRIMARY KEY,
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
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   ID ${id}
);;

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
SELECT DISTINCT ID, LOCALNAME, NAMESPACE FROM RDF_RESOURCES INNER JOIN RDF_STATEMENTS ON SUBJECT=ID

#-------------------------------------------------------------------
# Return the namespace string corresponding to a given id
getNamespace
SELECT URI FROM RDF_NAMESPACES WHERE ID = ?

#-------------------------------------------------------------------
# Return the namespace ID and local name of a resource
getResource
SELECT LOCALNAME, NAMESPACE FROM RDF_RESOURCES WHERE ID = ?

#-------------------------------------------------------------------
# Return the ID of a namespace string, if it exists
getNamespaceID
SELECT ID FROM RDF_NAMESPACES WHERE URI = ?

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
SELECT DISTINCT URI  from
    RDF_NAMESPACES INNER JOIN
        (RDF_STATEMENTS INNER JOIN RDF_RESOURCES ON PREDICATE = ID)
               ON RDF_NAMESPACES.ID = NAMESPACE

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
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES  RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
# Dummy to ensure Constraint processor and just append "AND <constraint>" statements
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.MODEL = S.MODEL

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES AS RS INNER JOIN (RDF_RESOURCES AS RP INNER JOIN RDF_STATEMENTS AS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

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

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
# Database specific ... not implemented here

#-------------------------------------------------------------------
# Template for creating attribute tables
createAttributeTable
CREATE TABLE RDF_A_${a} (
    SUBJECT ${id} NOT NULL,
    OBJECT ${id},
    OBJECT_ISLITERAL SMALLINT,
    PREDICATE ${id} NOT NULL,
    MODEL ${id}
);;
CREATE INDEX RDF_A_IDXS_${a} ON RDF_A_${a}(SUBJECT);;
CREATE INDEX RDF_A_IDXO_${a} ON RDF_A_${a}(OBJECT);;

#-------------------------------------------------------------------
# Insert an attribute entry
insertAttribute
INSERT INTO ?(SUBJECT, OBJECT, OBJECT_ISLITERAL, MODEL) VALUES(?,?,?,?)

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
alistP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_A_${a} S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
alistSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_A_${a} S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
alistPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_A_${a} S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
alistSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_A_${a} S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ?



# SQL driver file for Oracle suitable for multi-model formats.
# Changes are:
# 1. Limited indexable literal to 1000chars
# 2. Variant on getLiterID that ignores the language field (work around
#    for problem with Oracle thin driver not handling null strings as exptected)
# 3. Variants on list* which don't use "AS" table alias keyword
#
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
   LOCALNAME VARCHAR(250)
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
    LITERAL_IDX VARCHAR(1000) NOT NULL,
# The full literal, null if the LITERAL_IDX covers the whole literal
    LITERAL BLOB,
    INT_OK NUMBER(1),            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT
);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;
CREATE TABLE RDF_STATEMENTS (
    SUBJECT ${id} NOT NULL,
    PREDICATE ${id} NOT NULL,
    OBJECT ${id},
    OBJECT_ISLITERAL NUMBER(1),
    MODEL ${id},
    ISREIFIED SMALLINT,
    STATEMENT_ID ${id}
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
CREATE SEQUENCE RDF_RESOURCES_GEN;;
CREATE SEQUENCE RDF_NAMESPACE_GEN START WITH 2;;
CREATE SEQUENCE RDF_LITERALS_GEN;;
CREATE SEQUENCE RDF_MODELS_GEN;;
# Preallocate the anonymous namespace ID
INSERT INTO RDF_NAMESPACES (ID, URI) VALUES (0, '_');;

#-------------------------------------------------------------------
# Allocate an id for a resource
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateResourceID
SELECT RDF_RESOURCES_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Allocate an id for a NAMESPACE
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateNamespaceID
SELECT RDF_NAMESPACE_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT RDF_LITERALS_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Allocate an id for a model
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateModelID
SELECT RDF_MODELS_GEN.NEXTVAL FROM DUAL
#SELECT RDF_MODELS_GEN.NEXTVAL FROM RDF_LAYOUT_INFO

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralIDNoLang
SELECT ID FROM RDF_LITERALS WHERE LITERAL_IDX = ? AND LANGUAGE IS NULL

#-------------------------------------------------------------------
# Return the ID of the resource, if it exists - null localname
getResourceIDNoName
SELECT ID FROM RDF_RESOURCES WHERE NAMESPACE = ? AND LOCALNAME IS NULL

#-------------------------------------------------------------------
# List all the namespaces in the database mentioned in predicates
listNamespaces
SELECT DISTINCT N.URI
FROM RDF_NAMESPACES N, RDF_STATEMENTS S, RDF_RESOURCES R
WHERE
     S.PREDICATE = R.ID AND R.NAMESPACE = N.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_STATEMENTS S, RDF_RESOURCES RS, RDF_RESOURCES RP
WHERE S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND S.MODEL = ?

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
DROP SEQUENCE RDF_RESOURCES_GEN;;
DROP SEQUENCE RDF_NAMESPACE_GEN;;
DROP SEQUENCE RDF_LITERALS_GEN;;
DROP SEQUENCE RDF_MODELS_GEN;;


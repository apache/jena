# SQL driver file tweaks for postgres
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
    LITERAL OID,
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
CREATE SEQUENCE RDF_RESOURCES_GEN;;
CREATE SEQUENCE RDF_NAMESPACE_GEN START 2;;
CREATE SEQUENCE RDF_LITERALS_GEN;;
CREATE SEQUENCE RDF_MODELS_GEN;;
# Preallocate the anonymous namespace ID
INSERT INTO RDF_NAMESPACES (ID, URI) VALUES (0, '_');;

#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
# Database specific ... not implemented here

#-------------------------------------------------------------------
# Allocate an id for a resource
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateResourceID
SELECT NEXTVAL('RDF_RESOURCES_GEN')

#-------------------------------------------------------------------
# Allocate an id for a NAMESPACE
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateNamespaceID
SELECT NEXTVAL('RDF_NAMESPACE_GEN')

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT NEXTVAL('RDF_LITERALS_GEN')

#-------------------------------------------------------------------
# Allocate an id for a model
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateModelID
SELECT NEXTVAL('RDF_MODELS_GEN')

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
DROP SEQUENCE RDF_RESOURCES_GEN;;
DROP SEQUENCE RDF_NAMESPACE_GEN;;
DROP SEQUENCE RDF_LITERALS_GEN;;
DROP SEQUENCE RDF_MODELS_GEN;;




# SQL driver file for a generic RDF table format tweaked for Postgres.
# Changed "BLOB" to "OID".
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE RDF_LAYOUT_INFO (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   VAL VARCHAR(250)
);;
CREATE TABLE RDF_RESOURCES (
   ID INTEGER NOT NULL PRIMARY KEY,
   NAMESPACE INTEGER NOT NULL,
   LOCALNAME VARCHAR(250) NOT NULL
);;
CREATE INDEX RDF_IDX_RESOURCES_NAME ON RDF_RESOURCES(NAMESPACE, LOCALNAME);;
CREATE TABLE RDF_NAMESPACES (
    ID INTEGER NOT NULL PRIMARY KEY,
    URI varchar(250) NOT NULL
);;
CREATE INDEX RDF_IDX_NAMESPACES ON RDF_NAMESPACES(URI);;
CREATE TABLE RDF_LITERALS (
    ID INTEGER NOT NULL PRIMARY KEY,
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
    SUBJECT INTEGER NOT NULL,
    PREDICATE INTEGER NOT NULL,
    OBJECT INTEGER,
    OBJECT_ISLITERAL SMALLINT,
    MODEL INTEGER,
    ISREIFIED SMALLINT
#    STATEMENT_ID INTEGER
);;
CREATE INDEX RDF_IDX_SP ON RDF_STATEMENTS(SUBJECT, PREDICATE);;
CREATE INDEX RDF_IDX_O ON RDF_STATEMENTS(OBJECT, OBJECT_ISLITERAL);;
CREATE TABLE RDF_MODELS (
   NAME VARCHAR(120) NOT NULL PRIMARY KEY,
   ID INTEGER
);;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# Generators to index the main tables
CREATE SEQUENCE RDF_RESOURCES_GEN;;
CREATE SEQUENCE RDF_NAMESPACE_GEN START 2;
CREATE SEQUENCE RDF_LITERALS_GEN;;
# Preallocate the anonymous namespace ID
INSERT INTO RDF_NAMESPACES (ID, URI) VALUES (0, '_');;

#-------------------------------------------------------------------
# Allocate an id for a resource
# Interbase does obey the select syntax the needs a non-empty table in the from field
allocateResourceID
SELECT nextval('RDF_RESOURCES_GEN')

#-------------------------------------------------------------------
# Allocate an id for a NAMESPACE
# Interbase does obey the select syntax the needs a non-empty table in the from field
allocateNamespaceID
SELECT nextval('RDF_NAMESPACE_GEN')

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase does obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT nextval('RDF_LITERALS_GEN')

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
DROP SEQUENCE RDF_RESOURCES_GEN;;
DROP SEQUENCE RDF_NAMESPACE_GEN;;
DROP SEQUENCE RDF_LITERALS_GEN;;



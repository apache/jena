# SQL driver file for InterBase suitable for multi-model formats.
# This version uses procedures to check for duplicates before
# allocating and ID and doing the insert.
# Just overrides the init and clean up operations from the generic_generic
# table to meet the Interbase constraints and defined appropriate stored procedures.
# Also replaces the listXXX operations to drop the "AS" keyword from aliases
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
   LOCALNAME VARCHAR(160) NOT NULL  /* Note Interbase can index more than 180 in the joint idx */
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
    LITERAL_IDX VARCHAR(250) NOT NULL,
# The full literal, null if the LITERAL_IDX covers the whole literal
    LITERAL BLOB,
    INT_OK SMALLINT,            /* flag that literal is interpetable as an int */
    INT_LITERAL INT,
    WELL_FORMED SMALLINT
);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;
CREATE TABLE RDF_STATEMENTS (
    SUBJECT ${id} NOT NULL,
    PREDICATE ${id} NOT NULL,
    OBJECT ${id},
    OBJECT_ISLITERAL SMALLINT,
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
CREATE GENERATOR RDF_RESOURCES_GEN;;
CREATE GENERATOR RDF_NAMESPACE_GEN;;
CREATE GENERATOR RDF_LITERALS_GEN;;
CREATE GENERATOR RDF_MODELS_GEN;;
# Preallocate the anonymous namespace ID
INSERT INTO RDF_NAMESPACES (ID, URI) VALUES (0, '_');;
SET GENERATOR RDF_NAMESPACE_GEN TO 1;;

#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
# Procedure to insert a namespace into the namesapce table
CREATE PROCEDURE RDF_NAMESPACE_INSERT (NSNAME VARCHAR(250)) RETURNS (NSIDOUT INTEGER) AS
BEGIN
    /* Assign a namespace id first */
    SELECT ID FROM RDF_NAMESPACES WHERE URI = :nsname INTO :nsidout ;
    IF (NSIDOUT IS NULL) THEN
    BEGIN
        NSIDOUT = GEN_ID(RDF_NAMESPACE_GEN, 1);
        INSERT INTO RDF_NAMESPACES(ID, URI) VALUES (:nsidout, :nsname);
    END
END;;
# Procedure to insert a resource into the resources table if it is not already there
CREATE PROCEDURE RDF_RESOURCE_INSERT (LNAME VARCHAR(180),NSID INTEGER) RETURNS (ID INTEGER) AS
BEGIN
    SELECT ID FROM RDF_RESOURCES WHERE LOCALNAME = :lname AND NAMESPACE = :nsid INTO :id ;
    IF (ID IS NULL) THEN
    BEGIN
      ID = GEN_ID(RDF_RESOURCES_GEN, 1);
      INSERT INTO RDF_RESOURCES (ID, LOCALNAME, NAMESPACE) VALUES (:id, :lname, :nsid);
    END
END;;
# Procedure to insert a literal into the literals table and return the new ID
CREATE PROCEDURE RDF_LITERALS_INSERT (LITERAL_IDX VARCHAR(180), LITERAL BLOB, LANGUAGE VARCHAR(250), WELL_FORMED SMALLINT) RETURNS (ID INTEGER) AS
BEGIN
    SELECT ID FROM RDF_LITERALS L WHERE L.LITERAL_IDX = :literal_idx AND L.LANGUAGE = :language INTO :id;
    IF (ID IS NULL) THEN
    BEGIN
        ID = GEN_ID(RDF_LITERALS_GEN, 1);
        INSERT INTO RDF_LITERALS (ID, LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_OK) VALUES (:id, :literal_idx, :literal, :language, :well_formed, 0);
    END
END;;
# Procedure to insert a literal interpretable as an int into the literals table and return the new ID
CREATE PROCEDURE RDF_LITERALS_INSERT_INT (LITERAL_IDX VARCHAR(180), LITERAL BLOB, LANGUAGE VARCHAR(250), WELL_FORMED SMALLINT, IVAL INT) RETURNS (ID INTEGER) AS
BEGIN
    SELECT ID FROM RDF_LITERALS L WHERE L.LITERAL_IDX = :literal_idx AND L.LANGUAGE = :language INTO :id;
    IF (ID IS NULL) THEN
    BEGIN
        ID = GEN_ID(RDF_LITERALS_GEN, 1);
        INSERT INTO RDF_LITERALS (ID, LITERAL_IDX, LITERAL, LANGUAGE, WELL_FORMED, INT_LITERAL, INT_OK) VALUES (:id, :literal_idx, :literal, :language, :well_formed, :ival, 1);
    END
END;;
# Procedure to insert a statement only if it does not already exist
CREATE PROCEDURE RDF_STATEMENT_INSERT (SUBJECT INTEGER, PREDICATE INTEGER, OBJECT INTEGER, ISLITERAL SMALLINT, MODEL INTEGER) AS
    DECLARE VARIABLE SID INTEGER;
BEGIN
    SELECT SUBJECT FROM RDF_STATEMENTS WHERE SUBJECT = :subject AND PREDICATE = :predicate AND OBJECT = :object AND OBJECT_ISLITERAL = :isliteral AND MODEL = :model INTO :sid;
    IF (SID IS NULL) THEN
    BEGIN
        INSERT INTO RDF_STATEMENTS  (SUBJECT, PREDICATE, OBJECT, OBJECT_ISLITERAL, MODEL) VALUES (:subject, :predicate, :object, :isliteral, :model);
    END
END;;

#-------------------------------------------------------------------
# Allocate an id for a resource
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateResourceID
SELECT GEN_ID(RDF_RESOURCES_GEN,1) FROM RDF_LAYOUT_INFO

#-------------------------------------------------------------------
# Allocate an id for a NAMESPACE
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateNamespaceID
SELECT GEN_ID(RDF_NAMESPACE_GEN,1) FROM RDF_LAYOUT_INFO

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT GEN_ID(RDF_LITERALS_GEN,1) FROM RDF_LAYOUT_INFO

#-------------------------------------------------------------------
# Allocate an id for a model
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateModelID
SELECT GEN_ID(RDF_MODELS_GEN,1) FROM RDF_LAYOUT_INFO

#-------------------------------------------------------------------
# Insert a new namespace
insertNamespaceProc
EXECUTE PROCEDURE RDF_NAMESPACE_INSERT(?)

#-------------------------------------------------------------------
# Insert a new/duplicate resource
insertResourceProc
EXECUTE PROCEDURE RDF_RESOURCE_INSERT(?,?)

#-------------------------------------------------------------------
# Insert a literal
insertLiteralProc
EXECUTE PROCEDURE RDF_LITERALS_INSERT(?,?,?,?)

#-------------------------------------------------------------------
# Insert a literal intepretable as an int
insertLiteralIntProc
EXECUTE PROCEDURE RDF_LITERALS_INSERT_INT(?,?,?,?,?)

#-------------------------------------------------------------------
# Insert a statement
insertStatementProc
EXECUTE PROCEDURE RDF_STATEMENT_INSERT(?,?,?,?,?)

#-------------------------------------------------------------------
# List all the statements in the database
list
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject
listS
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.SUBJECT = RS.ID AND S.PREDICATE = RP.ID AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate
listP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and subject
listSP
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given object
listO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject and object
listSO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given predicate and object
listPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
listSPO
SELECT S.SUBJECT, RS.LOCALNAME, RS.NAMESPACE,
       S.PREDICATE, RP.LOCALNAME, RP.NAMESPACE,
       S.OBJECT, S.OBJECT_ISLITERAL
FROM RDF_RESOURCES RS INNER JOIN (RDF_RESOURCES RP INNER JOIN RDF_STATEMENTS S ON PREDICATE = RP.ID) ON SUBJECT = RS.ID
WHERE SUBJECT = RS.ID AND PREDICATE = RP.ID AND S.SUBJECT = ? AND S.PREDICATE = ? AND S.OBJECT = ? AND S.OBJECT_ISLITERAL = ? AND MODEL = ?

#-------------------------------------------------------------------
# List all the statements in the database matching a given subject, predicate and object
# but just used for testing of the statement is there so only returns a token item
checkStatement
SELECT SUBJECT FROM RDF_STATEMENTS WHERE SUBJECT = ? AND PREDICATE = ? AND OBJECT = ? AND OBJECT_ISLITERAL = ? AND MODEL = ?

#-------------------------------------------------------------------
# Drop all RDF procedures from a database
cleanDBprocedures
DROP PROCEDURE RDF_RESOURCE_INSERT;;
DROP PROCEDURE RDF_LITERALS_INSERT;;
DROP PROCEDURE RDF_LITERALS_INSERT_INT;;
DROP PROCEDURE RDF_STATEMENT_INSERT;;
DROP PROCEDURE RDF_NAMESPACE_INSERT;;

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
DELETE FROM RDB$GENERATORS WHERE RDB$GENERATOR_NAME = 'RDF_RESOURCES_GEN'
                              OR RDB$GENERATOR_NAME = 'RDF_NAMESPACE_GEN'
                              OR RDB$GENERATOR_NAME = 'RDF_LITERALS_GEN'
                              OR RDB$GENERATOR_NAME = 'RDF_MODELS_GEN'


# Generic SQL driver file for Oracle suitable for multi-model formats
#
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE RDF_StmtAsserted (
 SubjRes       VARCHAR(250) NOT NULL,
 PropRes       VARCHAR(250) NOT NULL,
 ObjRes        VARCHAR(250),
 ObjStr        VARCHAR(250),
 ObjLiteral    VARCHAR(250),
 GraphID       VARCHAR(250),
 CONSTRAINT uniq_subj_prop_obj
 UNIQUE (SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID)
);;
CREATE TABLE RDF_SystemStmtAsserted (
 SubjRes       VARCHAR(250) NOT NULL,
 PropRes       VARCHAR(250) NOT NULL,
 ObjRes        VARCHAR(250),
 ObjStr        VARCHAR(250),
 ObjLiteral    VARCHAR(250),
 GraphID       VARCHAR(250),
 CONSTRAINT uniq_subj_prop_obj
 UNIQUE (SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID)
);;
CREATE TABLE RDF_LITERALS (
 LiteralId     VARCHAR(250) NOT NULL PRIMARY KEY,
 LiteralIdx     VARCHAR(1000) NOT NULL UNIQUE,
 TypeRes       VARCHAR(250),
 Lang          VARCHAR(250),
 AsBLOB          BLOB,
 AsFloat       FLOAT,
 AsInt         INTEGER,
 ObjXSDType    VARCHAR(250)
);;
CREATE TABLE RDF_Graph(
 GraphId     VARCHAR(250) NOT NULL PRIMARY KEY,
 ReifierGraph        VARCHAR(250),
 ParentGraph         VARCHAR(250)
);;
CREATE INDEX RDF_IDX_SUBJ_PROP ON RDF_StmtAsserted(SubjRes, PropRes);;
CREATE INDEX RDF_IDX_OBJ ON RDF_StmtAsserted(ObjRes);;
CREATE INDEX RDF_IDX_SUBJ_PROP ON RDF_SystemStmtAsserted(SubjRes, PropRes);;
CREATE INDEX RDF_IDX_OBJ ON RDF_SystemStmtAsserted(ObjRes);;
CREATE INDEX RDF_IDX_LITERALS ON RDF_LITERALS(LITERAL_IDX);;

#-------------------------------------------------------------------
# Create a blank statement table - and indexes - compound statement group
createStatementTable
CREATE TABLE ${a} (
 SubjRes       VARCHAR(250) NOT NULL,
 PropRes       VARCHAR(250) NOT NULL,
 ObjRes        VARCHAR(250),
 ObjStr        VARCHAR(250),
 ObjLiteral    VARCHAR(250),
 GraphID       VARCHAR(250),
 CONSTRAINT uniq_subj_prop_obj
 UNIQUE (SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID)
);;
CREATE INDEX RDF_IDX_SUBJ_PROP ON ${a}(SubjRes, PropRes);;
CREATE INDEX RDF_IDX_OBJ ON ${a}(ObjRes);;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# Generators to index the main tables
CREATE SEQUENCE RDF_GRAPHID_GEN START WITH 2;;
CREATE SEQUENCE RDF_LITERALID_GEN;;

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT RDF_LITERALID_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Allocate an id for a GRAPH
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateGraphID
SELECT RDF_GRAPHID_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertStatementLiteralURI
INSERT INTO ${a} (SubjRes, PropRes, ObjRes, GraphID) VALUES (?, ?, ?, ?)

#-------------------------------------------------------------------
# Insert an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
insertStatementLiteralRef
INSERT INTO ${a} (SubjRes, PropRes, ObjLiteral, ObjStr, GraphID) VALUES (?, ?, ?, ?, ?)


#-------------------------------------------------------------------
# Insert an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
insertStatementLiteralVal
INSERT INTO ${a} (SubjRes, PropRes, ObjStr, GraphID) VALUES (?, ?, ?, ?)

#-------------------------------------------------------------------
# Return the count of rows in the table 
getRowCount
SELECT COUNT(*) FROM ${a}

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralIDNoLang
SELECT LITERALID FROM RDF_LITERALS WHERE LITERAL_IDX = ? AND LANGUAGE IS NULL

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
# Special case where the literal is was an empty string - in this case we
# put marker text in the literal field but there will also be a blob giving
# the true empty string literal
getLiteralIDNoLangNullLiteral
SELECT LITERALID FROM RDF_LITERALS WHERE LITERAL_IDX = ? AND LANGUAGE IS NULL AND LITERAL IS NOT NULL

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
# Special case where the literal is was an empty string - in this case we
# put marker text in the literal field but there will also be a blob giving
# the true empty string literal
getLiteralIDNullLiteral
SELECT LITERALID FROM RDF_LITERALS WHERE LITERAL_IDX = ? AND LANGUAGE = ? AND LITERAL IS NOT NULL


#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
SelectStatement
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject 
SelectStatementS
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} WHERE S.SubjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSP
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} WHERE S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSPOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} WHERE S.ObjRes = ? AND S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ? 

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSPOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} WHERE S.ObjRes = ? AND S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ? 

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


# Generic SQL driver file for MySQL suitable for multi-model formats
#
############################################
# TABLE CREATION
############################################
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE JENA_SYS_STMTASSERTED (
 SubjRes       VARCHAR2(250) NOT NULL,
 PropRes       VARCHAR2(250) NOT NULL,
 ObjRes        VARCHAR2(250),
 ObjStr        VARCHAR2(4000),
 ObjLiteral    INTEGER,
 GraphID       VARCHAR2(250),
 CONSTRAINT uniq_jena_sys_sasserted_spo 
   UNIQUE ( SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID )
);;
CREATE TABLE JENA_LITERALS (
 LiteralId     INTEGER NOT NULL PRIMARY KEY,
 LiteralIdx    VARCHAR2(1000) NOT NULL,
 Lang          VARCHAR2(250),
 asBLOB          VARCHAR2(4000),
 AsFloat       FLOAT,
 AsInt         INTEGER,
 XSDType    VARCHAR2(250)
);;
CREATE TABLE JENA_SYS_STMTREIFIED (
 SubjRes       VARCHAR2(250),
 PropRes       VARCHAR2(250),
 ObjRes        VARCHAR2(250),
 ObjStr        VARCHAR2(4000),
 ObjLiteral    INTEGER,
 GraphID       VARCHAR2(250),
 StmtRes		VARCHAR2(250) NOT NULL,
 HasType		INTEGER,
 CONSTRAINT uniq_jena_sysr_sasserted_spo 
   UNIQUE ( SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID )
);;
CREATE UNIQUE INDEX JENA_IDXR_STMT ON JENA_SYS_STMTREIFIED(StmtRes, HasType);;
CREATE INDEX JENA_IDXR_SUBJ_PROP ON JENA_SYS_STMTREIFIED(SubjRes, PropRes);;
CREATE INDEX JENA_IDXR_OBJ ON JENA_SYS_STMTREIFIED(ObjRes);;
CREATE INDEX JENA_IDX_SUBJ_PROP ON JENA_SYS_STMTASSERTED(SubjRes, PropRes);;
CREATE INDEX JENA_IDX_OBJ ON JENA_SYS_STMTASSERTED(ObjRes);;

#-------------------------------------------------------------------
# Create a blank statement table - and indexes - compound statement group
createStatementTable
CREATE TABLE ${a} (
 SubjRes       VARCHAR2(250) NOT NULL,
 PropRes       VARCHAR2(250) NOT NULL,
 ObjRes        VARCHAR2(250),
 ObjStr        VARCHAR2(4000),
 ObjLiteral    INTEGER,
 GraphID       VARCHAR2(250),
 CONSTRAINT ${a}_uspo 
   UNIQUE (SubjRes, PropRes, ObjRes, ObjStr, ObjLiteral, GraphID)
);;
CREATE INDEX ${a}_ISP ON ${a}(SubjRes, PropRes);;
CREATE INDEX ${a}_IO ON ${a}(ObjRes);;

#-------------------------------------------------------------------
# Create a blank reified statement table - and indexes - compound statement group
createReifStatementTable
CREATE TABLE ${a} (
 SubjRes       VARCHAR2(250),
 PropRes       VARCHAR2(250),
 ObjRes        VARCHAR2(250),
 ObjStr        VARCHAR2(4000),
 ObjLiteral    INTEGER,
 GraphID       VARCHAR2(250),
 StmtRes		VARCHAR2(250) NOT NULL,
 HasType		INTEGER
);;
CREATE UNIQUE INDEX ${a}_IDX_STMT ON ${a}(StmtRes, HasType);;
CREATE INDEX ${a}_ISP ON ${a}(SubjRes, PropRes);;
CREATE INDEX ${a}_IO ON ${a}(ObjRes);;
 
############################################
# INITIALIZATION
############################################
#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# Generators to index the main tables
CREATE SEQUENCE JENA_LITERALS_GEN;;
CREATE SEQUENCE JENA_GRAPHS_GEN;;

#-------------------------------------------------------------------
# Initialize a blank database - create any stored procedures - compound statement group
initDBprocedures
# Database specific ... not supported for MySQL

############################################
# DELETION
############################################
#-------------------------------------------------------------------
# Delete an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
deleteStatementObjectURI
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjRes = ? AND GraphID = ?)

#-------------------------------------------------------------------
# Remove all rows from given table with the given GraphID.
# Substitutes table name 
removeRowsFromTable
DELETE FROM ${a} WHERE (GraphID = ?)

#-------------------------------------------------------------------
# Delete an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
deleteStatementLiteralRef
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjLiteral = ? AND GraphID = ?)

#-------------------------------------------------------------------
# Delete an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
deleteStatementLiteralVal
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjStr = ? AND ObjLiteral is Null AND GraphID = ?)

############################################
# INSERT
############################################
#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertStatementObjectURI
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
# Insert a non-Blob literal string
insertLiteral
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX, LANG, XSDType) VALUES (?,?,?,?)

#-------------------------------------------------------------------
# Insert a Blob literal string
insertLiteralBlob
INSERT INTO JENA_LITERALS(LITERALIDX, AsBLOB, LANG, XSDType) VALUES (?,?,?,?)

#-------------------------------------------------------------------
# Insert a non-Blob literal string with Lang
insertLiteralLang
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX, LANG) VALUES (?,?)

#-------------------------------------------------------------------
# Insert a non-Blob literal string without Lang
insertLiteralNoLang
INSERT INTO JENA_LITERALS(LITERALIDX) VALUES (?)

#-------------------------------------------------------------------
# Insert a literal string
insertLiteralBlobLang
INSERT INTO JENA_LITERALS(LITERALIDX,AsBLOB, LANG) VALUES (?,?,?)

#-------------------------------------------------------------------
# Insert a literal INTEGER
insertLiteralIntLang
INSERT INTO JENA_LITERALS(LITERALIDX,AsInt, LANG) VALUES (?,?,?)

#-------------------------------------------------------------------
# Insert a literal string
insertLiteralBlobNoLang
INSERT INTO JENA_LITERALS(LITERALIDX,AsBLOB) VALUES (?,?)

#-------------------------------------------------------------------
# Insert a literal int
insertLiteralIntNoLang
INSERT INTO JENA_LITERALS(LITERALIDX,AsInt) VALUES (?,?)

#-------------------------------------------------------------------
# Insert a non-Blob literal string with Lang
insertLiteralwithIdLang
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX, LANG) VALUES (?,?,?)

#-------------------------------------------------------------------
# Insert a non-Blob literal string without Lang
insertLiteralwithIdNoLang
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX) VALUES (?,?)

#-------------------------------------------------------------------
# Insert a literal blob
insertLiteralwithIdLangBlob
INSERT INTO JENA_LITERALS(LITERALID,LITERALIDX,LANG, AsBLOB) VALUES (?,?,?,EMPTY_BLOB())

#-------------------------------------------------------------------
# lock literal with blob
lockLiteralwithIdBlob
SELECT ASBLOB FROM JENA_LITERALS WHERE LITERALID = ? FOR UPDATE

#-------------------------------------------------------------------
# Set a literal blob
updateBlobForLiteralID
UPDATE JENA_LITERALS SET ASBLOB = ? WHERE LITERALID = ?

#-------------------------------------------------------------------
# Insert a literal int
insertLiteralwithIdIntLang
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX,AsInt, LANG) VALUES (?,?,?,?)

#-------------------------------------------------------------------
# Insert a literal string
insertLiteralwithIdNoLangBlob
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX) VALUES (?,?)
#INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX,AsBLOB) VALUES (?,?,EMPTY_BLOB())

#-------------------------------------------------------------------
# Insert a literal int
insertLiteralIntwithIdNoLang
INSERT INTO JENA_LITERALS(LITERALID, LITERALIDX,AsInt) VALUES (?,?,?)

############################################
# QUERIES
############################################
#-------------------------------------------------------------------
# Return the count of rows in the table 
getRowCount
SELECT COUNT(*) FROM ${a}

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralID
SELECT LITERALID FROM JENA_LITERALS WHERE LITERALIDX = ? AND LANG = ?

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralIDNoLang
SELECT LITERALID FROM JENA_LITERALS WHERE LITERALIDX = ? AND LANG IS NULL

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
# Special case where the literal is was an empty string - in this case we
# put marker text in the literal field but there will also be a blob giving
# the true empty string literal
getLiteralIDNoLangNullLiteral
SELECT LITERALID FROM JENA_LITERALS WHERE LITERALIDX = ? AND LANG IS NULL

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
# Special case where the literal is was an empty string - in this case we
# put marker text in the literal field but there will also be a blob giving
# the true empty string literal
getLiteralIDNullLiteral
SELECT LITERALID FROM JENA_LITERALS WHERE LITERALIDX = ? AND lang = ?

#-------------------------------------------------------------------
# Return the details of a literal
getLiteral
SELECT asBLOB, LITERALIDX, Lang, XSDType FROM JENA_LITERALS WHERE LITERALID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
SelectStatement
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject 
SelectStatementS
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.SubjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSP
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSPOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjRes = ? AND S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ? 

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject, Property, and Object as Value
SelectStatementSPOV
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjStr = ? AND S.ObjLiteral IS NULL AND S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject, Property, and Object as LiteralRef
SelectStatementSPOR
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjLiteral = ? AND S.SubjRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Object as URI
SelectStatementSOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjRes = ? AND S.SubjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Object as Value
SelectStatementSOV
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjStr = ? AND S.SubjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject, Property, and Object as LiteralRef
SelectStatementSOR
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjLiteral = ? AND S.SubjRes = ? AND AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property and Object as URI
SelectStatementPOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property and Object as Value
SelectStatementPOV
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjStr = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same  Object as LiteralRef
SelectStatementPOR
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjLiteral = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same  Object as URI
SelectStatementOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Object as Value
SelectStatementOV
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjStr = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Object as LiteralRef
SelectStatementOR
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.ObjLiteral = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property 
SelectStatementP
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral 
FROM ${a} S WHERE S.PropRes = ? AND S.GraphID = ?

############################################
# SEQUENCES
############################################
#-------------------------------------------------------------------
# Select sequence name from sequences
SelectSequenceName
SELECT SEQUENCE_NAME FROM SEQ WHERE SEQUENCE_NAME = ?

#-------------------------------------------------------------------
# Select JENA sequence name from sequences
SelectJenaSequences
SELECT SEQUENCE_NAME FROM SEQ WHERE SEQUENCE_NAME LIKE ('JENA%')

#-------------------------------------------------------------------
# Select sequence name from sequences
DropSequence
DROP SEQUENCE ${a};;

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID
SELECT JENA_LITERALS_GEN.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Allocate an id for a GRAPH
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateGraphID
SELECT JENA_GRAPHS_GEN.NEXTVAL FROM DUAL

############################################
# REIFICATION
############################################
#-------------------------------------------------------------------
# Select all the statements in an Reified Statement (triple store) graph
SelectAllReifStatement
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an reified Statement (triple store) graph
SelectAllReifTypeStmt
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI
SelectReifStatement
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI and that have the HasType property defined
SelectReifTypeStatement
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Delete an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
deleteReifStatementObjectURI
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjRes = ? AND GraphID = ?
AND StmtRes = ?)

#-------------------------------------------------------------------
# Delete an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
deleteReifStatementLiteralVal
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjStr = ? AND ObjLiteral is null AND GraphID = ?
AND StmtRes = ?)

#-------------------------------------------------------------------
# Delete an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
deleteReifStatementLiteralRef
Delete FROM ${a} WHERE (SubjRes = ? AND PropRes = ? AND ObjLiteral = ? AND GraphID = ?
AND StmtRes = ?)

#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertReifStatementObjectURI
INSERT INTO ${a} (SubjRes, PropRes, ObjRes, GraphID, StmtRes, HasType) VALUES (?, ?, ?, ?, ?, ?)

#-------------------------------------------------------------------
# Insert an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
insertReifStatementLiteralRef
INSERT INTO ${a} (SubjRes, PropRes, ObjLiteral, ObjStr, GraphID, StmtRes, HasType) VALUES (?, ?, ?, ?, ?, ?, ?)

#-------------------------------------------------------------------
# Insert an triple with a Simple String literal into a Statement table, 
# substituting Statement table name 
# and taking values as arguments
insertReifStatementLiteralVal
INSERT INTO ${a} (SubjRes, PropRes, ObjStr, GraphID, StmtRes, HasType) VALUES (?, ?, ?, ?, ?, ?)

#-------------------------------------------------------------------
# Update the subject of a reified statement 
updateReifSubj
UPDATE ${a} SET SubjRes=? WHERE StmtRes = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the property of a reified statement 
updateReifProp
UPDATE ${a} SET PropRes=? WHERE StmtRes = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the object of a reified statement 
updateReifObj
UPDATE ${a} SET ObjRes=?, ObjStr=? ObjLiteral=? WHERE StmtRes = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the hasType of a reified statement 
updateReifHasType
UPDATE ${a} SET HasType=? WHERE StmtRes = ? AND GraphID = ?

#-------------------------------------------------------------------
# Find the reified statements with the given subject 
findFragSubj
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.SubjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given property 
findFragProp
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.PropRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given object resource
findFragObjOU
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.ObjRes = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given object string
findFragObjOV
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.ObjStr = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given object literal
findFragObjOL
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.ObjLiteral = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given hasType 
findFragHasType
SELECT S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral, S.StmtRes, S.HasType 
FROM ${a} S WHERE S.StmtRes = ? AND S.HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (resource) 
SelectReifURIByOU
SELECT S.StmtRes
FROM ${a} S WHERE S.SubjRes = ? AND S.PropRes = ? and S.ObjRes = ? AND S.GraphID = ? AND S.HasType = 1

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (string) 
SelectReifURIByOV
SELECT S.StmtRes
FROM ${a} S WHERE S.SubjRes = ? AND S.PropRes = ? and S.ObjStr = ? AND S.GraphID = ? AND S.HasType = 1

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (reference) 
SelectReifURIByOR
SELECT S.StmtRes
FROM ${a} S WHERE S.SubjRes = ? AND S.PropRes = ? and S.ObjLiteral = ? AND S.GraphID = ? AND S.HasType = 1

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (reference) 
SelectReifURI
SELECT S.StmtRes
FROM ${a} S WHERE S.GraphID = ? AND S.HasType = 1

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# that partially reify something 
SelectAllReifNodes
SELECT DISTINCT S.StmtRes
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Determine if the statement URI's partially reifies anything in a Reified
# Statement (triple store) graph
SelectReifNode
SELECT DISTINCT S.StmtRes
FROM ${a} S WHERE S.StmtRes = ? AND S.GraphID = ?

############################################
# CLEAN UP
############################################
#-------------------------------------------------------------------
# Drop named  table
dropTable
DROP TABLE ${a};;

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
#no-op because mySQL does not support sequences


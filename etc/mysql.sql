# Generic SQL driver file for MySQL suitable for multi-model formats
#
#-------------------------------------------------------------------
# Initialize a blank database - create system tables and indexes
#
# Parameters:
# a - column type for subj, prop, obj
#     VARCHAR(nn) BINARY if DBHasXact is true,
#        where nn is DBLongObjectLen (requires that nn <= 250)
#     else TINYBLOB if DBLongObjectLen <= 250; else MEDIUMBLOB
# b - table implementation type
#     INNODB if DBHasXact is true;
#     else MyISAM
# c - index key length, set to DBIndexKeyLen, default 250
#
# Note that the tables JENA_LONG_LIT, JENA_LONG_URI, JENA_PREFIX
# all have the same structure. These are used to store long objects.
# Tthere chould (should) be a separate operation to create
# these tables to ensure they have an identical structure.
# This is not urgent - left for future work.
#
initDBtables
DROP TABLE IF EXISTS JENA_SYS_STMT;;
CREATE TABLE JENA_SYS_STMT (
 Subj       ${a} NOT NULL,
 Prop       ${a} NOT NULL,
 Obj        ${a} NOT NULL,
 GraphID    INTEGER
) Type = ${b};;
DROP TABLE IF EXISTS JENA_LONG_LIT;;
CREATE TABLE JENA_LONG_LIT (
 ID      INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
 Head    ${a} NOT NULL,
 Hash    BIGINT,
 Tail    MEDIUMBLOB
) Type = ${b};;
DROP TABLE IF EXISTS JENA_LONG_URI;;
CREATE TABLE JENA_LONG_URI (
 ID      INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
 Head    ${a} NOT NULL,
 Hash    BIGINT,
 Tail    MEDIUMBLOB
) Type = ${b};;
DROP TABLE IF EXISTS JENA_PREFIX;;
CREATE TABLE JENA_PREFIX (
 ID      INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
 Head    ${a} NOT NULL,
 Hash    BIGINT,
 Tail    MEDIUMBLOB
) Type = ${b};;
DROP TABLE IF EXISTS JENA_GRAPH;;
CREATE TABLE JENA_GRAPH (
 ID      INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
 Name    TINYBLOB NOT NULL
) Type = ${b};;
CREATE UNIQUE INDEX JENA_IXLIT ON JENA_LONG_LIT(Head(${c}),Hash);;
CREATE UNIQUE INDEX JENA_IXURI ON JENA_LONG_URI(Head(${c}),Hash);;
CREATE UNIQUE INDEX JENA_IXBND ON JENA_PREFIX(Head(${c}),Hash);;
CREATE INDEX JENA_IXSP ON JENA_SYS_STMT(Subj(${c}), Prop(${c}));;
CREATE INDEX JENA_IXO ON JENA_SYS_STMT(Obj(${c}));;

#-------------------------------------------------------------------
# Create a blank statement table - and indexes
#
# Parameters:
# a - table name
# b - column type for subj, prop, obj (see param a in InitDBtables)
# c - table implementation type (see param b in InitDBtables)
# d - index key length (see param c in InitDBtables)
#
createStatementTable
CREATE TABLE ${a} (
 Subj       ${b} NOT NULL,
 Prop       ${b} NOT NULL,
 Obj        ${b} NOT NULL,
 GraphID    INTEGER
) TYPE = ${c};;
CREATE INDEX ${a}_IXSP ON ${a}(Subj(${d}), Prop(${d}));;
CREATE INDEX ${a}_IXO ON ${a}(Obj(${d}));;

#-------------------------------------------------------------------
# Create a blank reified statement table - and indexes
#
# Parameters:
# a - table name
# b - column type for subj, prop, obj (see param a in InitDBtables)
# c - table implementation type (see param b in InitDBtables)
# d - index key length (see param c in InitDBtables)
createReifStatementTable
CREATE TABLE ${a} (
 Subj       ${b},
 Prop       ${b},
 Obj        ${b},
 GraphID    INTEGER,
 Stmt       ${b} NOT NULL,
 HasType    CHAR(1) NOT NULL
) TYPE  = ${c};;
CREATE UNIQUE INDEX ${a}_IXSTMT ON ${a}(Stmt(${d}), HasType);;
CREATE INDEX ${a}_IXSP ON ${a}(Subj(${d}), Prop(${d}));;
CREATE INDEX ${a}_IXO ON ${a}(Obj(${d}));;
 
#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed
initDBgenerators
# Generators to index the main tables

#-------------------------------------------------------------------
# Delete all rows from named AST table
dropTable
DROP TABLE ${a};;

#-------------------------------------------------------------------
# Remove all rows from given table with the given GraphID.
# Substitutes table name 
removeRowsFromTable
DELETE FROM ${a} WHERE (GraphID = ?)

#-------------------------------------------------------------------
# Store the name of a new graph and create a unique identifier for it.
insertGraph
INSERT INTO JENA_GRAPH (Name) VALUES (?)

#-------------------------------------------------------------------
# Delete a triple
# substituting Statement table name 
# and taking values as arguments
deleteStatement
Delete FROM ${a} WHERE (Subj = ? AND Prop = ? AND Obj = ? AND GraphID = ?)

#-------------------------------------------------------------------
# Insert a triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertStatement
INSERT INTO ${a} (Subj, Prop, Obj, GraphID) VALUES (?, ?, ?, ?)

#-------------------------------------------------------------------
# Return the count of rows in the table 
getRowCount
SELECT COUNT(*) FROM ${a}

#-------------------------------------------------------------------
# Insert a long object
insertLongObject
INSERT INTO ${a} (Head, Hash, Tail) VALUES (?, ?, ?)

#-------------------------------------------------------------------
# Get the ID of the object that was just inserted
getLastInsertID
Select Last_Insert_ID()

#-------------------------------------------------------------------
# Return a long object
getLongObject
SELECT HEAD, TAIL FROM ${a} WHERE ID = ?

#-------------------------------------------------------------------
# Return the ID of a long object, if it exists
getLongObjectID
SELECT ID FROM ${a} WHERE Head = ? and Hash = ?

#-------------------------------------------------------------------
# Return the ID of a long object, if it exists
getLongObjectID
SELECT ID FROM ${a} WHERE Head = ? and Hash = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
SelectStatement
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject 
SelectStatementS
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
SelectStatementSP
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Subj = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property and object
SelectStatementSPO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Subj = ? AND S.Prop = ? AND S.GraphID = ? 

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Object
SelectStatementSO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property and Object
SelectStatementPO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Object
SelectStatementO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property 
SelectStatementP
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Reified Statement (triple store) graph
SelectAllReifStatement
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an reified Statement (triple store) graph
SelectAllReifTypeStmt
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI
SelectReifStatement
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI and that have the HasType property defined
SelectReifTypeStatement
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Delete an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
deleteReifStatement
Delete FROM ${a} WHERE (Subj = ? AND Prop = ? AND Obj = ? AND GraphID = ?
AND Stmt = ?)

#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertReifStatement
INSERT INTO ${a} (Subj, Prop, Obj, GraphID, Stmt, HasType) VALUES (?, ?, ?, ?, ?, ?)

#-------------------------------------------------------------------
# Update the subject of a reified statement 
updateReifSubj
UPDATE ${a} SET Subj=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the property of a reified statement 
updateReifProp
UPDATE ${a} SET Prop=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the object of a reified statement 
updateReifObj
UPDATE ${a} SET Obj=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the hasType of a reified statement 
updateReifHasType
UPDATE ${a} SET HasType=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Find the reified statements with the given subject 
findFragSubj
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given property 
findFragProp
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given object resource
findFragObj
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND S.Obj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given hasType 
findFragHasType
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType 
FROM ${a} S WHERE S.Stmt = ? AND S.HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (resource) 
SelectReifURISPO
SELECT S.Stmt
FROM ${a} S WHERE S.Subj = ? AND S.Prop = ? and S.Obj = ? AND S.GraphID = ? AND S.HasType = "T"

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (reference) 
SelectReifURI
SELECT S.Stmt
FROM ${a} S WHERE S.GraphID = ? AND S.HasType = "T"

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# that partially reify something 
SelectAllReifNodes
SELECT DISTINCT S.Stmt
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Determine if the statement URI's partially reifies anything in a Reified
# Statement (triple store) graph
SelectReifNode
SELECT DISTINCT S.Stmt
FROM ${a} S WHERE S.Stmt = ? AND S.GraphID = ?


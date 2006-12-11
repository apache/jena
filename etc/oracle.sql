# Generic SQL driver file for Oracle suitable for multi-model formats
#
# (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
#
#-------------------------------------------------------------------
# Initialize a blank database - create system tables and indexes
#
# Parameters:
# a - column type for subj, prop, obj
#     VARCHAR(nn) BINARY if DBHasXact is true,
#        where nn is DBLongObjectLen (requires that nn <= 250)
#     else TINYBLOB if DBLongObjectLen <= 250; else MEDIUMBLOB
# b - column type for head
# c - table and index name prefix, defaults to jena_
#
# Note that the tables JENA_LONG_LIT, JENA_LONG_URI, JENA_PREFIX
# all have the same structure. These are used to store long objects.
# Tthere chould (should) be a separate operation to create
# these tables to ensure they have an identical structure.
# This is not urgent - left for future work.
#
initDBtables
CREATE TABLE ${c}sys_stmt (
 Subj       ${a} NOT NULL,
 Prop       ${a} NOT NULL,
 Obj        ${a} NOT NULL,
 GraphID    INTEGER
) ;;
CREATE TABLE ${c}long_lit (
 ID      	INTEGER NOT NULL PRIMARY KEY,
 Head    	${b} NOT NULL,
 ChkSum		INTEGER,
 Tail    	BLOB
);;
CREATE SEQUENCE ${c}long_lit_ID_seq;;
CREATE TABLE ${c}long_uri (
 ID      	INTEGER NOT NULL PRIMARY KEY,
 Head    	${b} NOT NULL,
 ChkSum 	INTEGER,
 Tail    	BLOB
) ;;
CREATE SEQUENCE ${c}long_uri_ID_seq;;
CREATE TABLE ${c}prefix (
 ID      	INTEGER NOT NULL PRIMARY KEY,
 Head    	${b} NOT NULL,
 ChkSum		INTEGER,
 Tail    	BLOB
) ;;
CREATE SEQUENCE ${c}prefix_ID_seq;;
CREATE TABLE ${c}graph (
 ID      INTEGER NOT NULL PRIMARY KEY,
 Name    VARCHAR2(4000)
);;
CREATE SEQUENCE ${c}graph_ID_seq;;
CREATE UNIQUE INDEX ${c}xlit ON ${c}long_lit(Head,ChkSum);;
CREATE UNIQUE index ${c}xuri ON ${c}long_urI(Head,ChkSum);;
CREATE UNIQUE INDEX ${c}xbnd ON ${c}prefix(Head,ChkSum);;
CREATE INDEX ${c}xsp ON ${c}sys_stmt(Subj, Prop);;
CREATE INDEX ${c}xo ON ${c}sys_stmt(Obj);;

#-------------------------------------------------------------------
# Create a blank statement table - and indexes
#
# Parameters:
# a - table name
# b - column type for subj, prop, obj (see param a in InitDBtables)
createStatementTable
CREATE TABLE ${a} (
 Subj       ${b} NOT NULL,
 Prop       ${b} NOT NULL,
 Obj        ${b} NOT NULL,
 GraphID    INTEGER
);;
CREATE INDEX ${a}xsp ON ${a}(Subj, Prop);;
CREATE INDEX ${a}xo ON ${a}(Obj);;

#-------------------------------------------------------------------
# Create a blank reified statement table - and indexes
#
# Parameters:
# a - table name
# b - column type for subj, prop, obj (see param a in InitDBtables)
createReifStatementTable
CREATE TABLE ${a} (
 Subj       ${b},
 Prop       ${b},
 Obj        ${b},
 GraphID    INTEGER,
 Stmt       ${b} NOT NULL,
 HasType    CHAR(1) NOT NULL
);;
CREATE UNIQUE INDEX ${a}_IXSTMT ON ${a}(Stmt, HasType);;
CREATE INDEX ${a}XSP ON ${a}(Subj, Prop);;
CREATE INDEX ${a}XO ON ${a}(Obj);;

#-------------------------------------------------------------------
# Lock the database by using the existence of a table as a
# mutex, i.e., the lock is acquired if the table can be created,
# and if the table already exists, the lock is held by another thread.
#
# Parameters:
# a - mutex table name
lockDatabase
CREATE TABLE ${a} (Dummy INTEGER);;

#-------------------------------------------------------------------
# Unlock the database by dropping the mutex table.
#
# Parameters:
# a - mutex table name
unlockDatabase
DROP TABLE ${a};;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed
initDBgenerators
# Generators to index the main tables

#-------------------------------------------------------------------
# Delete all rows from named AST table
dropTable
DROP TABLE ${a}

#-------------------------------------------------------------------
# Remove all rows from given table with the given GraphID.
# Substitutes table name
removeRowsFromTable
DELETE FROM ${a} WHERE (GraphID = ?)

#-------------------------------------------------------------------
# Store the name of a new graph and create a unique identifier for it.
insertGraph
INSERT INTO ${a} (ID, Name) VALUES (?, ?)

#-------------------------------------------------------------------
# Remove the name of a graph.
deleteGraph
Update ${a} SET NAME=null where ID = ?

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
SELECT COUNT(*) FROM ${a} WHERE (GraphID = ?)

#-------------------------------------------------------------------
# Insert a long object with no tail
insertLongObject
INSERT INTO ${a} (ID, Head, ChkSum, Tail) VALUES (?, ?, ?, ?)

#-------------------------------------------------------------------
# Retrieve BLOB
# getEmptyBLOB
# SELECT Tail FROM ${a} WHERE (ID =  ${b}) FOR UPDATE
#
#-------------------------------------------------------------------
# Get the ID of the object that was just inserted
getInsertID
SELECT ${a}_ID_seq.NEXTVAL FROM DUAL

#-------------------------------------------------------------------
# Return a long object
getLongObject
SELECT HEAD, TAIL FROM ${a} WHERE ID = ?

#-------------------------------------------------------------------
# Return the ID of a long object, if it exists, based on the Head
getLongObjectID
SELECT ID FROM ${a} WHERE Head = ? and ChkSum is NULL

#-------------------------------------------------------------------
# Return the ID of a long object, if it exists, based on the Head and ChkSum
getLongObjectIDwithChkSum
SELECT ID FROM ${a} WHERE Head = ? and ChkSum = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
selectStatement
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject
selectStatementS
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property
selectStatementSP
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Subj = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Property and object
selectStatementSPO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Subj = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same subject and Object
selectStatementSO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property and Object
selectStatementPO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Object
selectStatementO
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Obj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the same Property
selectStatementP
SELECT S.Subj, S.Prop, S.Obj
FROM ${a} S WHERE S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Reified Statement (triple store) graph
selectReified
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an reified Statement (triple store) graph
selectReifiedT
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI
selectReifiedN
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.Stmt = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statements in an Asserted Statement (triple store) graph
# with the given statement URI and that have the HasType property defined
selectReifiedNT
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.Stmt = ? AND HasType = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Delete an all-URI triple into a Statement table,
# substituting Statement table name
# and taking URI's as arguments
deleteReified
Delete FROM ${a} WHERE (Subj = ? AND Prop = ? AND Obj = ? AND GraphID = ?
AND Stmt = ? AND HasType = ?)

#-------------------------------------------------------------------
# Delete a fragment of a reified statement in the reified statements table
# that has only a subject.
deleteReifiedS
Delete FROM ${a} WHERE (Subj = ? AND Prop is null AND Obj is null AND GraphID = ?
AND Stmt = ? AND HasType is null)

#-------------------------------------------------------------------
# Delete a fragment of a reified statement in the reified statements table
# that has only a predicate.
deleteReifiedP
Delete FROM ${a} WHERE (Subj is null AND Prop = ? AND Obj is null AND GraphID = ?
AND Stmt = ? AND HasType is null)

#-------------------------------------------------------------------
# Delete a fragment of a reified statement in the reified statements table
# that has only an object.
deleteReifiedO
Delete FROM ${a} WHERE (Subj is null AND Prop is null AND Obj = ? AND GraphID = ?
AND Stmt = ? AND HasType is null)

#-------------------------------------------------------------------
# Delete a fragment of a reified statement in the reified statements table
# that has only a type.
deleteReifiedT
Delete FROM ${a} WHERE (Subj is null AND Prop is null AND Obj is null AND GraphID = ?
AND Stmt = ? AND HasType = 'T')

#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table,
# substituting Statement table name
# and taking URI's as arguments
insertReified
INSERT INTO ${a} (Subj, Prop, Obj, GraphID, Stmt, HasType) VALUES (?, ?, ?, ?, ?, ?)

#-------------------------------------------------------------------
# Update the subject of a reified statement
updateReifiedS
UPDATE ${a} SET Subj=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the property of a reified statement
updateReifiedP
UPDATE ${a} SET Prop=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the object of a reified statement
updateReifiedO
UPDATE ${a} SET Obj=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Update the hasType of a reified statement
updateReifiedT
UPDATE ${a} SET HasType=? WHERE Stmt = ? AND GraphID = ?

#-------------------------------------------------------------------
# Find the reified statements with the given subject
selectReifiedNS
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.Stmt = ? AND S.Subj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given property
selectReifiedNP
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.Stmt = ? AND S.Prop = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Find the reified statement with the given object resource
selectReifiedNO
SELECT S.Subj, S.Prop, S.Obj, S.Stmt, S.HasType
FROM ${a} S WHERE S.Stmt = ? AND S.Obj = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (resource)
selectReifNodeSPOT
SELECT S.Stmt
FROM ${a} S WHERE S.Subj = ? AND S.Prop = ? and S.Obj = ? AND S.GraphID = ? AND S.HasType = 'T'

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# with the specified subject, property and literal (reference)
selectReifNodeT
SELECT S.Stmt
FROM ${a} S WHERE S.GraphID = ? AND S.HasType = 'T'

#-------------------------------------------------------------------
# Select all the statement URI's in a Reified Statement (triple store) graph
# that partially reify something
selectReifNode
SELECT DISTINCT S.Stmt
FROM ${a} S WHERE S.GraphID = ?

#-------------------------------------------------------------------
# Determine if the statement URI's partially reifies anything in a Reified
# Statement (triple store) graph
selectReifNodeN
SELECT DISTINCT S.Stmt
FROM ${a} S WHERE S.Stmt = ? AND S.GraphID = ?

#-------------------------------------------------------------------
# Select JENA sequence name from sequences
SelectJenaSequences
SELECT SEQUENCE_NAME FROM SEQ WHERE SEQUENCE_NAME LIKE ('${a}%')

#-------------------------------------------------------------------
# Select sequence name from sequences
DropSequence
DROP SEQUENCE ${a}

#-------------------------------------------------------------------
# Select sequence name from sequences
SelectSequenceName
SELECT SEQUENCE_NAME FROM SEQ WHERE SEQUENCE_NAME = ?

#-------------------------------------------------------------------
# Insert a long object with an EMPTY_BLOB for a tail tail
insertLongObjectEmptyTail
INSERT INTO ${a} (ID, Head, ChkSum, Tail) VALUES (?, ?, ?, EMPTY_BLOB())


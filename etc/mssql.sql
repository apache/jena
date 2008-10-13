# Generic SQL driver file for Microsoft SQL Server 2000(tm), suitable for multi-model formats
# (c) Copyright 2005, Hewlett-Packard Development Company, LP
#
# This file inherits from the postgresql.sql file and just includes the 
# changed operations. The changes are:
#  o use IDENTITY sequences for ID allocation
#  o use nvarchar/ntext for Unicode compatibility
# This design was adopted from an earlier SQL Driver by Erik Barke (eba@ur.se)
#-------------------------------------------------------------------
# Initialize a blank database - create system tables and indexes
#
# Parameters:
# a - column type for subj, prop, obj
#     NVARCHAR(nn)
# b - column type for head of long objects
#     NVARCHAR(nn)
# c - table and index name prefix
#
# Note that the tables JENA_LONG_LIT, JENA_LONG_URI, JENA_PREFIX
# all have the same structure. These are used to store long objects.
# There could (should) be a separate operation to create
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
 ID      	INT NOT NULL PRIMARY KEY IDENTITY(1,1),
 Head    	${b} NOT NULL,
 ChkSum		BIGINT,
 Tail    	NTEXT
) ;;
CREATE TABLE ${c}long_uri (
 ID      	INT NOT NULL PRIMARY KEY IDENTITY(1,1),
 Head    	${b} NOT NULL,
 ChkSum 	BIGINT,
 Tail    	NTEXT
) ;;
CREATE TABLE ${c}prefix (
 ID      	INT NOT NULL PRIMARY KEY IDENTITY(1,1),
 Head    	${b} NOT NULL,
 ChkSum		BIGINT,
 Tail    	NTEXT
) ;;
CREATE TABLE ${c}graph (
 ID		INT NOT NULL PRIMARY KEY IDENTITY(1,1),
 Name		NVARCHAR(1024)
) ;;
CREATE UNIQUE INDEX ${c}IXLIT ON ${c}long_lit(Head,ChkSum);;
CREATE UNIQUE INDEX ${c}IXURI ON ${c}long_uri(Head,ChkSum);;
CREATE UNIQUE INDEX ${c}IXBND ON ${c}prefix(Head,ChkSum);;
CREATE INDEX ${c}IXSP ON ${c}sys_stmt(Subj, Prop);;
CREATE INDEX ${c}IXO ON ${c}sys_stmt(Obj);;

#-------------------------------------------------------------------
# Change the lock level for a table to suppress pagelocks, not used
setLockLevel
SP_INDEXOPTION ${a}, 'AllowPageLocks', FALSE ;;
SP_INDEXOPTION ${a}, 'AllowRowLocks', TRUE ;;
 
#-------------------------------------------------------------------
# Store the name of a new graph and get the unique identifier for it.
# Note: The jdbc driver must support batch queries.
insertGraph
INSERT INTO ${a} (Name) VALUES (?)
SELECT @@IDENTITY

#-------------------------------------------------------------------
# Insert a long object and get the unique identifier for it.
# Note: The jdbc driver must support batch queries.
insertLongObject
INSERT INTO ${a} (Head, ChkSum, Tail) VALUES (?, ?, ?)
SELECT @@IDENTITY

#-------------------------------------------------------------------
# Get last inserted id (probably not used)
getInsertID
SELECT IDENT_CURRENT('${a}')

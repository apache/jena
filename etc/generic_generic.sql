# Generic SQL driver file for Oracle suitable for multi-model formats
#
#-------------------------------------------------------------------
# Initialize a blank database - create tables and indexes - compound statement group
initDBtables
CREATE TABLE JENA_SYS_STMT (
 Subj       VARCHAR(250) NOT NULL,
 Prop       VARCHAR(250) NOT NULL,
 Obj        VARCHAR(250),
 GraphID       VARCHAR(250),
 CONSTRAINT uniq_subj_prop_obj
 UNIQUE (Subj, Prop, Obj, GraphID)
);;
CREATE TABLE JENA_LITERAL (
 LitId     VARCHAR(250) NOT NULL PRIMARY KEY,
 LiteralIdx     VARCHAR(1000) NOT NULL UNIQUE,
 AsBLOB          BLOB,
);;
CREATE INDEX JENA_IDX_SUBJ_PROP ON JENA_StmtAsserted(Subj, Prop);;
CREATE INDEX JENA_IDX_OBJ ON JENA_StmtAsserted(Obj);;
CREATE INDEX JENA_IDX_SUBJ_PROP ON JENA_SystemStmtAsserted(Subj, Prop);;
CREATE INDEX JENA_IDX_OBJ ON JENA_SystemStmtAsserted(Obj);;
CREATE INDEX JENA_IDX_LITERALS ON JENA_LITERAL(LITERALIDX);;

#-------------------------------------------------------------------
# Create a blank statement table - and indexes - compound statement group
createStatementTable
CREATE TABLE ${a} (
 Subj       VARCHAR(250) NOT NULL,
 Prop       VARCHAR(250) NOT NULL,
 Obj        VARCHAR(250),
 GraphID       VARCHAR(250),
 CONSTRAINT uniq_subj_prop_obj
 UNIQUE (Subj, Prop, Obj, GraphID)
);;
CREATE INDEX JENA_IDX_SUBJ_PROP ON ${a}(Subj, Prop);;
CREATE INDEX JENA_IDX_OBJ ON ${a}(Obj);;

#-------------------------------------------------------------------
# Initialize a blank database - create any generators needed - compound statement group
initDBgenerators
# Generators to index the main tables

#-------------------------------------------------------------------
# Allocate an id for a literal
# Interbase doesn't obey the select syntax the needs a non-empty table in the from field
allocateLiteralID

#-------------------------------------------------------------------
# Allocate an id for a GRAPH
# Interbase doesn' obey the select syntax the needs a non-empty table in the from field
allocateGraphID

#-------------------------------------------------------------------
# Insert an all-URI triple into a Statement table, 
# substituting Statement table name 
# and taking URI's as arguments
insertStatement
INSERT INTO ${a} (Subj, Prop, Obj, GraphID) VALUES (?, ?, ?, ?)

#-------------------------------------------------------------------
# Return the count of rows in the table 
getRowCount
SELECT COUNT(*) FROM ${a}

#-------------------------------------------------------------------
# Return the ID of a literal string, if it exists
getLiteralID
SELECT LITID FROM JENA_LITERAL WHERE LITERALIDX = ?

#-------------------------------------------------------------------
# Drop all RDF generators from a database
cleanDBgenerators
DROP SEQUENCE JENA_RESOURCES_GEN;;
DROP SEQUENCE JENA_NAMESPACE_GEN;;
DROP SEQUENCE JENA_LITERALS_GEN;;
DROP SEQUENCE JENA_MODELS_GEN;;


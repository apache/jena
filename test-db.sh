#!/bin/bash

# You wil need to edit this script to fix:
# - User/password for the databases you wish to test
# - Location of JDBC driver files (via environment variable JDBC)

if [ "$OSTYPE" == "cygwin" ]
then 
    export S=";"
else 
    export S=":"
fi

case $1 in

hsqldb|hsql)
echo HSQLDB
DEFS="-Djena.db.url=jdbc:hsqldb:file:jenatest"
## DEFS="-Djena.db.url=jdbc:hsqldb:hsql://localhost/jenatest"
## DEFS="-Djena.db.url=jdbc:hsqldb:http://localhost:88/jenatest
DEFS=" $DEFS  -Djena.db.user=sa"
DEFS=" $DEFS  -Djena.db.password="
DEFS=" $DEFS  -Djena.db.type=HSQL"
DEFS=" $DEFS  -Djena.db.driver=org.hsqldb.jdbcDriver"
## HSQL does not support full transaction isolation.
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC=${JDBC:-$HOME/jlib/hsqldb.jar}
;;

hsqldb-mem|hsql-mem)
echo "HSQLDB (in-memory)"
DEFS="-Djena.db.url=jdbc:hsqldb:mem:jenatest"
DEFS=" $DEFS  -Djena.db.user=sa"
DEFS=" $DEFS  -Djena.db.password="
DEFS=" $DEFS  -Djena.db.type=HSQL"
DEFS=" $DEFS  -Djena.db.driver=org.hsqldb.jdbcDriver"
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC=${JDBC:-$HOME/jlib/hsqldb.jar}
;;

derby)
echo "Apache Derby (embedded)"
DEFS="-Djena.db.url=jdbc:derby:tmp/jenatest;create=true"
## DEFS="-Djena.db.url=jdbc:hsqldb:hsql://localhost/jenatest"
## DEFS="-Djena.db.url=jdbc:hsqldb:http://localhost:88/jenatest
DEFS=" $DEFS  -Djena.db.user="
DEFS=" $DEFS  -Djena.db.password="
DEFS=" $DEFS  -Djena.db.type=Derby"
DEFS=" $DEFS  -Djena.db.driver=org.apache.derby.jdbc.EmbeddedDriver"
# No row level locking
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC="${JDBC:-$HOME/jlib/db-derby-10.1.2.1-bin/lib/derby.jar}"
;;

postgres|postgresql) 
echo PostgreSQL
DEFS="-Djena.db.url=jdbc:postgresql://localhost/jenatest"
DEFS=" $DEFS  -Djena.db.user=test"
DEFS=" $DEFS  -Djena.db.password=password"
DEFS=" $DEFS  -Djena.db.type=PostgreSQL"
DEFS=" $DEFS  -Djena.db.driver=org.postgresql.Driver"
JDBC="${JDBC:-$HOME/jlib/hsqldb.jar}"
;;

mysql) 
echo MySQL
DEFS="-Djena.db.url=jdbc:mysql://localhost/jenatest"
DEFS=" $DEFS  -Djena.db.user=user"
DEFS=" $DEFS  -Djena.db.password=password"
DEFS=" $DEFS  -Djena.db.type=MySQL"
DEFS=" $DEFS  -Djena.db.driver=com.mysql.jdbc.Driver" 
JDBC="${JDBC:-$HOME/jlib/mysql-connector-java-3.1.12-bin.jar}"
;;

# MS SQL Server, jTDS driver, local
mssqlTdsLocal)
echo "MS SQL Server with TDS driver"
DEFS="-Djena.db.url=jdbc:jtds:sqlserver://localhost/jenatest"
DEFS=" $DEFS  -Djena.db.user=user"
DEFS=" $DEFS  -Djena.db.password=password"
DEFS=" $DEFS  -Djena.db.type=MsSQL"
DEFS=" $DEFS  -Djena.db.driver=net.sourceforge.jtds.jdbc.Driver"
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC="${JDBC:-}"
;;

# MS SQL Server, Microsoft driver, full SQL Server
# (in Palo Alto, beware speed!)
mssqlMsFull)
echo "MS SQL Server / Microsoft driver"
DEFS="-Djena.db.url=jdbc:sqlserver://dbase-pa2.labs.hpl.hp.com;databaseName=JenaTest"
DEFS=" $DEFS  -Djena.db.user=jenatest"
DEFS=" $DEFS  -Djena.db.password=6aXjen%4"
DEFS=" $DEFS  -Djena.db.type=MsSQL"
DEFS=" $DEFS  -Djena.db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver"
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC="${JDBC:-$HOME/jlib/sqljdbc.jar}"
;;

# MS SQL Server, Microsoft driver, local
mssqlMsLocal)
echo "MS SQL Server / Microsoft driver / Local"
DEFS="-Djena.db.url=jdbc:sqlserver://localhost;databaseName=Test"
DEFS=" $DEFS  -Djena.db.user=user"
DEFS=" $DEFS  -Djena.db.password=password"
DEFS=" $DEFS  -Djena.db.type=MsSQL"
DEFS=" $DEFS  -Djena.db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver"
DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC="${JDBC:-$HOME/jlib/sqljdbc.jar}"
;;

mssqle)
echo "MS SQL Server express"
DEFS="-Djena.db.url=jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=jenatest"
DEFS=" $DEFS  -Djena.db.user=user"
DEFS=" $DEFS  -Djena.db.password=password"
DEFS=" $DEFS  -Djena.db.type=MsSQL"
DEFS=" $DEFS  -Djena.db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver"
## DEFS=" $DEFS  -Djena.db.concurrent=false"
JDBC="${JDBC:-$HOME/jlib/sqljdbc.jar}"
;;

*) echo "you must specify a database type [postgres, mysql, mssql, mssqle, hsqldb, derby]"; exit ;;
esac

if [ "$JDBC" = "" ]
then
    echo "No JDBC driver " 1>&2 
    exit 1
    fi

# Assumes jena.jar is built
export CP="$JDBC"
for jar in lib/*jar; do export CP="$CP$S$jar" ; done

java $DEFS -classpath $CP junit.textui.TestRunner \
    com.hp.hpl.jena.db.test.TestPackage

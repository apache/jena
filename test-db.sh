#!/bin/bash


# run the DB tests. This is a HACK to get started. The test for "a server is available"
# is too weak and in any case we want a version that will work for any supported
# server. Still, have to begin somehow ...


# Wrapper suppressed because (a) only relevant for MySql and (b) fails on cygwin - der
#if mysql --user=test < /dev/null ; then

    if [ "$OSTYPE" == "cygwin" ]; then export S=";"; else export S=":"; fi

    case $1 in
        postgres) DEFS="-Djena.db.url=jdbc:postgresql://localhost/test -Djena.db.user=test -Djena.db.password= -Djena.db.type=PostgreSQL -Djena.db.driver=org.postgresql.Driver" ;; 
        mysql) export DEFS="-Djena.db.url=jdbc:mysql://localhost/test -Djena.db.user=test@localhost -Djena.db.password=test -Djena.db.type=MySQL -Djena.db.driver=com.mysql.jdbc.Driver" ;;
        # SQL Server, jTDS driver, local MSDE installation
        mssqlTdsLocal) export DEFS="-Djena.db.url=jdbc:jtds:sqlserver://localhost/Test -Djena.db.user=test -Djena.db.password=foo -Djena.db.type=MsSQL -Djena.db.driver=net.sourceforge.jtds.jdbc.Driver -Djena.db.concurrent=false" ;;
        # SQL Server, Microsoft driver, full SQL Server (in Palo Alto, beware speed!)
        mssqlMsFull)  export DEFS="-Djena.db.url=jdbc:sqlserver://dbase-pa2.labs.hpl.hp.com;databaseName=JenaTest -Djena.db.user=jenatest -Djena.db.password=6aXjen%4 -Djena.db.type=MsSQL -Djena.db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver -Djena.db.concurrent=false" ;;
        # SQL Server, Microsoft driver, local MSDE installation
        mssqlMsLocal) export DEFS="-Djena.db.url=jdbc:sqlserver://localhost;databaseName=Test -Djena.db.user=test -Djena.db.password=foo -Djena.db.type=MsSQL -Djena.db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver -Djena.db.concurrent=false" ;;
	*) echo "you must specify a database type [postgres or mysql]"; return ;;
    esac

    export CP=""
    for jar in lib/*jar; do export CP=$CP$S$jar; done

    java $DEFS -classpath $CP junit.textui.TestRunner com.hp.hpl.jena.db.test.TestPackage

#else
#    echo mysql does not seem to be installed.
#fi

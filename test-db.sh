#!/bin/bash


# run the DB tests. This is a HACK to get started. The test for "a server is available"
# is too weak and in any case we want a version that will work for any supported
# server. Still, have to begin somehow ...


if mysql --user=test < /dev/null ; then
    if [ "$OSTYPE" == "cygwin" ]; then export S=";"; else export S=":"; fi

    case $1 in
        postgres) DEFS="-Djena.db.url=jdbc:postgresql://localhost/test -Djena.db.user=test -Djena.db.password= -Djena.db.type=PostgreSQL -Djena.db.driver=org.postgresql.Driver" ;; 
        mysql) export DEFS="-Djena.db.url=jdbc:mysql://localhost/test -Djena.db.user=test -Djena.db.password= -Djena.db.type=MySQL -Djena.db.driver=com.mysql.jdbc.Driver" ;;
	*) echo "you must specify a database type [postgres or mysql]"; return ;;
    esac

    export CP=""
    for jar in lib/*jar; do export CP=$CP$S$jar; done

    java $DEFS -classpath $CP junit.textui.TestRunner com.hp.hpl.jena.db.test.TestPackage

else
    echo mysql does not seem to be installed.
fi

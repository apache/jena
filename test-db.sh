#!/bin/bash


# run the DB tests. This is a HACK to get started. The test for "a server is available"
# is too weak and in any case we want a version that will work for any supported
# server. Still, have to begin somehow ...


if mysql --user=test < /dev/null ; then
    if [ "$OSTYPE" == "cygwin" ]; then export S=";"; else export S=":"; fi

    export CP=""
    for jar in lib/*jar; do export CP=$CP$S$jar; done

    java -classpath $CP junit.textui.TestRunner com.hp.hpl.jena.db.test.TestPackage

else
    echo mysql does not seem to be installed.
fi

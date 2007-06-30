#!/bin/bash


S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi

err=0
if [ "$SDBROOT" == "" ]
then
    echo "SDBROOT not set" 2>&1 
    err=1
    fi
if [ "$SDB_JDBC" == "" ]
then
    echo "SDB_JDBC not set" 2>&1 
    err=1
    fi
if [ "$SDB_USER" == "" ]
then
    echo "SDB_USER not set" 2>&1 
    err=1
    fi
if [ "$SDB_PASSWORD" == "" ]
then
    echo "SDB_PASSWORD not set" 2>&1 
    err=1
    fi

if [ "$err" == 1 ]
then
    echo "Incomplete setup" 2>&1
    exit 1
    fi


CP="$SDB_JDBC"
for jar in lib/*
do
  [ -e "$jar" ] || echo "No such jar: $jar" 1>&2

  if [ "$CP" == "" ]
  then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
      fi
  done

( cd $SDBROOT
    STORE="sdb.ttl"
    if [ ! -e "$STORE" ]
    then
	echo "Store description '$STORE' not found" 2>&1
	exit 2
	fi

    java -cp $CP sdb.sdbtest -sdb sdb.ttl testing/manifest-sdb.ttl
)

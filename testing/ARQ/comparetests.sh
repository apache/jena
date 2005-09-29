#!/bin/bash

if [ $# != 1 ]
then
    echo "Usage: $0 <dir>" 1>&2
    exit 1
    fi

T=$1

for f in  */*.rq
do
  if [ -e "$T/$f" ]
      then
      # echo "$T/$f"
      if ! cmp -s "$T/$f" "$f"
	  then
	  #echo cp "$f" "$T/$f"
	  echo "$f"
      fi
  fi
done
  
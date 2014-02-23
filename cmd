#!/bin/bash

ds_active() {
    local DS="$1"
    curl -XPOST 'http://localhost:3030/$/datasets/'"${DS}?state=active"
}

ds_offline() {
    local DS="$1"
    curl -XPOST 'http://localhost:3030/$/datasets/'"${DS}?state=offline"
}

sys_datasets() {
    curl --data-binary @config2.ttl --header 'Content-type: text/turtle' \
	'http://localhost:3030/$/datasets'
}

ds_backup() {
    local DS="$1"
    curl -XPOST 'http://localhost:3030/$/backup/'"${DS}"
}

ds_delete() {
     local DS="$1"
     curl -XDELETE 'http://localhost:3030/$/datasets/'"${DS}"
}

ds_query() {
    local DS="$1"
    # curl does not automaticall %-encode
    wget -S -O- -q 'http://localhost:3030/'"${DS}"'NEW/query?query=ASK{}'
}

ds_sleep() {
    local DS="$1"
    curl -XPOST 'http://localhost:3030/$/sleep/'"${DS}"
}

ds-tasks() {
    case "$#" in
	0) 
	    curl 'http://localhost:3030/$/tasks' ;;
	1)
	    curl "http://localhost:3030/$/tasks/$1" ;;
	*)
	    echo "Wrong number of arguments" 1>&2 ;;
    esac
}


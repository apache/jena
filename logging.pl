#!/bin/perl
# find src -name \*java | xargs -n 1 perl -i.bak SCRIPT

undef $/ ;
$_ = <> ;
s/import org.apache.commons.logging.Log\s*;/import org.slf4j.Logger;/ ;
s/import org.apache.commons.logging.LogFactory\s*;/import org.slf4j.LoggerFactory;/ ;

s/ Log / Logger / ;
s/LogFactory.getLog/LoggerFactory.getLogger/ ;

# .fatal => .error

print $_ ;

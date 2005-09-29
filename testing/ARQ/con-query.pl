# Deduce absolute URIs
s!WITH\s*\<.*/([^/]*)\>!WITH \<$1\>! ;

# remove braces
s/{/ /;
s/} *// ;

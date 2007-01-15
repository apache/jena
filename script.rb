
require 'java'
# require 'sdb.rb'
#include_class('java.lang.String') {|p,name| "J#{name}" }

include_class 'java.lang.System'

# rename as "Factory"
#include_class('com.hp.hpl.jena.sdb.SDBFactory') {|p,name| name.sub(/^SDB/,'') }
include_class 'com.hp.hpl.jena.sdb.SDBFactory'

include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
#include_class 'com.hp.hpl.jena.util.FileManager'

include_class 'com.hp.hpl.jena.query.QueryFactory'
include_class 'com.hp.hpl.jena.query.QueryExecutionFactory'
include_class 'com.hp.hpl.jena.query.ResultSetFormatter'
include_class 'arq.cmd.ResultsFormat'
include_class 'arq.cmd.QueryCmdUtils'

# Higher level operations like the command line tools
# Command line => environment and actions?
# Then same here.

dataset = SDBFactory.connectDataset("sdb.ttl")

## puts "Connection : #{store.getConnection().getLabel()}"
## tables = store.getConfiguration().tables()
## tables.each { |x| puts x }

## model = dataset.getDefaultModel()
## model.write(System.out, "N3") 

# 
query = QueryFactory.create("SELECT * { ?s ?p ?o}")
qExec = QueryExecutionFactory.create(query, dataset)

QueryCmdUtils.executeQuery(query, qExec, nil)

## rs = qExec.execSelect()
## ResultSetFormatter.out(rs)
## qExec.close()


exit(0)

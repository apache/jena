require 'java'
# require 'sdb.rb'
#include_class('java.lang.String') {|p,name| "J#{name}" }

include_class 'java.lang.System'

include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
include_class 'com.hp.hpl.jena.util.FileManager'

include_class 'com.hp.hpl.jena.query.QueryFactory'
include_class 'com.hp.hpl.jena.query.QueryExecutionFactory'
include_class 'com.hp.hpl.jena.query.ResultSetFormatter'
include_class 'arq.cmd.ResultsFormat'
include_class 'arq.cmd.QueryCmdUtils'


include_class 'com.hp.hpl.jena.sdb.store.StoreFormatter'
include_class 'com.hp.hpl.jena.sdb.SDBFactory'
include_class 'com.hp.hpl.jena.sdb.sql.TableDump'

class SDB

  attr_reader :store, :dataset, :connection
  
  # factory operations
  def SDB.create(store)
    return SDB.new(store)
  end
  
  def SDB.fromDesc(descFile)
    return SDB.new(SDBFactory.connectStore(descFile))
  end
  
  def initialize(store)
    @store = store
    @connection = store.getConnection()
    @dataset = SDBFactory.connectDataset(store)
  end
  
  def format
    f = store.getTableFormatter() ; 
    f.format() ;
  end
  
  def load(file)
    model = SDBFactory.connectModel(store)
    FileManager.get().readModel(model, file)
  end
  
  
  # Execxute and print
  def query_print(queryString)
    query = QueryFactory.create(queryString)
    qExec = QueryExecutionFactory.create(query, @dataset)
    QueryCmdUtils.executeQuery(query, qExec, nil)
  end
  
  def tables
    return @connection.getTableNames()
  end
end


sdb = SDB.fromDesc("sdb.ttl")
#sdb.format
#sdb.load("D.ttl")
sdb.query_print("SELECT * { ?s ?p ?o}")

# sdb.tables.each { |name| puts name }

# Higher level operations like the command line tools
# Command line => environment and actions?
# Then same here.
#   Or a SDB sript library.

### --------
#store = SDBFactory.connectStore("sdb.ttl")
#puts "Connection : #{store.getConnection().getLabel()}"
#tables = store.getConfiguration().tables()
#tables.each { |x| puts x }
### --------
#
### --------
#
#dataset = SDBFactory.connectDataset("sdb.ttl")
### model = dataset.getDefaultModel()
### model.write(System.out, "N3") 
#
### --------
#query = QueryFactory.create("SELECT * { ?s ?p ?o}")
#qExec = QueryExecutionFactory.create(query, dataset)
#
#QueryCmdUtils.executeQuery(query, qExec, nil)
#
### rs = qExec.execSelect()
### ResultSetFormatter.out(rs)
### qExec.close()
#
#
#exit(0)

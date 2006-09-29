
require 'java'

#include_class('java.lang.String') {|p,name| "J#{name}" }

include_class 'com.hp.hpl.jena.sdb.store.Store'
include_class 'com.hp.hpl.jena.sdb.SDBFactory'
include_class 'com.hp.hpl.jena.sdb.sql.SDBConnectionDesc'
include_class 'com.hp.hpl.jena.sdb.store.StoreDesc'
include_class 'com.hp.hpl.jena.sdb.store.StoreFactory'
include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
include_class 'com.hp.hpl.jena.util.FileManager'
include_class 'java.lang.System'

# Higher level operations like the command line tools
# Command line => enviroment and actions?
# Then same here.

store = StoreFactory.create("sdb.ttl")
store.getTableFormatter().format()

tables = store.getConfiguration().tables()
tables.each { |x| puts x }
System.exit(0)



#c = SDBConnectionDesc.read("sdb.ttl")
#c.label = "HSQL in-memory TEST"
#
## Contrast with ...
#sd = StoreDesc.new("layout2", "hsqldb")
#
#store = StoreFactory.create(sd, c)

puts "Connection : #{store.getConnection().getLabel()}"
store.getTableFormatter().format()
model = SDBFactory.connectModel(store)
# model = StoreFactory.createModel(store)

FileManager.get().readModel(model,"D.ttl")
model.write(System.out, "N3") 

include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
#config = store.getConfiguration() 
#
#config.reset() 
#
#config.setModel(FileManager.get().loadModel("D.ttl")) 
#x = config.getTags() 
#puts(StrUtils.strjoinNL(x)) 
#      
#config.getModel().write(System.out, "N3") 
System.exit(0)

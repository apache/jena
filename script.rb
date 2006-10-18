
require 'java'
# require 'sdb.rb'

#include_class('java.lang.String') {|p,name| "J#{name}" }

include_class 'java.lang.System'
# rename as "Factory"
#include_class('com.hp.hpl.jena.sdb.SDBFactory') {|p,name| name.sub(/^SDB/,'') }
include_class 'com.hp.hpl.jena.sdb.SDBFactory'

include_class 'com.hp.hpl.jena.sdb.util.StrUtils'
include_class 'com.hp.hpl.jena.util.FileManager'

# Higher level operations like the command line tools
# Command line => environment and actions?
# Then same here.

store = SDBFactory.connectStore("sdb.ttl")
## store.setFeature(Feature.)
store.getTableFormatter().format()
## store.getConfiguration().reset() 
## puts "Connection : #{store.getConnection().getLabel()}"
## tables = store.getConfiguration().tables()
## tables.each { |x| puts x }

model = SDBFactory.connectModel(store)
model.read("file:D.ttl", "N3")
model.write(System.out, "N3") 
store.close() ;
exit(0)


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

#config = store.getConfiguration() 
#
#config.reset() 
#
#config.setModel(FileManager.get().loadModel("D.ttl")) 
#x = config.getTags()
#x.each { |x| puts x } 

include_class 'java.lang.System'
#config.getModel().write(System.out, "N3") 
exit(0)

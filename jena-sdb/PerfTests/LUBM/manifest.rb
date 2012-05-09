require '../lib/query_test'

require 'java'

java.lang.System.set_property 'derby.storage.pageCacheSize','3000'

test do
  #store 'sdb-hsqldb-file.ttl'
  #store 'sdb-derby.ttl'
  store 'sdb-hash.ttl'
  store 'sdb-index.ttl'
  query 'Query1.rq'
  #query 'Query2.rq'
  query 'Query3.rq'
  query 'Query4.rq'
  query 'Query5.rq'
  query 'Query6.rq'
  #query 'Query7.rq'
  query 'Query8.rq'
  #query 'Query9.rq'
  query 'Query10.rq'
  query 'Query11.rq'
  query 'Query12.rq'
  query 'Query13.rq'
  query 'Query14.rq'
end

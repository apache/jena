require '../lib/query_test'

test do
  store 'sdb-hsqldb-file.ttl'
  query 'Query1.rq'
  query 'Query2.rq'
  query 'Query3.rq'
  query 'Query4.rq'
end
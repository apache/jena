require '../lib/query_test'

test do
  store 'sdb-hash.ttl'
  store 'sdb-index.ttl'
  query 'Q1.rq'
  query 'Q2.rq'
end

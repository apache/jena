require '../lib/query_test'

test do
  store 'sdb-index.ttl'
  store 'sdb-hash.ttl'
  query 'Q1.rq'
  query 'Q2.rq'
  query 'Q3.rq'
  query 'Q4a.rq'
  query 'Q4.rq'
end

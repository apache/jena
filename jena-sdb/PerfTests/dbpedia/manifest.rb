require '../lib/query_test'

test do
  store 'sdb-hash.ttl'
  store 'sdb-index.ttl'
  query 'Q1.rq'
  query 'Q2.rq'
  query 'Q3.rq'
  query 'Q4.rq'
  query 'Q5.rq'
  query 'Q6.rq'
  query 'Q7.rq'
end

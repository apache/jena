require '../lib/query_test.rb'

test do
  store 'sdb-index.ttl'
  store 'sdb-hash.ttl'
  query 'ex1a.rq'
  query 'ex2a.rq'
  #query 'ex3a.rq'
  query 'ex4a.rq'
  query 'ex5a.rq'
  query 'ex6a.rq'
  query 'ex7a.rq'
  query 'ex8.rq'
  query 'ex9a.rq'
end

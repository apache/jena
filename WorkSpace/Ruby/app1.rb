require 'arq'

## Test

## Some data
model = FileManager.get().loadModel("D.ttl") 
qs = <<-'END'
  SELECT * 
  WHERE
  { ?s ?p ?o }
  END


## ## Query and output
## begin
##   q = Query.new(qs,model)
##   q.select.each {|row| puts row.s}
## ensure
##   q.close
## end

Query.select(qs, model).each{|row| puts "?s=#{row.s} ?p=#{row.p} ?o=#{row.o}" }

## Query and dump
Query.new(qs,model).put_select

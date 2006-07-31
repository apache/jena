require 'java'
require 'jdbc'


f = File.new("Q.sql") ;
sqlStr = f.read ;
#sqlStr = "EXPLAIN \n"+sqlStr 

c = JDBC::DB.connect("jdbc:mysql://localhost/SDB2",
                     "com.mysql.jdbc.Driver",
                     ENV['SDB_USER'],
                     ENV['SDB_PASSWORD'])

rs = c.query(sqlStr)
rs.dump

## 
## 
## cols = rs.cols
## p cols
## results = rs.all
## 
## 
## results.each do
##   |row|
##   row.each do
##     |k,v| 
##     if v 
##       print "/#{v}  /"
##     end
##   end
##   print "\n" 
## end

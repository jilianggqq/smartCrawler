curl -i -X POST -H "Content-Type: application/json" -d '{"url":"http://www.mkyong.com"}' http://localhost:8080/url/search
curl -i -X POST -H "Content-Type: application/json" -d '{"url":"crawlerurls:http://www.mkyong.com/java8/java-8-filter-a-null-value-from-a-stream/"}' http://localhost:8080/url/search
curl -i -X POST -H "Content-Type: application/json" -d '{"url":"http://www.mkyong.com/contact-mkyog/"}' http://localhost:8080/v1/urls/search

curl -i -X POST -H "Content-Type: application/json" -d '{"url":"http://www.mkyong.com/java8/java-8-filter-a-null-value-from-a-stream/"}' http://localhost:8080/v1/urls/search | python -mjson.tool

curl -i -X POST -H "Content-Type: application/json" -d '{"url":"http://www.mkyong.com/", "depth":2}' http://localhost:8080/v1/url

http://www.mkyong.com/contact-mkyong/
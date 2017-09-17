Smart Crawler
=============

This is a small app that will be instructed to crawl to a pre-configured depth from a URL by REST, and
to allow us to make a REST API call to see if a URL has been visited by the crawler, and the response code & timestamp from this visit (if any).

## Environment
  - Redis server : 3.0.6
  - Gradle : 4.1
  - Spring Boot : 1.5.6

## REST Apis
1. **v1/url**

    | URL | _/v1/url_ |
    | --- | --- |
    | Title | Providing a URL and depth n (if not provided, default is 2). Triggering a crawler at the server to do n depth work. |
    | Method | POST |
    | URL Params |  NULL |
    | Data Params | { url :String ,  [depth] :Number} |
    | Response Codes | CREATED (201), Bad Request (400), INTERNAL_SERVER_ERROR (500) |

2. **v1/urls/one**

    | URL | /v1/urls/one |
    | --- | --- |
    | Title | Take a URL and returns the HTTP status code and timestamp it was fetched, or that it wasn't visited |
    | Method | POST |
    | URL Params |  NULL |
    | Data Params | { url : String} |
    | Response Codes | OK (200), Bad Request (400). |
    | Response Contents | {"url" : String, "httpCode" : int(200, 404, 500...), "timestamp" : String (yyyy-MM-dd : hh:mm:ss)} |

3.  **v1/urls/mult**

    | URL | ___/v1/urls/mult___ |
    | --- | --- |
    | Title | Take URLs and returns the HTTP status code and timestamp it was fetched, or that it wasn't visited |
    | Method | POST |
    | URL Params |  NULL |
    | Data Params | [{ url :  String}, { url :  String}, { url :  String} ...] |
    | Response Codes | OK (200), Bad Request (400). |
    | Response Contents | [{"url" : String, "httpCode" : int(200, 404, 500...), "timestamp" : String (yyyy-MM-dd : hh:mm:ss)}, {"url" : String, "httpCode" : int(200, 404, 500...), "timestamp" : String (yyyy-MM-dd : hh:mm:ss)}, ...] |
## Running

1. __Starting Spring boot__.
    ```shell
    ./gradlew build bootRun
    ```

2. __Take a URL and queues it at max depth for processing.__
    ```curl
    curl -i -X POST -w "@curl-format.txt" \
    -H "Content-Type: application/json" \
    -d '{"url":"http://www.mkyong.com/", "depth":2}' \
    http://localhost:8080/v1/url
    ```
    The results is
    ```shell
    HTTP/1.1 201 
    Location: http://localhost:8080/v1/url
    Content-Length: 0
    Date: Sat, 16 Sep 2017 23:26:16 GMT
    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  6.210
                        ----------
             time_total:  6.210
    ```

3. __Take a URL and returns the HTTP status code and timestamp it was fetched, or that it wasn't visited__

  - ___visited url example.___
    ``` curl
    curl -i -X POST -w "@curl-format.txt" \
    -H "Content-Type: application/json" \
    -d '{"url":"http://www.mkyong.com/oracle/oracle-plsql-bitand-function-example/"}' \
    http://localhost:8080/v1/urls/one
    ```
    The results is
    ```shell
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8
    Transfer-Encoding: chunked
    Date: Sat, 16 Sep 2017 23:35:48 GMT

    {
      "url" : "http://www.mkyong.com/oracle/oracle-plsql-bitand-function-example/",
      "httpCode" : 200,
      "timestamp" : "2017-09-16 16:26:15"
    }

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  0.010
                        ----------
             time_total:  0.010

    ```

  - ___not visited url exampple.___
    ```cull
    curl -i -X POST -w "@curl-format.txt" \
    -H "Content-Type: application/json" \
    -d '{"url":"http://www.google.com"}' \
    http://localhost:8080/v1/urls/one
    ```

    The results is
    ``` shell
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8
    Transfer-Encoding: chunked
    Date: Sat, 16 Sep 2017 23:42:50 GMT

    {
      "url" : "No Content",
      "httpCode" : 204,
      "timestamp" : null
    }

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  0.018
                        ----------
             time_total:  0.020
    ```

4. __Take URLs and returns the HTTP status code and timestamp they were fetched, or they were not visited__
    ```cull
    curl -i -X POST -w "@curl-format.txt" \
    -H "Content-Type: application/json" \
    -d '[{"url":"http://www.google.com"}, {"url":"http://www.mkyong.com/oracle/oracle-plsql-bitand-function-example/"}, {"url":"http://www.mkyong.com/java/java-how-to-print-a-pyramid/"}]' \
    http://localhost:8080/v1/urls/mult
    ```

    The results is

    ```shell
    [ {
      "url" : "No Content",
      "httpCode" : 204,
      "timestamp" : null
    }, {
      "url" : "http://www.mkyong.com/oracle/oracle-plsql-bitand-function-example/",
      "httpCode" : 200,
      "timestamp" : "2017-09-16 16:26:15"
    }, {
      "url" : "http://www.mkyong.com/java/java-how-to-print-a-pyramid/",
      "httpCode" : 200,
      "timestamp" : "2017-09-16 16:26:15"
    } ]

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  0.011
                        ----------
             time_total:  0.011
    ```

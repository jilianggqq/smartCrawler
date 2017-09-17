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
    -d '{"url":"https://www.import.io/", "depth":3}' \
    http://localhost:8080/v1/url
    ```
    The results is
    ```shell
    HTTP/1.1 201 
    Location: http://localhost:8080/v1/url
    Content-Length: 0
    Date: Sun, 17 Sep 2017 08:27:19 GMT

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  7.900
                        ----------
             time_total:  7.900
    ```

3. __Take a URL and returns the HTTP status code and timestamp it was fetched, or that it wasn't visited__

  - ___visited url example.___
    ``` curl
    curl -i -X POST -w "@curl-format.txt" \
    -H "Content-Type: application/json" \
    -d '{"url":"https://www.import.io/solutions/manufacturing/"}' \
    http://localhost:8080/v1/urls/one
    ```
    The results is
    ```shell
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8
    Transfer-Encoding: chunked
    Date: Sun, 17 Sep 2017 07:35:55 GMT

    {
      "url" : "https://www.import.io/solutions/manufacturing/",
      "httpCode" : 200,
      "timestamp" : "2017-09-17 00:30:31"
    }

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  0.042
                        ----------
             time_total:  0.042
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
    -d '[{"url":"http://www.google.com"}, {"url":"https://www.import.io/post/author/garyread/"}, {"url":"https://www.import.io/post/meidata-data-extraction/"}]' \
    http://localhost:8080/v1/urls/mult
    ```

    The results is

    ```shell
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8
    Transfer-Encoding: chunked
    Date: Sun, 17 Sep 2017 07:37:52 GMT

    [ {
      "url" : "No Content",
      "httpCode" : 204,
      "timestamp" : null
    }, {
      "url" : "https://www.import.io/post/author/garyread/",
      "httpCode" : 200,
      "timestamp" : "2017-09-17 00:30:20"
    }, {
      "url" : "https://www.import.io/post/meidata-data-extraction/",
      "httpCode" : 200,
      "timestamp" : "2017-09-17 00:30:31"
    } ]

    time_namelookup:  0.004
           time_connect:  0.004
        time_appconnect:  0.000
       time_pretransfer:  0.004
          time_redirect:  0.000
     time_starttransfer:  0.016
                        ----------
             time_total:  0.016
    ```


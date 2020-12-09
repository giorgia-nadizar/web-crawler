# WEB CRAWLER
Simple Java implementation of a Web Crawler with Mercator Frontier, followed by a clustering step to group similar web-pages according to the SimHash of their content.

## Features
* Robustness (see the behaviour when using "http://spidertrap.altervista.org/" as seed page)
* Fairness (respects robots.txt requirements and avoids too frequent reconnections to the same host)
* Multi-thread
* Tries to keep stored web-pages fresh via a heuristic estimation of their change rate

## Requirements
Redis.io key-value store must be installed. Available from https://redis.io/download

## Usage

* Before executing, you need to make sure your redis is fully empty. To do so you need to start the server (sudo service redis-server restart), start the client (redis-cli) and issue the FLUSHALL command.

* Then you can simply compile and run the Crawler's Main class. Before any run make sure to always empty redis.


## Tuning parameters

* Config static class can be used to change default parameters of the crawler. 

* Prioritiser class can be extended to implement a custom priority criterion (see RandomPrioritiser as an example).
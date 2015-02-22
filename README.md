Akka Crawler project
====================

This project is for playing with Akka actors. It mostly demonstrates the ability of Actors to keep state in concurrent
processing.

Also works as a demo for resource handling with actors: it uses a router to spin a set amount of actors that execute
one task at a time and thus enables the router to handle the amount of concurrent activity. Currently the actors block
to prevent concurrent tasks but that is an anti-pattern and will be replaced with a queue inside the actor.

What it does?
-------------

The piece of code itself crawls through Wikipedia articles. It starts from one article and visits all the links it
finds reporting to a Collector for each visited article and continuing to the links of those new articles. The crawler
could be used for instance to find the shortest path from one article to another or to create a network out of Wikipedia
articles.

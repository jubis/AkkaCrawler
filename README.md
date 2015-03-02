Akka Crawler project
====================

This project is for playing with Akka actors. It mostly demonstrates the ability of Actors to keep state in concurrent
processing.

Also works as a demo for resource handling with actors: it spins a set amount of actors that execute
one task at a time and thus enables the handling of the amount of concurrent activity.
This is implemented by the worker actors queuing the excess jobs and also the "hub" actor queuing the job until
it has a free worker available. Of course the queue would be actually required only in one side - but this is a demo.

What it does?
-------------

The piece of code itself crawls through Wikipedia articles. It starts from one article and visits all the links it
finds reporting to a Collector for each visited article and continuing to the links of those new articles. The crawler
could be used for instance to find the shortest path from one article to another or to create a network out of Wikipedia
articles.

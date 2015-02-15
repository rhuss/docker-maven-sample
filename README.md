# Docker Maven Sample

 This is a sample project for demonstrating the usage of [rhuss/docker-maven-plugin](https://github.com/rhuss/docker-maven-plugin)

## A Microservice

This project contains of a very simple Microservice, which accesses a Postgres database. The purpose of
this micro service is to simply log every access in the database and return the list of all log entries. In
addition, a simple integration test checks this behaviour. The database schema is created with [Flyway](http://flywaydb.org/).

This setup consist of two images:

* The official PostgreSQL database `postgres:9`
* An Image holding the Microservice. This image is created during the maven build and based on the official `java:8u40` image.

Both containers from this image are docker linked together during the integration test.

## Running the Example

The test can be startes with

````bash
$ mvn clean install
````

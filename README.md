# Lunch Place Voting System

A system helping in finding a consensus for choosing a place to have lunch in.

Runs in [Kalix](https://www.kalix.io/) and scales horizontally.

Each entity instance (area, voter and occasion) can run on a separate hardware so in theory the solution's scalability is unlimited.

Secured by [JWT](https://jwt.io/).

## Solution is Addressing the Following Problems

* Lunch occasion participants don't have a tool to find consensus while finding a lunch place
* Lunch occasion participants can't remember all lunch place options
* Different personalities may make the verbal at-time-voting unequal

## Ubiquitous Language

![ubiquitous_language.png](ubiquitous_language.png)

## Prerequisite

* Docker & Docker Compose
* Java 11 or later
* sbt
* grpcui (nice to have)

See more: https://docs.kalix.io/java/index.html#_prerequisites

## Run

Open three terminals and run each command in project root in own tabs in given order.

```
docker-compose up
sbt run
grpcui -plaintext localhost:9000
```

## [JWT](https://jwt.io/)

Required claims:
* `username` (`admin` has admin rights by default)
* `organization` (each organization is a tenant)

## Known Issues

1. AreasWithVotersJoinView `LEFT JOIN` does not seem to work properly -> Can't see area before it has it's first voter

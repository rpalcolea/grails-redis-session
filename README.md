# Redis Database Session Grails Plugin

## Summary
Stores HTTP sessions in a Redis.

## Requirements
1. [Database Session Plugin](http://grails.org/plugin/database-session) 1.2.1+
2. [Redis Plugin](http://grails.org/plugin/redis) 1.5.2+

## Description

This plugin lets you store HTTP session data in a mongodb using [Database Session Plugin](http://grails.org/plugin/database-session).

## Artifacts

This contains a RedisPersistentService.groovy with the persistence logic and RedisSessionCleanupService.groovy for cleanup sessions.

It registers two aliases inside the doWithSpring closure. With this, it can work with the existing Proxy Session Filter, SessionFilters (for flash scope) and DatabaseCleanupJob

```groovy
springConfig.addAlias 'gormPersisterService', 'redisPersistentService'
springConfig.addAlias 'databaseCleanupService', 'redisSessionCleanupService'
```

## Usage

For configuring, please refer to the [Database Session Plugin](http://grails.org/plugin/database-session) documents.

## Authors
[Roberto Perez Alcolea](http://blog.perezalcolea.info)

Raj Govindarajan

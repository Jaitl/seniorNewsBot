# seniorNewsBot
[![Build Status](https://travis-ci.org/Jaitl/seniorNewsBot.svg?branch=master)](https://travis-ci.org/Jaitl/seniorNewsBot)
[![Coverage Status](https://coveralls.io/repos/github/Jaitl/seniorNewsBot/badge.svg?branch=master)](https://coveralls.io/github/Jaitl/seniorNewsBot?branch=master)

## Use
Add bot [@SeniorNewsBot](t.me/SeniorNewsBot) and send `/start`

## Development
### Check code
```
sbt clean test scalastyle scapegoat scalafmtCheck scalafmtSbtCheck
```
### Build docker image
```
sbt docker:publish
```

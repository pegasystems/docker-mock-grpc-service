# Mock GRPC Service and Client

This repository contains a simple grpc service and client that can be used for testing.

This project is based on the [grpc java examples](https://github.com/grpc/grpc-java/tree/master/examples)

[![Build Status](https://travis-ci.org/pegasystems/docker-mock-grpc-service.svg?branch=master)](https://travis-ci.org/pegasystems/docker-mock-grpc-service)

The service docker image support the following commands

| command | env variables | description |
| ------- | --------- | ----------- |
| /server | MY_POD_NAME | Starts the test server with a single method `getPodName` that returns the value of `MY_POD_NAME` |
| /client | TARGET | Attempts to make 100 connections to the `TARGET` and verifies that the requests are load balanced |


Protocol definition can be found [here](src/main/proto/pb.proto)
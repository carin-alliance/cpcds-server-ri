#!/bin/sh

rm -rf ./data
wait
docker build -t cpcds-write .
docker compose up
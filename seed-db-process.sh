#!/bin/sh

bundle install
bundle exec ruby upload.rb
# wait
# curl http://localhost:8080/cpcds-server/fhir/$mark-all-resources-for-reindexing?_format=json
# curl http://localhost:8080/cpcds-server/fhir/$perform-reindexing-pass?_format=json

#!/bin/sh

docker compose down
wait
git add .
git commit -m "Updated data"
cp -r data data2
git checkout patient-access
rm -rf data
mv data2 data
git add .
git commit -m "Updated data"

#!/bin/sh -e

BUCKETS=(bucket-1 bucket-2 bucket-3 bucket-4)
COUNTRIES=(USA India Australia Japan Canada China Canada Germany)

function generate_record() {
  BUCKET_INDEX=$(($RANDOM % 4))
  COUNTRY_INDEX=$(($RANDOM % 8))
  CONVERSION_INDEX=$((2 * $BUCKET_INDEX + $COUNTRY_INDEX)) # A number between 0 and 16
  CONVERSION_RATE=$((100 + 20*$CONVERSION_INDEX))          # A number between 100 (~10%) and 320 (~32%)
  if [ $(($RANDOM % 1024)) -lt $CONVERSION_RATE ]; then
    echo \{\"time\":$(($1 + 225*($CONVERSION_INDEX - 1))),\"type\":\"conversion\",\"bucket\":\"${BUCKETS[$BUCKET_INDEX]}\",\"country\":\"${COUNTRIES[$COUNTRY_INDEX]}\"\}
  else
    echo \{\"time\":$(($1 + 225*($CONVERSION_INDEX - 1))),\"type\":\"allocation\",\"bucket\":\"${BUCKETS[$BUCKET_INDEX]}\",\"country\":\"${COUNTRIES[$COUNTRY_INDEX]}\"\}
  fi
}

for month in 10 11 12; do
  for day in $(seq -w 30); do
    for hour in $(seq -w 0 24); do
      FILE=2017$month$day$hour.log.gz
      NUMBER_OF_RECORDS=$((102400 + ($RANDOM % 1024) * 100))
      START_OF_HOUR=$(date --date "2017-${month}-${day} ${hour}:00:00" +%s)
      seq $NUMBER_OF_RECORDS | while read; do
        generate_record $START_OF_HOUR
      done | gzip > $FILE
      aws s3 cp $FILE s3://clojure-blueprints/chapter-2/$FILE
    done
  done
done

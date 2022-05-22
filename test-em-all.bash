#!/usr/bin/env bash
#
# ./grdelw clean build
# docker-compose build
# docker-compose up -d
#
# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8081}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {
    url=$@
    if curl $url -ks -f -o /dev/null
    then
          echo "Ok"
          return 0
    else
          echo -n "not yet"
          return 1
    fi;
}

function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
}

set -e

echo "Start:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down"
    docker-compose down
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService http://$HOST:$PORT/meal-composite/1

# Verify that a normal request works, expect three recommendedDrinks, three comments and three ingredients
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/1 -s"
assertEqual 1 $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 404 (Not Found) error is returned for a non existing mealId (13)
assertCurl 404 "curl http://$HOST:$PORT/meal-composite/13 -s"

# Verify that no comments, ingredinets and recommendedDrinks are returned for mealId 113
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/113 -s"
assertEqual 113 $(echo $RESPONSE | jq .mealId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 0 $(echo $RESPONSE | jq ".comments | length")
assertEqual 0 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that no comments are returned for mealId 213
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/213 -s"
assertEqual 213 $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a mealId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/meal-composite/-1 -s"
assertEqual "\"Invalid mealId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a mealId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/meal-composite/invalidMealId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker-compose down"
    docker-compose down
fi

echo "End:" `date`
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

function recreateComposite() {
    local mealId=$1
    local composite=$2

    assertCurl 200 "curl -X DELETE http://$HOST:$PORT/meal-composite/${mealId} -s"
    curl -X POST http://$HOST:$PORT/meal-composite -H "Content-Type: application/json" --data "$composite"
}

function setupTestdata() {
int commentId, String author, String subject
    body=\
'{"mealId":1,"mealName":"meal 1","category":"category 1", "reciepeDescription":"description", "calories":1, "prepartionTime":"1h",
  "serves":1,
    "ingredients":[
        {"ingredientId":1,"name":"ing name 1","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":2,"name":"ing name 2","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":3,"name":"ing name 3","amount":1,"unitOfMeasure":"kg"}
    ], "recommendedDrinks":[
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true}
    ], "comments":[
        {"commentId":1,"author":"author 1","subject":"subject 1"},
        {"commentId":2,"author":"author 2","subject":"subject 2"},
       {"commentId":3,"author":"author 3","subject":"subject 3"}
    ]}'
    recreateComposite 1 "$body"

    body=\
'{"mealId":113,"mealName":"meal 113","category":"category 113", "reciepeDescription":"description", "calories":113, "prepartionTime":"1h",
  "serves":113,
    "ingredients":[
        {"ingredientId":1,"name":"ing name 1","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":2,"name":"ing name 2","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":3,"name":"ing name 3","amount":1,"unitOfMeasure":"kg"}
    ]}'
    recreateComposite 113 "$body"

    body=\
'{"mealId":213,"mealName":"meal 213","category":"category 213", "reciepeDescription":"description", "calories":213, "prepartionTime":"1h",
  "serves":213,
    "recommendedDrinks":[
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true}
    ]}'
    recreateComposite 213 "$body"

    body=\
'{"mealId":313,"mealName":"meal 313","category":"category 313", "reciepeDescription":"description", "calories":313, "prepartionTime":"1h",
    "serves":313,
      "comments":[
            {"commentId":1,"author":"author 1","subject":"subject 1"},
            {"commentId":2,"author":"author 2","subject":"subject 2"},
            {"commentId":3,"author":"author 3","subject":"subject 3"}
      ]}'
      recreateComposite 313 "$body"
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

waitForService curl -X DELETE http://$HOST:$PORT/product-composite/13

setupTestdata

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
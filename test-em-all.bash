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
: ${MEAL_ID_INGS_RECS_COMS=2}
: ${MEAL_ID_NOT_FOUND=14}
: ${MEAL_ID_NO_RECS=114}
: ${MEAL_ID_NO_INGS=214}
: ${MEAL_ID_NO_COMS=314}

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

function testCompositeCreated() {

    # Expect that the Meal Composite for mealId $MEAL_ID_INGS_RECS_COMS has been created with three recommendations, three ingredients and three comments
    if ! assertCurl 200 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$MEAL_ID_INGS_RECS_COMS" $(echo $RESPONSE | jq .mealId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    set -e
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
            sleep 3
            echo -n ", retry #$n "
        fi
    done
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 1

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 3
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
    local mealId=$1
    local composite=$2

    assertCurl 200 "curl -X DELETE http://$HOST:$PORT/meal-composite/${mealId} -s"
    curl -X POST http://$HOST:$PORT/meal-composite -H "Content-Type: application/json" --data "$composite"
}

function setupTestdata() {

    body="{\"mealId\":$MEAL_ID_NO_RECS"
    body+=\
',"mealName":"meal 1","category":"category 1","reciepeDescription":"description","calories":1,"prepartionTime":"1h","serves":1,"ingredients":[
      {"ingredientId":1,"name":"ing name 1","amount":1,"unitOfMeasure":"kg"},
      {"ingredientId":2,"name":"ing name 2","amount":1,"unitOfMeasure":"kg"},
      {"ingredientId":3,"name":"ing name 3","amount":1,"unitOfMeasure":"kg"}
      ], "comments":[
      {"commentId":1,"author":"author 1","subject":"subject 1"},
      {"commentId":2,"author":"author 2","subject":"subject 2"},
      {"commentId":3,"author":"author 3","subject":"subject 3"}
]}'

   recreateComposite "$MEAL_ID_NO_RECS" "$body"


       body="{\"mealId\":$MEAL_ID_NO_COMS"
       body+=\
',"mealName":"meal 1","category":"category 1","reciepeDescription":"description","calories":1,"prepartionTime":"1h","serves":1,"ingredients":[
        {"ingredientId":1,"name":"ing name 1","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":2,"name":"ing name 2","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":3,"name":"ing name 3","amount":1,"unitOfMeasure":"kg"}
        ], "recommendedDrinks":[
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":2,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":3,"drinkName":"drink name 1","nonalcoholic":true}
]}'

      recreateComposite "$MEAL_ID_NO_COMS" "$body"

    body="{\"mealId\":$MEAL_ID_NO_INGS"
    body+=\
',"mealName":"meal 1","category":"category 1","reciepeDescription":"description","calories":1,"prepartionTime":"1h","serves":1,
    "recommendedDrinks":[
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":2,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":3,"drinkName":"drink name 1","nonalcoholic":true}
    ], "comments":[
        {"commentId":1,"author":"author 1","subject":"subject 1"},
        {"commentId":2,"author":"author 2","subject":"subject 2"},
        {"commentId":3,"author":"author 3","subject":"subject 3"}
]}'

   recreateComposite "$MEAL_ID_NO_INGS" "$body"

    body="{\"mealId\":$MEAL_ID_INGS_RECS_COMS"
    body+=\
',"mealName":"meal 1","category":"category 1","reciepeDescription":"description","calories":1,"prepartionTime":"1h","serves":1,
    "ingredients":[
        {"ingredientId":1,"name":"ing name 1","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":2,"name":"ing name 2","amount":1,"unitOfMeasure":"kg"},
        {"ingredientId":3,"name":"ing name 3","amount":1,"unitOfMeasure":"kg"}
    ],
    "recommendedDrinks":[
        {"recommendedDrinkId":1,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":2,"drinkName":"drink name 1","nonalcoholic":true},
        {"recommendedDrinkId":3,"drinkName":"drink name 1","nonalcoholic":true}
    ], "comments":[
         {"commentId":1,"author":"author 1","subject":"subject 1"},
         {"commentId":2,"author":"author 2","subject":"subject 2"},
         {"commentId":3,"author":"author 3","subject":"subject 3"}
]}'

   recreateComposite "$MEAL_ID_INGS_RECS_COMS" "$body"


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

waitForService curl http://$HOST:$PORT/actuator/health

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three recommendedDrinks, three comments and three ingredients
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS -s"
assertEqual "$MEAL_ID_INGS_RECS_COMS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 404 (Not Found) error is returned for a non existing mealId (13)
assertCurl 404 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_NOT_FOUND -s"

# Verify that no comments, ingredinets and recommendedDrinks are returned for mealId 113
# Verify that no recommendedDrink are returned for mealId $MEAL_ID_NO_RECS
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_NO_RECS -s"
assertEqual "$MEAL_ID_NO_RECS" $(echo $RESPONSE | jq .mealId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that no comments are returned for mealId $MEAL_ID_NO_COMS
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_NO_COMS -s"
assertEqual "$MEAL_ID_NO_COMS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 0 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that no ingredients are returned for mealId $MEAL_ID_NO_INGS
assertCurl 200 "curl http://$HOST:$PORT/meal-composite/$MEAL_ID_NO_INGS -s"
assertEqual "$MEAL_ID_NO_INGS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 0 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a mealId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/meal-composite/-1 -s"
assertEqual "\"Invalid mealId: -1\"" "$(echo $RESPONSE | jq .message)"


# Verify that a 400 (Bad Request) error error is returned for a mealId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/meal-composite/invalidMealId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi
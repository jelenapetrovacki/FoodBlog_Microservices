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
: ${PORT=8443}
: ${MEAL_ID_INGS_RECS_COMS=2}
: ${MEAL_ID_NOT_FOUND=13}
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
    if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS -s"
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

    assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/meal-composite/${mealId} -s"
    curl -X POST -k https://$HOST:$PORT/meal-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
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

function testCircuitBreaker() {

    echo "Start Circuit Breaker tests!"

    EXEC="winpty docker run --rm -it --network=my-network alpine"

    # First, use the health - endpoint to verify that the circuit breaker is closed
    assertEqual "CLOSED" "$($EXEC wget meal-composite:8081/actuator/health -qO - | jq -r .components.circuitBreakers.details.meal.details.state)"

    # Open the circuit breaker by running three slow calls in a row, i.e. that cause a timeout exception
    # Also, verify that we get 500 back and a timeout related error message
    for ((n=0; n<3; n++))
    do
        assertCurl 500 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS?delay=3 $AUTH -s"
        message=$(echo $RESPONSE | jq -r .message)
        assertEqual "Did not observe any item or terminal signal within 2000ms" ".${message:0:57}"
    done

    # Verify that the circuit breaker now is open by running the slow call again, verify it gets 200 back, i.e. fail fast works, and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS?delay=3 $AUTH -s"
    assertEqual "Fallback meal2" "$(echo "$RESPONSE" | jq -r .mealName)"

    # Also, verify that the circuit breaker is open by running a normal call, verify it also gets 200 back and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS $AUTH -s"
    assertEqual "Fallback meal2" "$(echo "$RESPONSE" | jq -r .mealName)"

    # Verify that a 404 (Not Found) error is returned for a non existing mealId ($MOV_ID_NOT_FOUND) from the fallback method.
    assertCurl 404 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_NOT_FOUND $AUTH -s"
    assertEqual "Meal Id: $MEAL_ID_NOT_FOUND not found in fallback cache!" "$(echo $RESPONSE | jq -r .message)"

    # Wait for the circuit breaker to transition to the half open state (i.e. max 10 sec)
    echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
    sleep 10

    # Verify that the circuit breaker is in half open state
    assertEqual "HALF_OPEN" "$($EXEC wget meal-composite:8081/actuator/health -qO - | jq -r .components.circuitBreakers.details.meal.details.state)"

    # Close the circuit breaker by running three normal calls in a row
    # Also, verify that we get 200 back and a response based on information in the meal database
    for ((n=0; n<3; n++))
    do
        assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS $AUTH -s"
        assertEqual "Meal 214" "$(echo "$RESPONSE" | jq -r .mealName)"
    done

    # Verify that the circuit breaker is in closed state again
    assertEqual "CLOSED" "$($EXEC wget meal-composite:8081/actuator/health -qO - | jq -r .components.circuitBreakers.details.meal.details.state)"

    # Verify that the expected state transitions happened in the circuit breaker
    assertEqual "CLOSED_TO_OPEN"      "$($EXEC wget meal-composite:8081/actuator/circuitbreakerevents/meal/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-3].stateTransition)"
    assertEqual "OPEN_TO_HALF_OPEN"   "$($EXEC wget meal-composite:8081/actuator/circuitbreakerevents/meal/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-2].stateTransition)"
    assertEqual "HALF_OPEN_TO_CLOSED" "$($EXEC wget meal-composite:8081/actuator/circuitbreakerevents/meal/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-1].stateTransition)"
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

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three recommendedDrinks, three comments and three ingredients
assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS $AUTH -s"
assertEqual "$MEAL_ID_INGS_RECS_COMS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 404 (Not Found) error is returned for a non existing mealId (13)
assertCurl 404 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_NOT_FOUND $AUTH -s"

# Verify that no comments, ingredinets and recommendedDrinks are returned for mealId 113
# Verify that no recommendedDrink are returned for mealId $MEAL_ID_NO_RECS
assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_NO_RECS $AUTH -s"
assertEqual "$MEAL_ID_NO_RECS" $(echo $RESPONSE | jq .mealId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that no comments are returned for mealId $MEAL_ID_NO_COMS
assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_NO_COMS $AUTH -s"
assertEqual "$MEAL_ID_NO_COMS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 0 $(echo $RESPONSE | jq ".comments | length")
assertEqual 3 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that no ingredients are returned for mealId $MEAL_ID_NO_INGS
assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_NO_INGS $AUTH -s"
assertEqual "$MEAL_ID_NO_INGS" $(echo $RESPONSE | jq .mealId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendedDrinks | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
assertEqual 0 $(echo $RESPONSE | jq ".ingredients | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a mealId that is out of range (-1)
assertCurl 422 "curl -k https://$HOST:$PORT/meal-composite/-1 $AUTH -s"
assertEqual "\"Invalid mealId: -1\"" "$(echo $RESPONSE | jq .message)"


# Verify that a 400 (Bad Request) error error is returned for a mealId that is not a number, i.e. invalid format
assertCurl 400 "curl -k https://$HOST:$PORT/meal-composite/invalidMealId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/meal-composite/$MEAL_ID_INGS_RECS_COMS $READER_AUTH -X DELETE -s"

# testCircuitBreaker

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi
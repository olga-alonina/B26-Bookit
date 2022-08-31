Feature: GET /api/campuses/{campus_location}

  Scenario: positive test
    Given I have a token with teacher credentials and I am signed in
    When I send GET request to "/api/rooms/available"
    Then I should get status code 200
    Then I should get the following "src/test/resources/json-schemas/AvailableRoom.json"

  Scenario: negative test
    Given I have invalid session token
    When I send GET request to "/api/rooms/available"
    Then I should get status code 422
Feature: GET /api/campuses/my

  Scenario: positive test
    Given I have a token with teacher credentials and I am signed in
    When I send GET request to "/api/campuses/my"
    Then I should get status code 200
    Then I should get the following "src/test/resources/json-schemas/AllCampuses.json"

  Scenario: negative test
    Given I have invalid session token
    When I send GET request to "/api/campuses/my"
    Then I should get status code 422
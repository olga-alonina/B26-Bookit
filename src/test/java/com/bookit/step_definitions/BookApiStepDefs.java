package com.bookit.step_definitions;

import com.bookit.pages.*;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.CoreMatchers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class BookApiStepDefs {
    LoginPage loginPage = new LoginPage();
    DashBoard_MapPage dashBoard_mapPage = new DashBoard_MapPage();
    MePage mePage = new MePage();
    public static final Logger LOG = LogManager.getLogger();
    String baseUrl = Environment.BASE_URL;
    String accessToken;
    Response response;
    Map<String, String> newRecordMap;

    @Given("User logged in to Bookit api as teacher role")
    public void user_logged_in_to_Bookit_api_as_teacher_role() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user : email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        accessToken = BookItApiUtil.getAccessToken(email, password);

        if (accessToken == null || accessToken.isEmpty()) {
            LOG.error("Could not authorize user in authorization server");
            fail("Could not authorize user in authorization server");
        }

    }

    @Given("User sends GET request to {string}")
    public void user_sends_GET_request_to(String endpoint) { ///api/users/me
        response = given().accept(ContentType.JSON)
                .and().header("Authorization", accessToken)
                .when().get(baseUrl + endpoint);
        response.then().log().all();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expStatusCode) {
        assertEquals("Status code verification failed", expStatusCode, response.statusCode());
        response.then().statusCode(expStatusCode);
    }

    @Then("content type is {string}")
    public void content_type_is(String expContentType) {
        response.then().contentType(expContentType);
        assertEquals("Content type verification failed. expected = " + expContentType + " but actual = " + response.contentType()
                , expContentType, response.contentType());
    }

    /**
     * {
     * "id": 11516,
     * "firstName": "Barbabas",
     * "lastName": "Lyst",
     * "role": "teacher"
     * }
     */
    @Then("role is {string}")
    public void role_is(String expRole) {
        assertEquals(expRole, response.path("role"));

        JsonPath jsonPath = response.jsonPath();
        assertEquals(expRole, jsonPath.getString("role"));

        //deserialization: json to map or json to pojo
        Map<String, ?> responseMap = response.as(Map.class);
        assertEquals(expRole, responseMap.get("role"));
    }

    @Given("User logged in to Bookit app as teacher role")
    public void user_logged_in_to_Bookit_app_as_teacher_role() {
        Driver.getDriver().get(Environment.URL);
        loginPage.logIn(Environment.TEACHER_EMAIL, Environment.TEACHER_PASSWORD);

    }

    @Given("User is on self page")
    public void user_is_on_self_page() {
        dashBoard_mapPage.myButton.click();
        dashBoard_mapPage.selfButton.click();

    }

    @Then("User should see same info on UI and API")
    public void user_should_see_same_info_on_UI_and_API() {
        Map<String, String> apiInfo = response.body().as(Map.class);
        System.out.println("apiInfo = " + apiInfo);
        String name = apiInfo.get("firstName") + " " + apiInfo.get("lastName");
        String name2 = mePage.gather_ui_Info().get("name");
        assertEquals(name, name2);

        String role = apiInfo.get("role");
        String role2 = mePage.gather_ui_Info().get("role");
        assertEquals(role, role2);

    }

    @When("Users sends POST request to {string} with following info:")
    public void users_sends_POST_request_to_with_following_info(String endpoint, Map<String, String> teamInfo) {
        response = given().accept(ContentType.JSON)
                .and().queryParams(teamInfo)
                .and().header("Authorization", accessToken)
                .when().post(baseUrl + endpoint);
        response.prettyPrint();
        newRecordMap = teamInfo;
    }

    @Then("Database should persist same team info")
    public void database_should_persist_same_team_info() {
        int newTeamID = response.path("entryiId");

        String sql = "SELECT * FROM team WHERE id = " + newTeamID;
        Map<String, Object> dbNewTeamMap = DBUtils.getRowMap(sql);

        System.out.println("sql = " + sql);
        System.out.println("dbNewTeamMap = " + dbNewTeamMap);

        assertThat(dbNewTeamMap.get("id"), equalTo((long) newTeamID));
        assertThat(dbNewTeamMap.get("name"), equalTo(newRecordMap.get("team-name")));
        assertThat(dbNewTeamMap.get("batch_number").toString(), equalTo(newRecordMap.get("batch-number")));
    }

    @Then("User deletes previously created team")
    public void user_deletes_previously_created_team() {
        int teamId = response.path("entryiId");
        given().accept(ContentType.JSON)
                .and().header("Authorization", accessToken)
                .and().pathParam("id", teamId)
                .when().delete(baseUrl + "/api/teams/{id}")
                .then().log().all();
    }

    @And("User sends GET request to {string} with {string}")
    public void user_sends_get_request_to_with(String endPoints, String teamId) {
        response = given().accept(ContentType.JSON)
                .and().pathParam("id", teamId)
                .and().header("Authorization", accessToken)
                .when().get(baseUrl + endPoints);
    }

    @And("Team name should be {string} in response")
    public void team_name_should_be_in_response(String teamName) {
        assertEquals(response.body().path("name"), teamName);
    }

    @And("Database query should have same {string} and {string}")
    public void database_query_should_have_same_and(String teamId, String teamName) {
        int id = Integer.parseInt(teamId);
        String sql = "SELECT * FROM team WHERE id = " + teamId;
        Map<String, Object> dbNewTeamMap = DBUtils.getRowMap(sql);

        System.out.println("sql = " + sql);
        System.out.println("dbNewTeamMap = " + dbNewTeamMap);

        assertThat(dbNewTeamMap.get("id"), equalTo((long) id));
        assertThat(dbNewTeamMap.get("name"), equalTo(teamName));
    }


    @Then("Database should contain same student info")
    public void database_should_contain_same_student_info() {
        int newStudentId = response.path("entryiId");
        String sql = "select * from users where id = " + newStudentId;
        Map<String, Object> dbStudentMap = DBUtils.getRowMap(sql);
        System.out.println("dbStudentMap = " + dbStudentMap);

        assertThat(newRecordMap.get("first-name"), equalTo(dbStudentMap.get("firstname")));
        assertThat(newRecordMap.get("last-name"), equalTo(dbStudentMap.get("lastname")));
        assertThat(newRecordMap.get("email"),is(dbStudentMap.get("email")));
        assertThat(newRecordMap.get("role"),is(dbStudentMap.get("role")));
    }

    @Then("User should able to login bookit app using {string} and {string}")
    public void user_should_able_to_login_bookit_app_using_and(String email, String password) {
        Driver.getDriver().get(Environment.URL);
        loginPage.logIn(newRecordMap.get("email"),newRecordMap.get("password"));

    }
    /*{

    "entryiId": 15525,
    "entryType": "Student",
    "message": "user anna zayarny has been added to database."

    }*/

    @Then("User deletes previously created student")
    public void user_deletes_previously_created_student() {
        int newStudentId = response.path("entryiId");
        given().accept(ContentType.JSON)
                .and().header("Authorization",accessToken)
                .and().pathParam("id", newStudentId)
                .when().delete(baseUrl + "/api/students/{id}")
                .then().statusCode(204).log().all();
    }
//    " first-name"     "anna"
//            "last-name  "  "zayarny"
//            "email"            "annazayarny18041988@gmail.com"
//            |"password"        "abc123456789"
//            "role"             "student-team-leader "
//            "campus-location"  "VA"
//            "batch-number"    "8"
//             "team-name"    "Nukes"
}

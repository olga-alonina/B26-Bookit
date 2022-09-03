package com.bookit.step_definitions;

import com.bookit.pages.*;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.*;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.*;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class BookApiStepDefs {
    LoginPage loginPage = new LoginPage();
    DashBoard_MapPage dashBoard_mapPage = new DashBoard_MapPage();
    MePage mePage = new MePage();
    HuntPage huntPage = new HuntPage();
    public static final Logger LOG = LogManager.getLogger();
    String baseUrl = Environment.BASE_URL;
    String accessToken;
    Response response;
    Map<String, String> newRecordMap;
    WebDriverWait wait = new WebDriverWait(Driver.getDriver(), 25);


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
        assertThat(newRecordMap.get("email"), is(dbStudentMap.get("email")));
        assertThat(newRecordMap.get("role"), is(dbStudentMap.get("role")));
    }

    @Then("User should able to login bookit app using {string} and {string}")
    public void user_should_able_to_login_bookit_app_using_and(String email, String password) {
        Driver.getDriver().get(Environment.URL);
        loginPage.logIn(newRecordMap.get("email"), newRecordMap.get("password"));

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
                .and().header("Authorization", accessToken)
                .and().pathParam("id", newStudentId)
                .when().delete(baseUrl + "/api/students/{id}")
                .then().statusCode(204).log().all();
    }

    @Given("User logged in to Bookit api as team lead role")
    public void user_logged_in_to_Bookit_api_as_team_lead_role() {
        String email = Environment.LEADER_EMAIL;
        String password = Environment.LEADER_PASSWORD;
        LOG.info("Authorizing leader user : email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        accessToken = BookItApiUtil.getAccessToken(email, password);
    }

    @Then("response should match {string} schema")
    public void response_should_match_schema(String jsonFileName) {
        response.then().body(JsonSchemaValidator.matchesJsonSchema(new File(jsonFileName)));
    }

    @Given("User logged in to Bookit app as team lead role")
    public void user_logged_in_to_Bookit_app_as_team_lead_role() {
        Driver.getDriver().get(Environment.URL);
        loginPage.logIn(Environment.LEADER_EMAIL, Environment.LEADER_PASSWORD);
    }

    @When("User goes to room hunt page")
    public void user_goes_to_room_hunt_page() {
        dashBoard_mapPage.huntButton.click();

    }

    @When("User searches for room with date:")
    public void user_searches_for_room_with_date(Map<String, String> searchInfoTable) {
        Actions actions = new Actions(Driver.getDriver());
        //date
        huntPage.dateField.sendKeys(searchInfoTable.get("date"));
        //from
       // wait.until(ExpectedConditions.invisibilityOf(huntPage.loadingBar));
        wait.until( ExpectedConditions.elementToBeClickable(huntPage.fromField));//todo dont delete it
        actions.moveToElement(huntPage.fromField).click().perform();
        for (WebElement each : huntPage.timeList) {
            if (each.getText().equals(searchInfoTable.get("from"))) {
                each.click();
                break;
            }
        }
        //to
        wait.until( ExpectedConditions.elementToBeClickable(huntPage.toField));
        actions.moveToElement(huntPage.toField).click().perform();
        for (WebElement each1 : huntPage.timeList) {
            if (each1.getText().equals(searchInfoTable.get("to"))) {
                each1.click();
                break;
            }
        }
        //search
        wait.until( ExpectedConditions.elementToBeClickable(huntPage.searchSign));
        actions.click(huntPage.searchSign).perform();
        wait.until(ExpectedConditions.textToBePresentInElement(huntPage.headerPageTitle , "free spots"));


    }

    @Then("User should see available rooms")
    public void user_should_see_available_rooms() {
        assertTrue(huntPage.amountRoomsUI.size() > 0);

    }

    @Then("User sends GET request to {string} with:")
    public void user_sends_GET_request_to_with(String endPoints, Map<String, String> roomParams) {
        System.out.println("baseUrl + endPoints = " + baseUrl + endPoints);
        System.out.println("accessToken = " + accessToken);
        response = given().accept(ContentType.JSON)
                .and().queryParams(roomParams)
                .and().header("Authorization", accessToken)
                .log().all()
                .when().get(baseUrl + endPoints);

    }

    @Then("available rooms in response should match UI results")
    public void available_rooms_in_response_should_match_UI_results() {
        int UIRooms = huntPage.amountRoomsUI.size();
        System.out.println("hunt2Page.amountRoomsUI = " + huntPage.amountRoomsUI);
        List<String> amountRoomsAPI = response.path("name");
        System.out.println("amountRoomsAPI = " + amountRoomsAPI);
        int APIRooms = amountRoomsAPI.size();
        assertEquals(APIRooms, UIRooms);
    }

    @Then("available rooms in database should match UI and API results")
    public void available_rooms_in_database_should_match_UI_and_API_results() {
        String dbPath = "select room.id , room.name , description , capacity , withtv , withwhiteboard \n" +
                "          from room inner join cluster c on c.id = room.cluster_id \n" +
                "          where c.name='light-side';";
        List<Object> freeRoomDB = DBUtils.getColumnData(dbPath, "name");
        List<String> amountRoomsAPI = response.path("name");
        List<String>nameRoomUI = new ArrayList<>();
        for(WebElement each:huntPage.nameRoomsUI){
            nameRoomUI.add(each.getText());
        }
        assertEquals(nameRoomUI, freeRoomDB);
        assertEquals(amountRoomsAPI, freeRoomDB);

    }

}

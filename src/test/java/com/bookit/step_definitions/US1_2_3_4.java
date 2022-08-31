package com.bookit.step_definitions;


import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.fail;


public class US1_2_3_4 {

    public static final Logger LOG = LogManager.getLogger();
    public String token;
    public Response response;
    String baseUrl = Environment.BASE_URL;
    String location="IL";//


    @Given("I have a token with teacher credentials and I am signed in")
    public void i_have_a_token_with_teacher_credentials_and_I_am_signed_in() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user : email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        token = BookItApiUtil.getAccessToken(email, password);

        if (token == null || token.isEmpty()) {
            LOG.error("Could not authorize user in authorization server");
            fail("Could not authorize user in authorization server");
        }
    }

    @When("I send GET request to {string}")
    public void i_send_GET_request_to(String path) {
        switch (path) {
            case ("/api/campuses/my"):
            case ("/api/campuses"):
                response = given().accept(ContentType.JSON)
                        .and().header("Authorization", token)
                        .when().get(baseUrl + path);
                break;
            case ("/api/campuses/{campus_location}"):
                response = given().accept(ContentType.JSON)
                        .and().pathParam("campus_location", location)
                        .and().header("Authorization", token)
                        .when().get(baseUrl + path);
                break;
            case ("/api/rooms/available"):
                Map<String, Object> timeLine = new HashMap<>();
                timeLine.put("year", 2022);
                timeLine.put("month", 8);
                timeLine.put("day", 28);
                timeLine.put("conference-type", "SOLID");
                timeLine.put("cluster-name", "dark-side");
                timeLine.put("timeline-id", 6);
                timeLine.put("timeline-id", 7);

                response = given().accept(ContentType.JSON)
                        .and().header("Authorization", token)
                        .and().queryParams(timeLine)
                        .when().get(baseUrl + path);
                break;
        }
    }

    @Then("I should get status code {int}")
    public void i_should_get_status_code(int statusCode) {
        response.then().assertThat().statusCode(statusCode);

    }

    @Then("I should get the following {string}")
    public void i_should_get_the_following(String jsonFileName) {
        response.prettyPrint();
        response.then().body(JsonSchemaValidator.matchesJsonSchema(
                new File(jsonFileName)
        )).log().all();
    }


    @Given("I have invalid session token")
    public void i_have_invalid_session_token() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user : email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        token = BookItApiUtil.getAccessToken(email, password);

        if (token == null || token.isEmpty()) {
            LOG.error("Could not authorize user in authorization server");
            fail("Could not authorize user in authorization server");
        }
        token = token + "something";
    }
}
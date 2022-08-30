package com.bookit.step_definitions;

import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.Environment;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookApiStepDefs {

    public static final Logger LOG = LogManager.getLogger();
    String url = Environment.BASE_URL;

    @Given("User logged in to Bookit api as teacher role")
    public void user_logged_in_to_Bookit_api_as_teacher_role() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user : email = " + email + ", password = " + password);

    }

    @Given("User sends GET request to {string}")
    public void user_sends_GET_request_to(String string) {

    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer int1) {

    }

    @Then("content type is {string}")
    public void content_type_is(String string) {

    }

    @Then("role is {string}")
    public void role_is(String string) {

    }

}

package com.bookit.pages;

import com.bookit.utilities.Environment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {
    @FindBy(name="email")
    public WebElement emailInputBox;
    @FindBy(name="password")
    public  WebElement passwordInputBox;
    @FindBy(css = "[class='button is-dark']")
    public  WebElement signInButton;

    public  void logIn(String email,String password){
        emailInputBox.sendKeys(""+email);
        passwordInputBox.sendKeys(""+password);
        signInButton.click();
    }
}

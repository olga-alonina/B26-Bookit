package com.bookit.pages;

import java.util.*;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HuntPage extends BasePage {
    @FindBy(id = "mat-input-0")
    public WebElement dateField;

    @FindBy(css = "#mat-select-0 span")
    public WebElement fromField;
    @FindBy(css = "#mat-select-1 span")
    public WebElement toField;

    @FindBy(css = "[class='mat-option-text']")
    public List<WebElement> timeList;

    @FindBy(css = "[class='mat-button']")
    public WebElement searchSign;

    @FindBy(css = "[class='loading-bar-fixed']")
    public WebElement loadingBar;

    @FindBy(css = "[class='column is-one-third ng-star-inserted']")
    public List<WebElement> amountRoomsUI;

    @FindBy(css = "[class=\"title is-size-4\"]")
    public List<WebElement> nameRoomsUI;
    @FindBy(css = "[class=\"title\"]")
    public WebElement headerPageTitle;

}

package com.bookit.pages;

import java.util.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class DashBoard_MapPage extends BasePage{

    @FindBy(css = "[class='navbar-end'] a")
    public List<WebElement>topRightMenu;
    @FindBy(xpath = "//div[@class='navbar-item has-dropdown is-hoverable']/a[.='my']")
    public WebElement myButton;
    @FindBy(css = "[href='/me']")
    public WebElement selfButton;

    @FindBy(css = "[href='/hunt']")
    public WebElement huntButton;

}

package com.bookit.pages;


import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;



public class MePage extends BasePage{

    @FindBy(css = "[class='title is-6']")
    public List<WebElement> listOfInfo;
    @FindBy(css = "[class='subtitle is-7']")
    public List<WebElement> subOfInfo;

    public Map<String, String> gather_ui_Info() {
        Map<String, String> uiInfo = new HashMap();
        for (int i = 0; i < listOfInfo.size(); i++) {
            for (int i1 = 0; i1 < subOfInfo.size(); i1++) {
                if (!uiInfo.containsValue(listOfInfo.get(i).getText())
                        &&(!uiInfo.containsKey(subOfInfo.get(i1).getText()))) {
                    uiInfo.put(subOfInfo.get(i1).getText(), listOfInfo.get(i).getText());
                }
            }
        }
        System.out.println("uiInfo = " + uiInfo);
        return uiInfo;
    }
}

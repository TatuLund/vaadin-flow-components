package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void testDefaultStrings() {
        LoginElement login = $(LoginElement.class).waitForFirst();

        Assert.assertEquals("App name", login.getTitle());
        Assert.assertEquals("Inspiring application description", login.getMessage());
        Assert.assertEquals("Log in", login.getFormTitle());
        if (!BrowserUtil.isEdge(getDesiredCapabilities()) && !BrowserUtil.isSafari(getDesiredCapabilities())) {
            // Error message should be hidden by default, however Safari and Edge drivers return the innerHTML content
            Assert.assertEquals("", login.getErrorMessageTitle());
            Assert.assertEquals("", login.getErrorMessage());
        }
        Assert.assertEquals("Username", login.getUsernameField().getLabel());
        Assert.assertEquals("Password", login.getPasswordField().getLabel());
        Assert.assertEquals("Log in", login.getSubmitButton().getText());
        Assert.assertEquals("Forgot password", login.getForgotPasswordButton().getText());
        Assert.assertEquals("In case you need to provide some additional info for the user.",
                login.getAdditionalInformation());
    }

    @Test
    public void forgotPassword() {
        checkForgotPassword($(LoginElement.class).waitForFirst());
    }

    private void checkForgotPassword(LoginElement login) {
        login.forgotPassword();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Forgot password button pressed",
                notification);
    }

    @Test
    public void disabledLogin() {
        getDriver().get(getBaseURL() + "/disable-login");
        LoginElement login = $(LoginElement.class).waitForFirst();
        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("password");
        login.submit();

        Assert.assertFalse("Login notification was shown",
                $(NotificationElement.class).waitForFirst().isOpen());

        if (BrowserUtil.isEdge(getDesiredCapabilities())) {
            skipTest("Skip for Edge due to the sendKeys usage");
        }
        login.getPasswordField().focus();
        login.sendKeys(Keys.ENTER);
        Assert.assertFalse("Login notification was shown",
                $(NotificationElement.class).waitForFirst().isOpen());

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            skipTest("Temporary Skip IE until disabled property won't reflectToAttribute");
            Assert.assertFalse("Disabled property should not reflect to attribute", login.hasAttribute("disabled"));
        }
        // Forgot password event should be processed anyway
        checkForgotPassword(login);
    }

    @Test
    public void enterKeyLogin() {
        if (BrowserUtil.isEdge(getDesiredCapabilities())) {
            skipTest("Skip for Edge due to the sendKeys usage");
        }
        LoginElement login = $(LoginElement.class).waitForFirst();
        checkSuccessfulLogin(login, () -> {
            login.focus();
            login.sendKeys(Keys.ENTER);
        });
    }

    @Test
    public void passwordEnterKeyLogin() {
        if (BrowserUtil.isEdge(getDesiredCapabilities())) {
            skipTest("Skip for Edge due to the sendKeys usage");
        }
        LoginElement login = $(LoginElement.class).waitForFirst();
        checkSuccessfulLogin(login, () -> {
            login.getPasswordField().focus();
            login.sendKeys(Keys.ENTER);
        });
    }

    @Test
    public void usernameEnterKeyLogin() {
        if (BrowserUtil.isEdge(getDesiredCapabilities())) {
            skipTest("Skip for Edge due to the sendKeys usage");
        }
        LoginElement login = $(LoginElement.class).waitForFirst();
        checkSuccessfulLogin(login, () -> {
            login.getUsernameField().focus();
            login.sendKeys(Keys.ENTER);
        });
    }

    @Test
    public void login() {
        LoginElement login = $(LoginElement.class).waitForFirst();
        checkSuccessfulLogin(login, () -> login.submit());
    }

    private void checkSuccessfulLogin(LoginElement login, Runnable submit) {
        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("password");
        submit.run();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Successful login", notification);
    }

    @Test
    public void failedLogin() {
        LoginElement login = $(LoginElement.class).waitForFirst();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("wrongPassword");
        login.submit();
        String notification = $(NotificationElement.class).waitForFirst().getText();
        Assert.assertEquals("Login failed", notification);
    }

    @Test
    public void actionLogin() {
        getDriver().get(getBaseURL() + "/action");
        LoginElement login = $(LoginElement.class).waitForFirst();

        login.getUsernameField().setValue("username");
        login.getPasswordField().setValue("wrongPassword");
        login.submit();
        Assert.assertTrue("Redirect didn't happened on login",
                getDriver().getCurrentUrl().endsWith("process-login-here"));
    }
}

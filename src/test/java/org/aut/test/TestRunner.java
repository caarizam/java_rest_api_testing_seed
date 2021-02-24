package org.aut.test;

import cucumber.api.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        plugin = {
                "pretty",
                "json:target/cucumber-json-report",
                "html:target/cucumber-html"
        },
            features = {"src/test/resources/features/"},
        glue = {"classpath:org.aut.steps.definition"},
        tags = {"@e2e"})
public class TestRunner {
}

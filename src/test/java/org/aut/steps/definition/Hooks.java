package org.aut.steps.definition;

import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.aut.common.Constants;
import org.aut.helpers.PropertiesReaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Suite-level hooks (BeforeAll, AfterAll) run once before the first scenario and once after the last one.
 * Cucumber requires them to be static, so no step classes can be injected there.
 * Cucumber has no BeforeFeature/AfterFeature hook, so the feature boundaries are detected from the scenario
 * URI: the first scenario of a new feature file starts it, and the next file (or AfterAll) finishes it.
 * Step-level hooks (BeforeStep, AfterStep) run around every step of every scenario.
 */
public class Hooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);
    private static final Pattern FEATURE_LINE = Pattern.compile("^\s*Feature:\s*(.*?)\s*$");

    private static URI currentFeatureUri;
    private static String currentFeatureName;


    @BeforeAll
    public static void beforeAll() {
        PropertiesReaderHelper properties = new PropertiesReaderHelper();

        LOGGER.info("Test suite starting against {}:{}", properties.getProperty("url-base"), properties.getProperty("port"));

        if (properties.getProperty("api-key").isEmpty()) {
            LOGGER.warn("No api-key configured: requests will be sent without the x-api-key header");
        }
    }

    @AfterAll
    public static void afterAll() {
        finishCurrentFeature();
        Constants.getInstance().clearAccessTokens();

        LOGGER.info("Test suite finished, stored access tokens cleared");
    }

    // order = 0 so it runs before any other @Before hook
    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        PropertiesReaderHelper properties = new PropertiesReaderHelper();
        boolean singleRegister = Boolean.parseBoolean(properties.getProperty("single_register"));
        if (!scenario.getUri().equals(currentFeatureUri)) {
            finishCurrentFeature();
            currentFeatureUri = scenario.getUri();
            currentFeatureName = readFeatureName(currentFeatureUri);

            if (singleRegister) {
                Constants.getInstance().setShouldICreateAUser(true);
            }

            LOGGER.info("Feature starting: '{}' ({})", currentFeatureName, currentFeatureUri);
        } else {
            if (singleRegister && Constants.getInstance().isAuthenticated()) {
                Constants.getInstance().setShouldICreateAUser(false);
            }
        }
    }

    private static void finishCurrentFeature() {
        if (currentFeatureUri != null) {
            LOGGER.info("Feature finished: '{}'", currentFeatureName);
            currentFeatureUri = null;
            currentFeatureName = null;
            Constants.getInstance().clearAccessTokens();
            Constants.getInstance().setShouldICreateAUser(true);
        }
    }

    private static String readFeatureName(URI uri) {
        String path = uri.getSchemeSpecificPart();
        try (InputStream input = Hooks.class.getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = FEATURE_LINE.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not read the feature name from {}", uri, ex);
        }
        return uri.toString();
    }

    @BeforeStep
    public void beforeStep(Scenario scenario) {
        LOGGER.debug("Step starting in scenario '{}'", scenario.getName());
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        // The scenario status already reflects the step that just ran
        if (scenario.isFailed()) {
            LOGGER.warn("Step failed in scenario '{}'", scenario.getName());
        } else {
            LOGGER.debug("Step finished in scenario '{}'", scenario.getName());
        }
    }
}

package bdd;

import cucumber.api.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/specs",
        glue = "bdd/steps",
        tags = "@nosuchtag"
)
public class ExecutableSpecifications {
}

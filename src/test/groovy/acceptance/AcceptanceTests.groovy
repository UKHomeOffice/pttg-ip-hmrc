package acceptance;

import cucumber.api.CucumberOptions
import net.serenitybdd.cucumber.CucumberWithSerenity
import org.junit.runner.RunWith

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/specs",
        glue = "acceptance/steps",
        tags = "not @WIP"
)
class AcceptanceTests {}

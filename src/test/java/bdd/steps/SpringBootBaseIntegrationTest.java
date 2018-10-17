package bdd.steps;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.ServiceRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "pttg.audit.url=http://localhost:1111",
                "base.hmrc.url=http://localhost:2222",
                "base.hmrc.access.code.url=http://localhost:3333",
                "hmrc.sa.self-employment-only=false"
        })
public abstract class SpringBootBaseIntegrationTest {
}

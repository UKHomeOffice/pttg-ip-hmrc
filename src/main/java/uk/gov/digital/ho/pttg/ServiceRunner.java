package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_SERVICE_STARTED;

@SpringBootApplication
@Slf4j
public class ServiceRunner {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRunner.class, args);

		log.info("HMRC Service Started", value(EVENT, HMRC_SERVICE_STARTED));
	}
}

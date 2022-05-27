package se.magnus.microservices.core.meal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ComponentScan("se.magnus")
@EnableMongoRepositories
public class MealServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(MealServiceApplication.class);
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx  = SpringApplication.run(MealServiceApplication.class, args);
		
		String mongodDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongodDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
	}

}

package kt3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KompiuteriuTinklai3Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(KompiuteriuTinklai3Application.class);
		app.setHeadless(false);
		app.run(args);
	}
}

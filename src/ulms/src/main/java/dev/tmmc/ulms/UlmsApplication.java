package dev.tmmc.ulms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
@RestController
public class UlmsApplication {

    @RequestMapping("/")
    public String home() {
        return "Hello Docker World";
    }

	public static void main(String[] args) {
		SpringApplication.run(UlmsApplication.class, args);
	}

}

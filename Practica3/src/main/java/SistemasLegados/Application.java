package SistemasLegados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // pantalla física sin esto no va por la cara
        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(Application.class, args);
    }
}
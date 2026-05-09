package com.compapption.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * Clase principal de la aplicación Spring Boot de Compapption.
 * Arranca el contexto de Spring y habilita la ejecución de tareas programadas.
 *
 * @author Mario
 */
@SpringBootApplication
@EnableScheduling
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApiApplication.class);
		app.addListeners(new EnvDebugListener());
		app.run(args);
	}

	// TODO: TEMPORAL — quitar tras diagnosticar inyección de vars en Railway.
	// No imprime valores de secretos, sólo presencia + longitud.
	static class EnvDebugListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
		private static final String[] KEYS = {
			"SPRING_PROFILES_ACTIVE", "MAIL_HOST", "MAIL_PORT", "MAIL_USERNAME",
			"DB_HOST", "DB_PORT", "DB_NAME", "DB_USER",
			"FRONTEND_URL", "JWT_SECRET", "CLOUDINARY_CLOUD_NAME", "PORT"
		};

		@Override
		public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
			ConfigurableEnvironment env = event.getEnvironment();
			System.out.println("=== [DEBUG ENV @ Railway] ===");
			System.out.println("active profiles : " + Arrays.toString(env.getActiveProfiles()));
			System.out.println("default profiles: " + Arrays.toString(env.getDefaultProfiles()));
			System.out.println("getenv(SPRING_PROFILES_ACTIVE) = "
				+ describe(System.getenv("SPRING_PROFILES_ACTIVE")));
			for (String key : KEYS) {
				String envVal = System.getenv(key);
				String propVal = env.getProperty(key);
				System.out.println(String.format(
					"%-25s | System.getenv=%s | env.getProperty=%s",
					key, describe(envVal), describe(propVal)));
			}
			System.out.println("=== [/DEBUG ENV] ===");
		}

		private static String describe(String v) {
			if (v == null) return "<null>";
			if (v.isEmpty()) return "<empty>";
			return "len=" + v.length();
		}
	}

}

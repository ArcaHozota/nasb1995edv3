package app.preach.gospel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import app.preach.gospel.common.ProjectConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Projectアプリケーション
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan
public class NASB1995Application {
	public static void main(final String[] args) {
		SpringApplication.run(NASB1995Application.class, args);
		log.info(ProjectConstants.MESSAGE_SPRING_APPLICATION);
	}
}

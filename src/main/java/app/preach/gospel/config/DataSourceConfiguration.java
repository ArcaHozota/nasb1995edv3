package app.preach.gospel.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class DataSourceConfiguration {

	private static String getSecretString() {
		final String secretName = "prod/my_app/pg";
		final Region region = Region.of("ap-northeast-1");
		// Create a Secrets Manager client
		try (SecretsManagerClient client = SecretsManagerClient.builder().region(region).build()) {
			final GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName)
					.build();
			GetSecretValueResponse getSecretValueResponse;
			try {
				getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
			} catch (final Exception e) {
				// For a list of exceptions thrown, see
				// https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
				throw e;
			}
			return getSecretValueResponse.secretString();
			// Your code goes here.
		}
	}

	@Bean
	protected DataSource dataSource() throws Exception {
		final var secretJson = getSecretString();
		final var om = new ObjectMapper();
		final JsonNode n = om.readTree(secretJson);
		final String host = n.get("host").asText();
		final int port = n.get("port").asInt();
		final String dbname = n.get("dbname").asText();
		final String username = n.get("username").asText();
		final String password = n.get("password").asText();
		final String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbname + "?sslmode=require";
		final var ds = new HikariDataSource();
		ds.setJdbcUrl(jdbcUrl);
		ds.setUsername(username);
		ds.setPassword(password);
		return ds;
	}

}

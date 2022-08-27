package com.weatherapicontroller.ForecastCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// logging imports
@EnableJpaRepositories("com.weatherapicontroller.ForecastCollector.repo.JpaRepository")
@SpringBootApplication
public class ForecastCollectorApplication implements CommandLineRunner {

	@Autowired
	static
	JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		// jdbcTemplate.execute("DROP TABLE LOCATION IF EXISTS");
		SpringApplication.run(ForecastCollectorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String DB_URL = "jdbc:postgresql://localhost:5432/forecast_controller";
		String USER = "postgres";
		String PASS = "7875";

		// Open a connection to database
		try {
			Connection conn = DriverManager.getConnection(DB_URL ,USER ,PASS);
			Statement statement = conn.createStatement();

			// Execute a query
			System.out.println("Inserting records into the table...");
			// check if such table exists
			String sql_check = "DROP TABLE IF EXISTS company;";
			statement.executeUpdate(sql_check);
			// create table company with api keys & rest necessary data
			String sql_create = "CREATE TABLE company(" +
					"id SERIAL, name VARCHAR(255), api_key VARCHAR(255), api_url VARCHAR(255));";
			statement.executeUpdate(sql_create);
			// INSERT OWM DATA (url is url pattern id, units, app)
			String sql_insert = "insert into company (api_key, api_url, name) values" +
					"('9afbc956d3b5e6886292ecf8d449f81f'," +
					"'http://api.openweathermap.org/data/2.5/weather?id=%d&appid=%s&units=%s'," +
					"'OWM');";

			statement.executeUpdate(sql_insert);
		} catch (Exception error) {
			System.out.println(error);
		}
	}
}

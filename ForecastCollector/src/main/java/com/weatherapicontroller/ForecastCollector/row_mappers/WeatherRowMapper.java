package com.weatherapicontroller.ForecastCollector.row_mappers;

import com.weatherapicontroller.ForecastCollector.models.Location;
import com.weatherapicontroller.ForecastCollector.models.Weather;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WeatherRowMapper implements RowMapper<Weather> {
    @Override
    public Weather mapRow(ResultSet rs, int rowNum) throws SQLException {
        Weather weather = new Weather();
        weather.setId(rs.getInt("id"));
        weather.setCity(rs.getString("city"));
        weather.setCountry(rs.getString("country"));
        weather.setDate(rs.getDate("date"));
        weather.setTime(rs.getTime("time"));
        weather.setHumidity(rs.getDouble("humidity"));
        weather.setPressure(rs.getDouble("pressure"));
        weather.setTemp(rs.getDouble("temp"));
        return weather;
    }
}


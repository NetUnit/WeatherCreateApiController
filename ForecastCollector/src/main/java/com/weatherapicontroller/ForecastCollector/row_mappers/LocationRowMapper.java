package com.weatherapicontroller.ForecastCollector.row_mappers;

import com.weatherapicontroller.ForecastCollector.models.Location;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationRowMapper implements RowMapper<Location> {

    @Override
    public Location mapRow(ResultSet rs, int rowNum) throws SQLException {

        Location location = new Location();
        location.setId(rs.getInt("id"));
        location.setCity(rs.getString("city"));
        location.setCity_id(rs.getInt("city_id"));
        return location;

    }
}
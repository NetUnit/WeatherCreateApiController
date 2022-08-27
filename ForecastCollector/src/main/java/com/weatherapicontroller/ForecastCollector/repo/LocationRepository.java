package com.weatherapicontroller.ForecastCollector.repo;

import com.weatherapicontroller.ForecastCollector.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface LocationRepository extends CrudRepository<Location, Integer> {

}


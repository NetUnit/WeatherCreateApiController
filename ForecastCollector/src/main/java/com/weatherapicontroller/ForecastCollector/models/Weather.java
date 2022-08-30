package com.weatherapicontroller.ForecastCollector.models;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Weather")
public class Weather {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // satisfy condition of autofield when querying through jdbc
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    // assign fields
    private String country;
    private String city;
    private Date date;
    private Time time;
    private Double pressure;
    private Double humidity;
    private Double temp;
    @ManyToOne
    private Location location;
    private Integer company_id;


    // getters
    public Integer getId() {return id;}
    public String getCountry() {return country;}
    public String getCity() {return city;}
    public Date getDate() {return date;}
    public Time getTime() {return time;}
    private Double getPressure() {return pressure;}
    private Double getHumidity() {return humidity;}
    private Double getTemp() {return temp;}
    private Location getLocation() {return location;}
    private Integer getCompany() {return company_id;}

    // setters
    public void setId(Integer id) {this.id = id;}
    public void setCountry(String country) {this.country = country;}
    public void setCity(String city) {this.city = city;}
    public void setDate(Date date) {this.date = date;}
    public void setTime(Time time) {this.time = time;}
    public void setPressure(Double pressure) {this.pressure = pressure;}
    public void setHumidity(Double humidity) {this.humidity = humidity;}
    public void setTemp(Double temp) {this.temp = temp;}

    public Weather() {};

    public Weather(String country, String city, Date date, Time time,
                   Double pressure, Double humidity, Double temp,
                   Integer location_id, Integer company_id) {
        this.country = country;
        this.city = city;
        this.date = date;
        this.time = time;
        this.pressure = pressure;
        this.humidity = humidity;
        this.temp = temp;
        this.location = new Location();
        this.company_id = company_id;
    };
}

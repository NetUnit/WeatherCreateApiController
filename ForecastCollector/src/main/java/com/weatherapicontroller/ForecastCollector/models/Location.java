package com.weatherapicontroller.ForecastCollector.models;


import javax.persistence.*;

@Entity
@Table(name = "Location")
public class Location {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // satisfy condition of autofield when querying through jdbc
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    // assign fields
    private String city;
    private Integer city_id;


    // adding getters
    public Integer getId() {return id;}
    public String getCity() {return city;}
    public Integer getCity_id() {return city_id;}

    // adding setters
    public void setId(int id) {this.id = id;}
    public void setCity(String city) {this.city = city;}
    public void setCity_id(int city_id) {this.city_id = city_id;}

    // adding init
    public Location() {};

    public Location(String city, Integer city_id) {
        this.city = city;
        this.city_id = city_id;
    }
}

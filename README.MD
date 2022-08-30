# WeatherCollectAPIController

city id's:
London: 2643743
Tokio: 1850147
Perth: 2063523
Philadelphia: 4560349
Kyiv: 703448
Lviv: 702550
Barcelona: 3128760
Riga: 456173
Chicago: 4887398
Sydney: 2147714
Saint-Quentin: 6943723




Curl commnads to test API:

# GET API KEY: "9afbc956d3b5e6886292ecf8d449f81f"

# get only basic weather with city (id) weather & metric data (units)
# curl "https://api.openweathermap.org/data/2.5/weather?q=Budapest&appid=9afbc956d3b5e6886292ecf8d449f81f&units=metric"

# get basic weather parameters
# curl -X GET "http://api.openweathermap.org/data/2.5/weather?id=702550&appid=9afbc956d3b5e6886292ecf8d449f81f&units=metric"


# get * weather data: header -> forecast <- full json
# curl -X GET "http://api.openweathermap.org/data/2.5/forecast?id=702550&appid=9afbc956d3b5e6886292ecf8d449f81f"

# *** TEST POST URLS *** #
# curl -X POST -H "Content-Type: application/json" -d '{"city":"Lviv","api_key":"9afbc956d3b5e6886292ecf8d449f81f", "city_id":"702550"}' "http://localhost:8080/owm"

# *** TEST GET URLS ***
# curl -X GET -H "Content-Type: application/json" -d '{"city":"Lviv","api_key":"9afbc956d3b5e6886292ecf8d449f81f", "city_id":"702550"}' "http://localhost:8080/owm"
# curl -X GET -H "Content-Type: application/json" -d '{"id":"3"}' "http://localhost:8080/owm/3" - header like ... -d '{"id":"3"} is when url considers @RequestBody Map<String, String> as a parameter;

// ALTER TABLE weather ALTER COLUMN date SET DEFAULT now();
// select * from weather join location on location_id=LOCATION.ID where location_id=4 order by location.id
// select l.city, w.country, w.date, w.humidity, w.pressure, w.temp, w.time from weather w left join location l on l.id=w.location_id;










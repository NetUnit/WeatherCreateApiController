package com.weatherapicontroller.ForecastCollector.rest_controllers;

import com.weatherapicontroller.ForecastCollector.models.Location;
import com.weatherapicontroller.ForecastCollector.models.Weather;
import com.weatherapicontroller.ForecastCollector.repo.LocationRepository;
import com.weatherapicontroller.ForecastCollector.row_mappers.LocationRowMapper;
import com.weatherapicontroller.ForecastCollector.row_mappers.WeatherRowMapper;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.springframework.dao.DataIntegrityViolationException;

import com.google.gson.Gson;
// import com.weatherapicontroller.ForecastCollector.repo.JpaRepository;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import  org.apache.commons.beanutils.BeanUtils;

import static java.lang.String.valueOf;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;


@RestController
public class GetForecast {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //    @Autowired
    //    private LocationRepository location_repo;

    private String apiKey="9afbc956d3b5e6886292ecf8d449f81f";
    // create a separate package for that
    private String OWM = "http://api.openweathermap.org/data/2.5/weather?id=%d&appid=%s&units=%s";
    private String unitGroup="metric"; //us,metric,uk
    private String max_rec_sql = "SELECT * FROM %s WHERE id=(SELECT MAX(id) FROM %s)";
    private String last_date_sql = "SELECT * FROM %s where city='%s' ORDER BY id DESC LIMIT 1;";

    // weather parameters
    String weather = "weather";
    String main = "main";
    String sys = "sys";
    public String getApiEndPoint(String city_id, String api_key) {
        Integer cityId = Integer.valueOf(city_id);
        String apiEndPoint = String.format(OWM, cityId, api_key, unitGroup);
        return apiEndPoint;
    }
    public CloseableHttpResponse get_response(String apiEndPoint) throws IOException {
        // submit the request to the Weather API
        HttpGet get = new HttpGet(apiEndPoint);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // get response from url connection
        CloseableHttpResponse response = httpclient.execute(get);
        return response;
    }
    public List get_response_headers(CloseableHttpResponse response) {
        // get all headers from request
        List <Header> headers = List.of(response.getAllHeaders());
        return headers;
    }
    public String getEntityOrError(CloseableHttpResponse response) throws IOException {
        // assign empty string
        String rawResult=null;
        // HTTP entity is a core data that has usually been passed through the HTTP response
        // like python request.data. Entity is an abstraction representing a request or response payload,
        // request body that carries major information of an HTTP request/response.
        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String result = String.format(
                        "This request is inappropriate %d%n", response.getStatusLine().getStatusCode()
                );
                return result;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // raw result is a full json obj entity
                rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
                // System.out.println(rawResult);
            }
        } finally {
            response.close();
        }
        return rawResult;
    }
    public JSONObject get_json_data(String entity_raw_result) {
        // assign new JSON object with entity result
        JSONObject json_full_response = new JSONObject(entity_raw_result);
        return json_full_response;
    }
    // Select appropriate Object from json_data with Class-keys
    public HashMap<String, Object> get_weather_pars(JSONObject json_data) {
        Object weather_pars = json_data.get(main);
        HashMap<String, Object> main_weather = new Gson().fromJson(String.valueOf(weather_pars), HashMap.class);
        return main_weather;
    }
    public HashMap<String, Object> get_main_weather_pars(JSONObject json_data) {
        Object main_weather_pars = json_data.get(main);
        HashMap<String, Object> main_weather = new Gson().fromJson(String.valueOf(main_weather_pars), HashMap.class);
        return main_weather;
    }
    public HashMap<String, Object> get_sys_info(JSONObject json_data) {
        Object sys_info = json_data.get(sys);
        HashMap<String, Object> sys = new Gson().fromJson(String.valueOf(sys_info), HashMap.class);
        return sys;
    }
    public Object get_city(JSONObject json_data) {
        // assign new JSON object with entity result
        Object city = json_data.get("name");
        return city;
    }
    public Object get_city_id(JSONObject json_data) {
        // assign new JSON object with entity result
        Object city_id = json_data.get("id");
        return city_id;
    }
    public void check_obj_field_value(Object object) throws IllegalAccessException {
        // check object fields & values
        List<Field> fields = List.of(object.getClass().getDeclaredFields());
        for (Field field: fields) {
            field.setAccessible(true);
        }
    }
    public boolean check_weather_time_rec(String city) throws IllegalAccessException {
        try {
            // parse db and get of this city
            String last_city_record = String.format(last_date_sql, weather, city);
            List<Weather> last_weather_lst = jdbcTemplate.query(
                    last_city_record,
                    new WeatherRowMapper()
            );
            // get int day and minute value for last record time by city
            Weather last_time = last_weather_lst.get(0);
            String lastDate = String.format("%s", last_time.getDate());
            int last_rec_day = Integer.valueOf(List.of(lastDate.split("-")).get(2));
            System.out.println(String.format("Last record date: %s", last_rec_day));
            String lastTime = String.format("%s", last_time.getTime());
            int last_rec_min = Integer.valueOf(List.of(lastTime.split(":")).get(1));
            System.out.println(String.format("Last record min: %s", last_rec_min));
            // get int day and minute value for now
            LocalTime now = LocalTime.now();
            Date get_current_dt = new Date();
            int current_date = get_current_dt.getDate();
            System.out.println(String.format("Current date: %s", current_date));
            int current_minute = now.getMinute();
            System.out.println(String.format("Current time: %s", current_minute));

            if (current_date - last_rec_day > 0) {
                System.out.println("Date is earlier: can PUSH to db");
                return true;
            }
            if (Math.abs(current_minute - last_rec_min) > 10) {
                System.out.println("Time is bigger than 10 mins: can PUSH to db");
                return true;
            }
        } catch (IndexOutOfBoundsException error) {
            System.out.println(String.format("THIS IS A NEW RECORD IN %s | %s", weather.toUpperCase(), city));
            return true;
        }
        return false;
    }
    // find matches https://www.baeldung.com/spring-data-query-by-example
    public boolean check_obj_exists(String city, Integer city_id) {
        String get_sql = String.format("SELECT * FROM LOCATION WHERE city='%s' OR city_id='%d'", city, city_id);
        List<Location> locations = jdbcTemplate.query(
                get_sql,
                new LocationRowMapper()
        );
        // returns true if such city is already present in the db
        return locations.size() > 0;
    }
    private int getHttpRequestStatus(HttpServletRequest request) throws IOException {
        String request_url = String.format("%s", request.getRequestURL());
        URL url = new URL(request_url);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.connect();

        int code = connection.getResponseCode();
        return code;
    }
    // create separate exceptions
    public void raise404(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    public boolean create_location_entity(JSONObject json_obj) throws IllegalAccessException {
         // *** create Location entity *** \\
        String country = get_sys_info(json_obj).get("country").toString();
        String city = get_city(json_obj).toString();
        Integer city_id = (Integer) get_city_id(json_obj);

        // check if such city already in the db
        if (!check_obj_exists(city, city_id)) {
            // instantiate location obj & put 2 parameters into init
            Location location = new Location(city, city_id);

            check_obj_field_value(location);
            // *** save object *** \\
            // #1 through CRUD operation
            // locationRepo.save(location);

            // #2 through the jpa raw query
            // find max id in the table
            String max_rec = String.format(max_rec_sql, "LOCATION", "LOCATION");

            Location last_record = jdbcTemplate.query(
                    max_rec,
                    new LocationRowMapper()
            ).get(0);
            // incrementing id by 1 position
            Integer max_id = last_record.getId();
            Integer next_id = max_id + 1;

            // insert new record
            String insert_sql = String.format(
                    "INSERT INTO LOCATION (id, city, city_id) values ('%d', '%s', '%d');",
                    next_id, city, city_id
            );
            System.out.println(insert_sql);
            int rows = jdbcTemplate.update(insert_sql);
            if (rows > 0) {
                System.out.println("A new LOCATION ENTITY has been inserted.");
                return true;
            }
            // locationRepo.save(location);
        } else {
            // convert this into exception afterwards
            System.out.println(
                    String.format("Such LOCATION ENTITY is already in the db: %d", HttpStatus.SC_CONFLICT)
            );
        }
        return false;
    }
    public boolean create_basic_weather_entity(JSONObject json_obj) throws IllegalAccessException {
        HashMap sys_map = get_sys_info(json_obj);
        // get country from json obj (HasMap)
        String country = sys_map.get("country").toString();
        String city = get_city(json_obj).toString();

        // get date
        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        // get accurate time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = now.format(format);
        System.out.println(time);
        // get main weather pars from json obj (pressure, humidity, temp...etc)
        HashMap main_weather_pars = get_main_weather_pars(json_obj);
        // get pressure
        Double pressure = (Double) main_weather_pars.get("pressure");
        // get humidity
        Double humidity = (Double) main_weather_pars.get("humidity");
        // get temperature
        Double temp = (Double) main_weather_pars.get("temp");
        // find max id
        String max_rec = String.format(max_rec_sql, weather, weather);
        List<Weather> records = jdbcTemplate.query(
                max_rec,
                new WeatherRowMapper()
        );

        // determination of max id from db
        // id takes part in INSERT COMMAND
        Integer id;
        if (records.size() > 0) {
            // incrementing id by 1 position
            Weather last_record = records.get(0);
            Integer max_id = last_record.getId();
            id = max_id + 1;
        } else {
            id = 1;
        }

        boolean time_check_passed = check_weather_time_rec(city);

        if (time_check_passed) {
            // insert entity into db
            String insert_sql = String.format(
                    "INSERT INTO %s (id, country, city, date, time, humidity, pressure, temp)" +
                            " values ('%d', '%s', '%s', '%tc', '%s', '%.1f', '%.1f', '%.1f');",
                    weather.toUpperCase(), id, country, city, date, time, humidity, pressure, temp
            );
            System.out.println(insert_sql);
            // return true;
            int rows = jdbcTemplate.update(insert_sql);
            if (rows > 0) {
                System.out.println(String.format("A new %s RECORD ENTITY has been inserted.", weather.toUpperCase()));
                return true;
            }
        }
        return false;
    }

    public List get_forecasts_by_city(String city) {
        String query = String.format("select * from weather " +
                "where city='%s' order by date;",
                city
                );
        List<Weather> forecasts_set = jdbcTemplate.query(
                query,
                new WeatherRowMapper()
        );

        return forecasts_set;
    }

    // unique method that responds equally for POST and GET
    @RequestMapping("/owm")
    public String owm_mapper(
            HttpServletRequest request,
            @RequestBody Map<String, String> json_data) throws IOException, IllegalAccessException, ParseException {
        String method = String.format("request method: %s", request.getMethod());
        System.out.println(method);
        // get city_id and api_key if such arrived through header
        String city_id = json_data.get("city_id");
        String api_key = json_data.get("api_key");

        String api_endpoint = getApiEndPoint(city_id, api_key);
        CloseableHttpResponse response = get_response(api_endpoint);
        String entity_raw_result = getEntityOrError(response);
        // full json data
        JSONObject json_obj = get_json_data(entity_raw_result);

        // #1 transfer location entity into db
        create_location_entity(json_obj);

        // #2 transfer weather_par entity into db
        create_basic_weather_entity(json_obj);

        // stringify response
        return json_obj.toString();

        // render JSON object
        // for later --->
    }
    // collects data from openweathermap.org
    @GetMapping("/owm/all")
    public String get_all_weather(HttpServletRequest request) throws IOException, IllegalAccessException {
        String method = String.format("This is %s request", request.getMethod());

        // get list of cities
        String get_all  = "SELECT * FROM LOCATION ORDER BY id";
        List<Location> locations = jdbcTemplate.query(
                get_all,
                new LocationRowMapper()
        );
        JSONObject full_json = new JSONObject();
        for (Location location: locations) {
            String city = location.getCity();
            String city_id = location.getCity_id().toString();
            String api_endpoint = getApiEndPoint(city_id, apiKey);
            CloseableHttpResponse response = get_response(api_endpoint);
            String entity_raw_result = getEntityOrError(response);
            // full json data
            JSONObject json_data = get_json_data(entity_raw_result);
            // extend JSON object for every city
            full_json.append(city, json_data);
        }
        return full_json.toString();
    }
    @GetMapping("/owm/{id}")
    public String get_weather_by_id(@PathVariable String id) throws IOException, IllegalAccessException {
        String get_city = String.format("SELECT * FROM LOCATION where id=%s", id);
        Location city = jdbcTemplate.query(
                get_city,
                new LocationRowMapper()
        ).get(0);
        String city_id = city.getCity_id().toString();
        String api_endpoint = getApiEndPoint(city_id, apiKey);
        CloseableHttpResponse response = get_response(api_endpoint);
        String entity_raw_result = getEntityOrError(response);
        // full json data
        JSONObject json_obj = get_json_data(entity_raw_result);
        return json_obj.toString();
    }
    @GetMapping("/owm/cities/{city}")
    public String get_weather_by_city(@PathVariable String city) throws IllegalAccessException {
        // get object by city;
        List<Weather> forecasts = get_forecasts_by_city(city);
        JSONObject json_obj = new JSONObject();

        for (Weather forecast: forecasts) {
            HashMap<String, Object> data = new HashMap<>();
            Field[] fields = forecast.getClass().getDeclaredFields();

            for (Field field: fields) {
                field.setAccessible(true);
                data.put(field.getName(), field.get(forecast));
            }

            String cityName = forecast.getCity();
            json_obj.append(cityName, data);
        }

        // JSONObject city_weather_records = get_forecasts_by_city(city);
        return json_obj.toString();
    }

    @PostMapping("/owm/create-city")
    public Map<String, String> create(
            HttpServletRequest request,
            @RequestBody Map<String, String> json_data) throws IOException {

        String method = request.getMethod();

        try {
            String city = json_data.get("city");
            String city_id = json_data.get("city_id");
            String apiKey = json_data.get("api_key");

            // remove api_key from response
            json_data.remove("api_key", apiKey);

            // get API endpoint
            String api_endpoint = getApiEndPoint(city_id, apiKey);
            // get response from weather server
            CloseableHttpResponse response = get_response(api_endpoint);
            // get response entity object
            String entity_raw_result = getEntityOrError(response);
            // get weather result
            JSONObject json_obj = get_json_data(entity_raw_result);
            // process object saving into db or pass
            boolean result = create_location_entity(json_obj);

            // define weather json_obj to render here in
            if (result) {
                String msg = "The City has been added to the db";
                String status = String.format("%s", String.format("%d", HttpStatus.SC_CREATED));
                json_data.put("message", msg);
                json_data.put("method", method);
                json_data.put("status", status);
                return json_data;
            } else {
                String status = String.format("%d", HttpStatus.SC_CONFLICT);
                String msg = String.format("Such record is already in the LOCATION: %s", status);
                json_data.put("message", msg);
                json_data.put("method", method);
                json_data.put("status", status);
                return json_data;
            }
        } catch (NumberFormatException error) {
            String ident = "This might caused of U didn't point digital location id";
            String msg = String.format("%s : %s", ident, error);
            System.out.println(msg);
            json_data.put("message", msg);
        } catch (Exception err_other) {
            String ident = "This might be caused of a bad content data - check fields";
            String msg = String.format("%s : %s", ident, err_other);
            System.out.println(msg);
            json_data.put("message", msg);
        }

        String status = String.format("%d", getHttpRequestStatus(request));
        // return json object + message + status
        json_data.put("method", method);
        json_data.put("status", status);
        return json_data;
    }




    // put saving into the db here: into request POST mapping
    //    @PostMapping
    //    public Map<String, String> create(@RequestBody Map<String, String> message) {
    //        message.put("id", valueOf(counter++));
    //        messages.add(message);
    //        return message;
    //    }

    //    public boolean check_obj_exists(String city, Integer city_id) {
    //        System.out.println(city);
    //        System.out.println(city_id);
    //        ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
    //                .withMatcher("jkjk", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
    //                .withMatcher(String.valueOf(456654), ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
    //
    //        Example<Location> example = Example.of(new Location("jkjk", 456654), customExampleMatcher);
    //        Optional match = jpaRepo.findOne(example);
    //
    //        System.out.println(match.isPresent());
    //        // boolean condition = matches.size() != 0;
    //        // System.out.println(condition);
    //        return match.isPresent();
    //    }
}

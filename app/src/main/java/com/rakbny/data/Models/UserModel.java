package com.rakbny.data.Models;

import java.io.Serializable;

/**
 * Created by sotra on 11/2/2016.
 */
public class UserModel implements Serializable {
     String id ,name ,email ,phone , type , bus_id ,token  , school , station_id ;
      Double  lat , lung ;

    public UserModel(String id, String name, String email, String phone, String type, String bus_id, String token,Double lat , Double lung,String station_id) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.bus_id = bus_id;
        this.token = token;
        this.lat = lat ;
        this.lung = lung  ;
        this.station_id = station_id;
    }

    public UserModel() {
    }

    public String getStation_id() {
        return station_id;
    }

    public void setStation_id(String station_id) {
        this.station_id = station_id;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLung() {
        return lung;
    }

    public void setLung(Double lung) {
        this.lung = lung;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBus_id() {
        return bus_id;
    }

    public void setBus_id(String bus_id) {
        this.bus_id = bus_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

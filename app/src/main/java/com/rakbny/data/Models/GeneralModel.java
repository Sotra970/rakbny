package com.rakbny.data.Models;

import java.io.Serializable;

/**
 * Created by sotra on 10/30/2016.
 */
public class GeneralModel implements Serializable{
    String name=null , id=null , type= null ;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

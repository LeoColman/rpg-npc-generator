package me.kerooker.characterinformation;


import java.io.Serializable;

import me.kerooker.enums.Priority;

public class GenericInformation implements Information, Serializable {

    private String information;
    private Priority priority;

    public GenericInformation(String information, Priority priority) {
        this.information = information;
        this.priority = priority;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public String getInformation() {
        return information;
    }
}

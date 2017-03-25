package me.kerooker.characterinformation;


import java.io.Serializable;

import me.kerooker.enums.Priority;

public class Gender implements Information, Serializable {

    private static final Priority genderPriority = Priority.NORMAL;
    private me.kerooker.enums.Gender gender;

    public Gender() {
        this.gender = me.kerooker.enums.Gender.getRandomGender();
    }

    public Gender(me.kerooker.enums.Gender gender) {
        this.gender = gender;
    }

    @Override
    public Priority getPriority() {
        return genderPriority;
    }

    @Override
    public String getInformation() {
        return "Gender: " + gender.toString();
    }
}

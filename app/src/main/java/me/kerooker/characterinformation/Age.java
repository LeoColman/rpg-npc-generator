package me.kerooker.characterinformation;

import android.support.annotation.Nullable;

import java.io.Serializable;

import me.kerooker.enums.Priority;

public class Age implements Information, Serializable {

    private static final Priority agePriority = Priority.NORMAL;
    private me.kerooker.enums.Age age;

    public Age() {
        generateRandomAge();
    }
// --Commented out by Inspection START (12/04/2017 18:40):
//    public Age(me.kerooker.enums.Age age) {
//        this.age = age;
//    }
// --Commented out by Inspection STOP (12/04/2017 18:40)

    private void generateRandomAge() {
        age = me.kerooker.enums.Age.getRandomAge();
    }

    @Nullable
    public me.kerooker.enums.Age getAge() {
        return age;
    }

    @Override
    public Priority getPriority() {
        return agePriority;
    }

    @Override
    public String getInformation() {
        return "Age: " + age.toString();
    }
}

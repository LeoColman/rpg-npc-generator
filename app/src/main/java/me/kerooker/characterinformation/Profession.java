package me.kerooker.characterinformation;

import me.kerooker.enums.Age;
import me.kerooker.enums.Priority;


public class Profession implements Information {

    private me.kerooker.enums.Age age;
    private static final Priority professionPriority = Priority.NORMAL;

    public Profession(me.kerooker.enums.Age age) {
        this.age = age;
    }

    public Profession() {
        this.age = Age.getRandomAge();
    }



    @Override
    public Priority getPriority() {
        return professionPriority;
    }

    @Override
    public String getInformation() {
        return null;
    }
}

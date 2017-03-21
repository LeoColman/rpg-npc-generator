package me.kerooker.characterinformation;

import me.kerooker.enums.Priority;

public class Age implements Information {

    private me.kerooker.enums.Age age;
    private static final Priority agePriority = Priority.NORMAL;

    public Age() {
        generateRandomAge();
    }
    public Age(me.kerooker.enums.Age age) {
        this.age = age;
    }

    private void generateRandomAge() {
        age = me.kerooker.enums.Age.getRandomAge();
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

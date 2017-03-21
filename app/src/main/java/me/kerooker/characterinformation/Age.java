package me.kerooker.characterinformation;

import me.kerooker.enums.Priority;

public class Age implements Information {

    private me.kerooker.enums.Age age;

    public Age() {
        age = me.kerooker.enums.Age.getRandomAge();
    }
    public Age(me.kerooker.enums.Age age) {
        this.age = age;
    }

    @Override
    public Priority getPriority() {
        return null;
    }

    @Override
    public String getInformation() {
        return null;
    }
}

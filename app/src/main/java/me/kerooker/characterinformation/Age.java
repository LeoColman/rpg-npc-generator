package me.kerooker.characterinformation;

import android.support.annotation.Nullable;

import me.kerooker.enums.Priority;

public class Age implements Information {

    private static final Priority agePriority = Priority.HIGH;
    private me.kerooker.enums.Age age;

    public Age() {
        generateRandomAge();
    }
    public Age(me.kerooker.enums.Age age) {
        this.age = age;
    }

    private void generateRandomAge() {
        age = me.kerooker.enums.Age.getRandomAge();
    }

    /**
     *
     * @return
     */
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

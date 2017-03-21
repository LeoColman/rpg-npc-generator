package me.kerooker.characterinformation;


import me.kerooker.enums.Priority;

public class Sexuality implements Information {

    private static final Priority sexualityPriority = Priority.NORMAL;
    private me.kerooker.enums.Sexuality sexuality;

    public Sexuality() {
        this.sexuality = me.kerooker.enums.Sexuality.getRandomSexuality();
    }

    public Sexuality(me.kerooker.enums.Sexuality sexuality) {
        this.sexuality = sexuality;
    }

    @Override
    public Priority getPriority() {
        return sexualityPriority;
    }

    @Override
    public String getInformation() {
        return "Sexuality: " + sexuality.toString();
    }
}

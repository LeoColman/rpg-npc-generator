package me.kerooker.characterinformation;


import java.io.Serializable;

import me.kerooker.enums.Priority;

public class Sexuality implements Information, Serializable {

    private static final Priority sexualityPriority = Priority.NORMAL;
    private me.kerooker.enums.Sexuality sexuality;

    public Sexuality() {
        this.sexuality = me.kerooker.enums.Sexuality.getRandomSexuality();
    }

// --Commented out by Inspection START (12/04/2017 18:41):
//    public Sexuality(me.kerooker.enums.Sexuality sexuality) {
//        this.sexuality = sexuality;
//    }
// --Commented out by Inspection STOP (12/04/2017 18:41)

    @Override
    public Priority getPriority() {
        return sexualityPriority;
    }

    @Override
    public String getInformation() {
        return "Sexuality: " + sexuality.toString();
    }
}

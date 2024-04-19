package org.fourtran.fourtranmc;

import java.time.LocalDate;

public final class Utils {

    public static boolean isHalloween() {
        LocalDate date = LocalDate.now();
        return date.getMonthValue() == 10 && date.getDayOfMonth() == 31;
    }

}

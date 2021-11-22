package mwg.wb.analytics.helper;

import java.util.Objects;

/**
 * Created by Ankush on 17/07/17.
 */
public class PreConditions {


    private PreConditions(){}

    public static <T> void notNull(
            final T t ,
            final String errorMsg)
    {
        if(Objects.isNull(t)){
            throw new IllegalArgumentException(errorMsg);
        }
    }


}

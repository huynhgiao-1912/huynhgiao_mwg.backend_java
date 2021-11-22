package mwg.wb.analytics.helper;

import java.util.Objects;

/**
 * Created by Ankush on 04/04/17.
 */
public class Strings {

    public static boolean hasText(final String str){
        if(Objects.isNull(str)){
            return false;
        }
        for(char ch : str.toCharArray()){
            if(!Character.isWhitespace(ch)){
                return true;
            }
        }
        return false;
    }

}

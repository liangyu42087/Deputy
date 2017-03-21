package deputy.android.com.deputyliang.testing;

import deputy.android.com.deputyliang.model.Shift;

/**
 * Created by liangyu42087 on 2017/3/21.
 */

public class ShiftTestUtil {

    public static Shift[] generateArrayOfFakeShift(){

        Shift[] shifts = new Shift[10];
        for(int i = 0 ; i < 10; i++){
            int position = i+1;
            Shift shift = new Shift();
            shift.set_id(position);
            shift.setShift_id(position);
            shift.setStart("Start time for " + position);
            shift.setStartLatitude("0.0000");
            shift.setStartLongitude("0.0000");
            shift.setImage("https://unsplash.it/500/500/?random");
            shifts[i] =  shift;
        }
        return shifts;
    }
}

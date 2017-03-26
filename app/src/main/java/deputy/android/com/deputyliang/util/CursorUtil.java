package deputy.android.com.deputyliang.util;

import android.database.Cursor;

import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;

/**
 * Created by liangyu42087 on 2017/3/26.
 */

public class CursorUtil {
    public static Shift getShiftFromCursor(Cursor cursor){
        Shift shift = new Shift();
        shift.set_id(cursor.getInt(cursor.getColumnIndex(DeputyContract.ShiftEntry._ID)));
        shift.setStart(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START)));
        shift.setStartLongitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE)));
        shift.setStartLatitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE)));
        shift.setEnd(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END)));
        shift.setEndLatitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE)));
        shift.setEndLongitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE)));
        return shift;
    }
}

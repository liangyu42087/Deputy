package deputy.android.com.deputyliang.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liangyu42087 on 2017/3/25.
 */

public class FileUtil {

    private static final String IMAGE_DIR = "image";
    private static final String TAG = FileUtil.class.getSimpleName();


    public static Uri saveToInternalStorage(Context context, Bitmap bitmapImage, String filename){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);

        File mypath = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write file", e);
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to close file", e);
                e.printStackTrace();
            }
        }
        return  Uri.fromFile(mypath);
    }

}

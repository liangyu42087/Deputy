package deputy.android.com.deputyliang.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;

/**
 * Created by liangyu42087 on 2017/3/23.
 */

public class DeputyAsyncHandler extends AsyncQueryHandler{

    private WeakReference<AsyncListener> mListener;

    public interface AsyncListener {
        void onAsyncComplete(int token, int result, Uri uri, Cursor cursor);
    }

    public DeputyAsyncHandler(ContentResolver cr, AsyncListener listener) {
        super(cr);
        mListener = new WeakReference<AsyncListener>(listener);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        AsyncListener listener = mListener.get();
        if(listener != null){
            listener.onAsyncComplete(token, -1, null, cursor);
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
        AsyncListener listener = mListener.get();
        if(listener != null){
            listener.onAsyncComplete(token, result, null, null);
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        AsyncListener listener = mListener.get();
        if(listener != null){
            listener.onAsyncComplete(token, -1, uri, null);
        }
    }
}

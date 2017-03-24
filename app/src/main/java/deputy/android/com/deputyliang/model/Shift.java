package deputy.android.com.deputyliang.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liangyu42087 on 2017/3/21.
 */

public class Shift implements Parcelable {

    private int _id;
//    private int shift_id;
    private long start;
    private long end;
    private double startLongitude;
    private double startLatitude;
    private double endLongitude;
    private double endLatitude;
    private String image;



    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
/*

    public int getShift_id() {
        return shift_id;
    }


    public void setShift_id(int shift_id) {
        this.shift_id = shift_id;
    }*/



    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Shift() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._id);
        dest.writeLong(this.start);
        dest.writeLong(this.end);
        dest.writeDouble(this.startLongitude);
        dest.writeDouble(this.startLatitude);
        dest.writeDouble(this.endLongitude);
        dest.writeDouble(this.endLatitude);
        dest.writeString(this.image);
    }

    protected Shift(Parcel in) {
        this._id = in.readInt();
        this.start = in.readLong();
        this.end = in.readLong();
        this.startLongitude = in.readDouble();
        this.startLatitude = in.readDouble();
        this.endLongitude = in.readDouble();
        this.endLatitude = in.readDouble();
        this.image = in.readString();
    }

    public static final Creator<Shift> CREATOR = new Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel source) {
            return new Shift(source);
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };
}

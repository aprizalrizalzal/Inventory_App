package id.sch.smkn1batukliang.inventory.model.users.levels;

import android.os.Parcel;
import android.os.Parcelable;

public class Levels implements Parcelable {


    public static final Creator<Levels> CREATOR = new Creator<Levels>() {
        @Override
        public Levels createFromParcel(Parcel in) {
            return new Levels(in);
        }

        @Override
        public Levels[] newArray(int size) {
            return new Levels[size];
        }
    };

    private String authId;
    private LevelsItem levelsItem;

    public Levels() {
    }

    public Levels(String authId, LevelsItem levelsItem) {
        this.authId = authId;
        this.levelsItem = levelsItem;
    }

    protected Levels(Parcel in) {
        authId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public LevelsItem getLevelsItem() {
        return levelsItem;
    }

    public void setLevelsItem(LevelsItem levelsItem) {
        this.levelsItem = levelsItem;
    }
}

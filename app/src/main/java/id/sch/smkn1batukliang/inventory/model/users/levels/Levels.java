package id.sch.smkn1batukliang.inventory.model.users.levels;

import android.os.Parcel;
import android.os.Parcelable;

import id.sch.smkn1batukliang.inventory.model.users.Users;

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

    private String levelId;
    private Users users;

    public Levels() {
    }

    public Levels(String levelId, Users users) {
        this.levelId = levelId;
        this.users = users;
    }

    protected Levels(Parcel in) {
        levelId = in.readString();
        users = in.readParcelable(Users.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(levelId);
        dest.writeParcelable(users, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }


}

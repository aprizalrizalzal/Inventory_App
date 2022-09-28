package id.sch.smkn1batukliang.inventory.model.levels;

import android.os.Parcel;
import android.os.Parcelable;

public class LevelsItem implements Parcelable {

    public static final Creator<LevelsItem> CREATOR = new Creator<LevelsItem>() {
        @Override
        public LevelsItem createFromParcel(Parcel in) {
            return new LevelsItem(in);
        }

        @Override
        public LevelsItem[] newArray(int size) {
            return new LevelsItem[size];
        }
    };

    private String levelId;
    private String level;
    private String timestamp;

    public LevelsItem() {
    }

    public LevelsItem(String levelId, String level, String timestamp) {
        this.levelId = levelId;
        this.level = level;
        this.timestamp = timestamp;
    }

    protected LevelsItem(Parcel in) {
        levelId = in.readString();
        level = in.readString();
        timestamp = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(levelId);
        dest.writeString(level);
        dest.writeString(timestamp);
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

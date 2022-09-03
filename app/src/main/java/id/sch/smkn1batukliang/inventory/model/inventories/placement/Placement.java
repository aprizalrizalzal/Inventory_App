package id.sch.smkn1batukliang.inventory.model.inventories.placement;

import android.os.Parcel;
import android.os.Parcelable;

public class Placement implements Parcelable {

    public static final Creator<Placement> CREATOR = new Creator<Placement>() {
        @Override
        public Placement createFromParcel(Parcel in) {
            return new Placement(in);
        }

        @Override
        public Placement[] newArray(int size) {
            return new Placement[size];
        }
    };

    private String authId;
    private PlacementItem placementItem;

    public Placement() {
    }

    public Placement(String authId, PlacementItem placementItem) {
        this.authId = authId;
        this.placementItem = placementItem;
    }

    protected Placement(Parcel in) {
        authId = in.readString();
        placementItem = in.readParcelable(PlacementItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
        dest.writeParcelable(placementItem, flags);
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

    public PlacementItem getPlacementItem() {
        return placementItem;
    }

    public void setPlacementItem(PlacementItem placementItem) {
        this.placementItem = placementItem;
    }
}

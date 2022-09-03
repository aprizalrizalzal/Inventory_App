package id.sch.smkn1batukliang.inventory.model.inventories.placement;

import android.os.Parcel;
import android.os.Parcelable;

public class PlacementItem implements Parcelable {

    public static final Creator<PlacementItem> CREATOR = new Creator<PlacementItem>() {
        @Override
        public PlacementItem createFromParcel(Parcel in) {
            return new PlacementItem(in);
        }

        @Override
        public PlacementItem[] newArray(int size) {
            return new PlacementItem[size];
        }
    };

    private String photoLink;
    private String placementId;
    private String placement;
    private String timestamp;
    private String username;

    public PlacementItem() {
    }

    public PlacementItem(String photoLink, String placementId, String placement, String timestamp, String username) {
        this.photoLink = photoLink;
        this.placementId = placementId;
        this.placement = placement;
        this.timestamp = timestamp;
        this.username = username;
    }

    protected PlacementItem(Parcel in) {
        photoLink = in.readString();
        placementId = in.readString();
        placement = in.readString();
        timestamp = in.readString();
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photoLink);
        dest.writeString(placementId);
        dest.writeString(placement);
        dest.writeString(timestamp);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

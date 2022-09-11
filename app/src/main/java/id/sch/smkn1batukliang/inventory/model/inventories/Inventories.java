package id.sch.smkn1batukliang.inventory.model.inventories;

import android.os.Parcel;
import android.os.Parcelable;

public class Inventories implements Parcelable {

    public static final Creator<Inventories> CREATOR = new Creator<Inventories>() {
        @Override
        public Inventories createFromParcel(Parcel in) {
            return new Inventories(in);
        }

        @Override
        public Inventories[] newArray(int size) {
            return new Inventories[size];
        }
    };

    private String authId;
    private String placementId;
    private InventoriesItem inventoriesItem;

    public Inventories() {
    }

    public Inventories(String authId, String placementId, InventoriesItem inventoriesItem) {
        this.authId = authId;
        this.placementId = placementId;
        this.inventoriesItem = inventoriesItem;
    }

    protected Inventories(Parcel in) {
        authId = in.readString();
        placementId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
        dest.writeString(placementId);
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

    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public InventoriesItem getInventoriesItem() {
        return inventoriesItem;
    }

    public void setInventoriesItem(InventoriesItem inventoriesItem) {
        this.inventoriesItem = inventoriesItem;
    }
}

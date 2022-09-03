package id.sch.smkn1batukliang.inventory.model.inventories.procurement;

import android.os.Parcel;
import android.os.Parcelable;

public class Procurement implements Parcelable {


    public static final Creator<Procurement> CREATOR = new Creator<Procurement>() {
        @Override
        public Procurement createFromParcel(Parcel in) {
            return new Procurement(in);
        }

        @Override
        public Procurement[] newArray(int size) {
            return new Procurement[size];
        }
    };

    private String authId;
    private String placementId;
    private ProcurementItem procurementItem;

    public Procurement() {
    }

    public Procurement(String authId, String placementId, ProcurementItem procurementItem) {
        this.authId = authId;
        this.placementId = placementId;
        this.procurementItem = procurementItem;
    }

    protected Procurement(Parcel in) {
        authId = in.readString();
        placementId = in.readString();
        procurementItem = in.readParcelable(ProcurementItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
        dest.writeString(placementId);
        dest.writeParcelable(procurementItem, flags);
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

    public ProcurementItem getProcurementItem() {
        return procurementItem;
    }

    public void setProcurementItem(ProcurementItem procurementItem) {
        this.procurementItem = procurementItem;
    }
}

package id.sch.smkn1batukliang.inventory.model.inventories.procurement;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcurementItem implements Parcelable {

    public static final Creator<ProcurementItem> CREATOR = new Creator<ProcurementItem>() {
        @Override
        public ProcurementItem createFromParcel(Parcel in) {
            return new ProcurementItem(in);
        }

        @Override
        public ProcurementItem[] newArray(int size) {
            return new ProcurementItem[size];
        }
    };

    private Double amount;
    private String description;
    private String photoLink;
    private Double price;
    private String procurementId;
    private String procurement;
    private String timestamp;
    private String unit;
    private Integer volume;

    public ProcurementItem() {
    }

    public ProcurementItem(Double amount, String description, String photoLink, Double price, String procurementId, String procurement, String timestamp, String unit, Integer volume) {
        this.amount = amount;
        this.description = description;
        this.photoLink = photoLink;
        this.price = price;
        this.procurementId = procurementId;
        this.procurement = procurement;
        this.timestamp = timestamp;
        this.unit = unit;
        this.volume = volume;
    }

    protected ProcurementItem(Parcel in) {
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readDouble();
        }
        description = in.readString();
        photoLink = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        procurementId = in.readString();
        procurement = in.readString();
        timestamp = in.readString();
        unit = in.readString();
        if (in.readByte() == 0) {
            volume = null;
        } else {
            volume = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(amount);
        }
        dest.writeString(description);
        dest.writeString(photoLink);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        dest.writeString(procurementId);
        dest.writeString(procurement);
        dest.writeString(timestamp);
        dest.writeString(unit);
        if (volume == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(volume);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProcurementId() {
        return procurementId;
    }

    public void setProcurementId(String procurementId) {
        this.procurementId = procurementId;
    }

    public String getProcurement() {
        return procurement;
    }

    public void setProcurement(String procurement) {
        this.procurement = procurement;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }
}

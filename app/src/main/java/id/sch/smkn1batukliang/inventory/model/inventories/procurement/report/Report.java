package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report;

import android.os.Parcel;
import android.os.Parcelable;

public class Report implements Parcelable {


    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    private String authId;
    private String placementId;
    private ReportItem reportItem;

    public Report() {
    }

    public Report(String authId, String placementId, ReportItem reportItem) {
        this.authId = authId;
        this.placementId = placementId;
        this.reportItem = reportItem;
    }

    protected Report(Parcel in) {
        authId = in.readString();
        placementId = in.readString();
        reportItem = in.readParcelable(ReportItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authId);
        dest.writeString(placementId);
        dest.writeParcelable(reportItem, flags);
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

    public ReportItem getReportItem() {
        return reportItem;
    }

    public void setReportItem(ReportItem reportItem) {
        this.reportItem = reportItem;
    }
}

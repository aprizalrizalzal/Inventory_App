package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report;

import android.os.Parcel;
import android.os.Parcelable;

public class ReportItem implements Parcelable {

    public static final Creator<ReportItem> CREATOR = new Creator<ReportItem>() {
        @Override
        public ReportItem createFromParcel(Parcel in) {
            return new ReportItem(in);
        }

        @Override
        public ReportItem[] newArray(int size) {
            return new ReportItem[size];
        }
    };

    private boolean approved;
    private boolean known;
    private String pdfLink;
    private String purpose;
    private boolean received;
    private String report;
    private String reportId;
    private String timestamp;

    public ReportItem() {
    }

    public ReportItem(boolean approved, boolean known, String pdfLink, String purpose, boolean received, String report, String reportId, String timestamp) {
        this.approved = approved;
        this.known = known;
        this.pdfLink = pdfLink;
        this.purpose = purpose;
        this.received = received;
        this.report = report;
        this.reportId = reportId;
        this.timestamp = timestamp;
    }

    protected ReportItem(Parcel in) {
        approved = in.readByte() != 0;
        known = in.readByte() != 0;
        pdfLink = in.readString();
        purpose = in.readString();
        received = in.readByte() != 0;
        report = in.readString();
        reportId = in.readString();
        timestamp = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (approved ? 1 : 0));
        dest.writeByte((byte) (known ? 1 : 0));
        dest.writeString(pdfLink);
        dest.writeString(purpose);
        dest.writeByte((byte) (received ? 1 : 0));
        dest.writeString(report);
        dest.writeString(reportId);
        dest.writeString(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

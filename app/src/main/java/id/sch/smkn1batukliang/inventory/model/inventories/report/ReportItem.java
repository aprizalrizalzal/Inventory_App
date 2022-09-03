package id.sch.smkn1batukliang.inventory.model.inventories.report;

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

    private String pdfLink;
    private String purpose;
    private String report;
    private String reportId;
    private boolean status;
    private String timestamp;

    public ReportItem() {
    }

    public ReportItem(String pdfLink, String purpose, String report, String reportId, boolean status, String timestamp) {
        this.pdfLink = pdfLink;
        this.purpose = purpose;
        this.report = report;
        this.reportId = reportId;
        this.status = status;
        this.timestamp = timestamp;
    }

    protected ReportItem(Parcel in) {
        pdfLink = in.readString();
        purpose = in.readString();
        report = in.readString();
        reportId = in.readString();
        status = in.readByte() != 0;
        timestamp = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pdfLink);
        dest.writeString(purpose);
        dest.writeString(report);
        dest.writeString(reportId);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeString(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

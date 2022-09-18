package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report;

import android.os.Parcel;
import android.os.Parcelable;

import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.Principal;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.TeamLeader;
import id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response.VicePrincipal;

public class ReportItem implements Parcelable {

    private String pdfLink;
    private Principal principal;
    private String purpose;
    private String report;
    private String reportId;
    private TeamLeader teamLeader;
    private String timestamp;
    private Boolean received;
    private VicePrincipal vicePrincipal;

    public ReportItem() {
    }

    public ReportItem(String pdfLink, Principal principal, String purpose, String report, String reportId, TeamLeader teamLeader, String timestamp, Boolean received, VicePrincipal vicePrincipal) {
        this.pdfLink = pdfLink;
        this.principal = principal;
        this.purpose = purpose;
        this.report = report;
        this.reportId = reportId;
        this.teamLeader = teamLeader;
        this.timestamp = timestamp;
        this.received = received;
        this.vicePrincipal = vicePrincipal;
    }

    protected ReportItem(Parcel in) {
        pdfLink = in.readString();
        purpose = in.readString();
        report = in.readString();
        reportId = in.readString();
        timestamp = in.readString();
        byte tmpReceived = in.readByte();
        received = tmpReceived == 0 ? null : tmpReceived == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pdfLink);
        dest.writeString(purpose);
        dest.writeString(report);
        dest.writeString(reportId);
        dest.writeString(timestamp);
        dest.writeByte((byte) (received == null ? 0 : received ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

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

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
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

    public TeamLeader getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(TeamLeader teamLeader) {
        this.teamLeader = teamLeader;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean isReceived() {
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public VicePrincipal getVicePrincipal() {
        return vicePrincipal;
    }

    public void setVicePrincipal(VicePrincipal vicePrincipal) {
        this.vicePrincipal = vicePrincipal;
    }
}

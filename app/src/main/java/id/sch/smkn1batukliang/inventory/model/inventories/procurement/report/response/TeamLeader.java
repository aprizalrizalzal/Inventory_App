package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response;

public class TeamLeader {
    private Boolean approved;
    private String description;

    public TeamLeader() {
    }

    public TeamLeader(Boolean approved, String description) {
        this.approved = approved;
        this.description = description;
    }

    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

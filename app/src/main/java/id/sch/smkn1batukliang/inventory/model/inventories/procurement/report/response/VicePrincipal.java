package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response;

public class VicePrincipal {
    private Boolean known;
    private String description;

    public VicePrincipal() {
    }

    public VicePrincipal(Boolean known, String description) {
        this.known = known;
        this.description = description;
    }

    public Boolean isKnown() {
        return known;
    }

    public void setKnown(Boolean known) {
        this.known = known;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

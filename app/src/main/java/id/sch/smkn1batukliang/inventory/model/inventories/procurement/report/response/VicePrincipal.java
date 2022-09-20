package id.sch.smkn1batukliang.inventory.model.inventories.procurement.report.response;

public class VicePrincipal {
    private String description;
    private Boolean known;

    public VicePrincipal() {
    }

    public VicePrincipal(String description, Boolean known) {
        this.description = description;
        this.known = known;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isKnown() {
        return known;
    }

    public void setKnown(Boolean known) {
        this.known = known;
    }
}

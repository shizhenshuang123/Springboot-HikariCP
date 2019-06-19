package utry.hikaricp.model;

public class UserInfo {
    private Integer id;

    private String remarks;

    public UserInfo() {
    }

    public UserInfo(Integer id, String remarks) {
        this.id = id;
        this.remarks = remarks;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

package n22.online.funtools.bean;

public class MenuBean {
    private String menuId;
    private String menuName;

    public MenuBean() {
    }

    public MenuBean(String menuId, String menuName) {
        this.menuId = menuId;
        this.menuName = menuName;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
}

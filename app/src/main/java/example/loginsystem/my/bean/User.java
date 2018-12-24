package example.loginsystem.my.bean;

// 自定义用户类
public class User {
    private Integer id;              // 用户ID
    private String name;             // 用户名
    private Integer libraryId;       // 当前正在背词库
    private Integer errorInterval;   // 错误间隔
    private Integer randInterval;    // 随机间隔
    private String updated_at;       // 创建时间

    public User()
    {

    }

    public User(Integer userId, String username, Integer libraryId, Integer errorInterval, Integer randInterval, String updated_at)
    {
        this.id = userId;
        this.name = username;
        this.libraryId = libraryId;
        this.errorInterval = errorInterval;
        this.randInterval = randInterval;
        this.updated_at = updated_at;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Integer libraryId) {
        this.libraryId = libraryId;
    }

    public Integer getErrorInterval() {
        return errorInterval;
    }

    public void setErrorInterval(Integer errorInterval) {
        this.errorInterval = errorInterval;
    }

    public Integer getRandInterval() {
        return randInterval;
    }

    public void setRandInterval(Integer randInterval) {
        this.randInterval = randInterval;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", libraryId=" + libraryId +
                ", errorInterval=" + errorInterval +
                ", randInterval=" + randInterval +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }
}

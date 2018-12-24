package example.loginsystem.my.bean;

/**
 * Created by Administrator on 2018/12/13.
 */

public class Library {

    private String name;
    private Integer count;
    private String created_at;
    private Integer id;

    public Library()
    {

    }

    public Library(Integer id, String name, Integer count, String created_at)
    {
        this.name = name;
        this.count = count;
        this.created_at = created_at;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name + "  单词数: " + count;
    }
}

package example.loginsystem.my.bean;

/**
 * Created by Administrator on 2018/12/13.
 */

public class Word {
    private Integer id;         // id
    private String translation; // 翻译
    private String content;     // 单词内容
    private Integer library_id;  // 词库id
    private Integer skill_level;  // 熟练等级

    public Word()
    {

    }

    public Word(Integer id, Integer library_id, String content, String translation)
    {
        this.id = id;
        this.library_id = library_id;
        this.content = content;
        this.translation = translation;
    }


    public Word(Integer id, Integer library_id, String content, String translation, Integer skill_level)
    {
        this.id = id;
        this.library_id = library_id;
        this.content = content;
        this.translation = translation;
        this.skill_level = skill_level;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLibrary_id() {
        return library_id;
    }

    public void setLibrary_id(Integer library_id) {
        this.library_id = library_id;
    }

    public Integer getSkill_level() {
        return skill_level;
    }

    public void setSkill_level(Integer skill_level) {
        this.skill_level = skill_level;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", translation='" + translation + '\'' +
                ", content='" + content + '\'' +
                ", library_id=" + library_id +
                ", skill_level=" + skill_level +
                '}' + "\n";
    }
}

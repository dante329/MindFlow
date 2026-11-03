package model;

/**
 * 知识点/章节实体类
 */
public class KnowledgePoint {

    private int kpId;
    private int subjectId;
    private Integer parentId;
    private String name;
    private int sortOrder;

    public KnowledgePoint() {
    }

    public KnowledgePoint(int subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }

    public KnowledgePoint(int subjectId, Integer parentId, String name, int sortOrder) {
        this.subjectId = subjectId;
        this.parentId = parentId;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    // ==================== Getters & Setters ====================

    public int getKpId() {
        return kpId;
    }

    public void setKpId(int kpId) {
        this.kpId = kpId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        String indent = parentId == null ? "" : "  └─ ";
        return indent + name + " (ID:" + kpId + ")";
    }
}

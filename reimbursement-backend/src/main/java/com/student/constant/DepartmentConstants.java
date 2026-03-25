package com.student.constant;

public class DepartmentConstants {
    
    // 部门ID常量
    public static final int OFFICE = 1;           // 办公室
    public static final int XUEFENG = 2;          // 学风建设部
    public static final int QUANYI = 3;           // 权益保障部
    public static final int WAILIAN = 4;          // 公共关系部
    public static final int MEDIA = 5;            // 自觉传媒部
    public static final int SUZHI = 6;            // 学生综合素质发展中心
    
    /**
     * 根据部门ID获取部门全称
     */
    public static String getDepartmentName(int id) {
        switch (id) {
            case 1:
                return "文案会务与人事管理工作项目组";
            case 2:
                return "学风建设与文化促进工作项目组";
            case 3:
                return "学生权益与提案调研工作项目组";
            case 4:
                return "外联公关与社会融入工作项目组";
            case 5:
                return "品牌建设与媒体推广工作项目组";
            case 6:
                return "活动创意与素质拓展工作项目组";
            default:
                return "未知部门";
        }
    }
}
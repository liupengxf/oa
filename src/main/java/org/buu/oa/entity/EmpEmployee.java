package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("emp_employee")
public class EmpEmployee {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("emp_no")
    private String empNo;

    @TableField("name")
    private String name;

    @TableField("gender")
    private Integer gender;

    @TableField("dept_id")
    private Long deptId;

    @TableField("position")
    private String position;

    @TableField("entry_date")
    private LocalDate entryDate;

    @TableField("status")
    private Integer status;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
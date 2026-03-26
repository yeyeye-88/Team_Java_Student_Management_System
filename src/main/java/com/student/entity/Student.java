package com.student.entity;

import jakarta.persistence.*; //告诉程序连接数据库
@Entity //告诉数据库，这个类是一张表
@Table(name = "student") //告诉数据库表名是student
public class Student {
    @Id  //这个是给数据库看的
    private Long id; //这个是本student类用的成员变量

    public Student(){

    }
}

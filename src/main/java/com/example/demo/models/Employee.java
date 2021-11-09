package com.example.demo.models;


import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.lang.Math;
@Entity
@Table(name="employee", indexes = {
        @Index(name="email_index", columnList = "email")
})
public class Employee {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    @Column(unique = true)
    @NotNull
    @Length(min=14, max=14)
    private String ssn;
    @Column(unique = true)
    @NotNull
    private String email;
    @NotNull
    private String imageurl;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}

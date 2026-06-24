package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "microbiology_test")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String testCode;

    private String naam;

    @Column(nullable = false)
    private boolean extraTest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestType testType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staal_type_id", nullable = false)
    private StaalType staalType;

    public Test() {
    }

    public Test(String testCode, String naam, StaalType staalType, boolean extraTest, TestType testType) {
        this.testCode = testCode;
        this.naam = naam;
        this.staalType = staalType;
        this.extraTest = extraTest;
        this.testType = testType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public boolean isExtraTest() {
        return extraTest;
    }

    public void setExtraTest(boolean extraTest) {
        this.extraTest = extraTest;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public StaalType getStaalType() {
        return staalType;
    }

    public void setStaalType(StaalType staalType) {
        this.staalType = staalType;
    }
}

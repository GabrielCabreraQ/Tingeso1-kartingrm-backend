package com.kartingrm.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "incomingreport")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class IncomingReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true,nullable = false)
    private Long id;

    private String reportType;
    private String monthYear;
    private String fileName;
    private double totalIncome;

    @Lob
    private byte[] fileContent;
}





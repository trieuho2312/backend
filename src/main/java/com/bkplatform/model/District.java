package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="district")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class District {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long districtId;
    @Column(nullable = false, length = 100)
    private String districtName;
    @ManyToOne(optional = false) @JoinColumn(name="province_id")
    private Province province;
}

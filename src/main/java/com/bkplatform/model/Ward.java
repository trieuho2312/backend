package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="ward")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ward {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wardId;
    @Column(nullable = false, length = 100)
    private String wardName;
    @ManyToOne(optional = false) @JoinColumn(name="district_id")
    private District district;
}

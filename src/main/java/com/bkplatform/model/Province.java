package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="province")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Province {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long provinceId;
    @Column(nullable = false, length = 100)
    private String provinceName;
}

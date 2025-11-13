package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="address_book")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    @ManyToOne(optional = false) @JoinColumn(name="user_id")
    private User user;
    @Column(nullable = false, columnDefinition = "text")
    private String addressDetail;
    @Column(length = 15)
    private String phoneNo;
    @ManyToOne @JoinColumn(name="ward_id")
    private Ward ward;
}

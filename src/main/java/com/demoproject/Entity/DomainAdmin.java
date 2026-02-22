package com.demoproject.Entity;



import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="domain_admin",
uniqueConstraints = { 
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "mobilenumber")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DomainAdmin extends BaseUser {

    // @Column(nullable = false)
    // private String domain;

    // @Column(nullable = false)
    // private String name;
    
    // @Column(nullable = false)
    // private String mobileNumber;


    

    @OneToOne
    @JoinColumn(name = "university_id", nullable = false)
    // @JsonBackReference
    @JsonIgnore
    private University university;
  

    

}

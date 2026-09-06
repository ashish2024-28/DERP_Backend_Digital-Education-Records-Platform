package com.demoproject.Entity;



import com.demoproject.listener.StringNormalizationListener;
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
@EntityListeners(StringNormalizationListener.class)
public class DomainAdmin extends BaseUser {


    @OneToOne
    @JoinColumn(name = "university_id", nullable = false)
    // @JsonBackReference
    @JsonIgnore
    private University university;
  

    

}

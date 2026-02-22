package com.demoproject.Entity;



import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faculty",
uniqueConstraints = @UniqueConstraint(columnNames = "gmail"))

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Faculty extends BaseUser {
    
    // facultyId  means (Id which provide by University or collage)
    //unique domain wise
    @Column(nullable = false)
    private String facultyId;


    @Column(nullable = false)
    private String course;
    @Column(nullable = false)
    private String teachingBatch;

    // for this use configration extend WebMvcConfigurer
    private String profilePhotoPath; // Stores "alex_profile.png"





    @ManyToOne
    @JoinColumn(name = "university_id")
    // @JsonBackReference
    @JsonIgnore
    private University university;
   

    


}

package com.demoproject.DTO.FeesAccount.DomainAdmin;

import com.demoproject.Entity.University;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter

//payment method types.
public class FeesCourseWiseDTO {

 private String course;

 private Double fees;


}

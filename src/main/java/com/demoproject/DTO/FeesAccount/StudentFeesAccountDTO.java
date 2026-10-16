package com.demoproject.DTO.FeesAccount;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter

//total fees , total paid , dues of per student with academic session .

public class StudentFeesAccountDTO {


 private String academicSession;

 private Double totalFees;

 private Double totalPaid;

 private Double totalDues;


}

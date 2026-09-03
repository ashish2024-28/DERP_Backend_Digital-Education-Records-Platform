package com.demoproject.Entity;

public enum Role {
    ADMIN, // only 1 admin of the derp (this application )
    DOMAIN_ADMIN, // only 1 admin of the university
    SUB_ADMIN,  // multiple subAdmin like pvc, registar, hods etc
    FEES_ADMIN, //  1 or more to check the fees of students and manage it
    FACULTY,    // multiple faculty or teachers
    STUDENT // multiple students
}

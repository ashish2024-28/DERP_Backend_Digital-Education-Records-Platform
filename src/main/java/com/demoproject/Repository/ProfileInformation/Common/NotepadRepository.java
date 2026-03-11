package com.demoproject.Repository.ProfileInformation.Common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demoproject.Entity.Role;
import com.demoproject.Entity.ProfileInformation.Common.Notepad;

@Repository
public interface NotepadRepository extends JpaRepository<Notepad, Long>{

    List<Notepad> findByOwnerEmailId(String ownerEmailId);

    void deleteByOwnerEmailIdAndId(String ownerEmailId, Long id);


}
package com.topick.superapp.mhc.auth.repository;

import com.topick.superapp.mhc.model.Doctor;
import com.topick.superapp.mhc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.server.UID;
import java.util.Optional;
import java.util.UUID;

public interface DoctoRepository extends JpaRepository<Doctor, UUID> {

}

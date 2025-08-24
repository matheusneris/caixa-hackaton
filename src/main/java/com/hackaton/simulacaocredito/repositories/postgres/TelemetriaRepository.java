package com.hackaton.simulacaocredito.repositories.postgres;

import com.hackaton.simulacaocredito.models.postgres.Telemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    List<Telemetria> findByDataReferencia(LocalDate data);
}

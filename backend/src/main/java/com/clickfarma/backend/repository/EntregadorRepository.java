package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.model.Entregador.StatusEntregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    List<Entregador> findByAtivoTrue();
    List<Entregador> findByStatusAndAtivoTrue(StatusEntregador status);
    List<Entregador> findByStatus(StatusEntregador status);
    Optional<Entregador> findByCpf(String cpf);
    Optional<Entregador> findByCpfAndAtivoTrue(String cpf);
    List<Entregador> findByAtivoTrueAndLatitudeIsNotNull();
}

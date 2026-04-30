package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByFarmaciaIdOrderByDataCriacaoDesc(Long farmaciaId);
    List<Pagamento> findByMotoboyIdOrderByDataCriacaoDesc(Long motoboyId);
    List<Pagamento> findByStatusOrderByDataCriacaoDesc(Pagamento.StatusPagamento status);

    @Query("SELECT p FROM Pagamento p WHERE p.referenciaPeriodo = :periodo")
    List<Pagamento> findByReferenciaPeriodo(@Param("periodo") String periodo);

    @Query("SELECT p FROM Pagamento p ORDER BY p.dataCriacao DESC")
    List<Pagamento> findAllOrderByDataDesc();
}

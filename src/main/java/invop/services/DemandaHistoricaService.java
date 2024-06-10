package invop.services;

import invop.entities.DemandaHistorica;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DemandaHistoricaService extends BaseService<DemandaHistorica, Long> {

    public void crearDemandaHistorica(LocalDate fechaDesde, LocalDate fechaHasta, Long idArticulo, LocalDateTime fechaAlta) throws Exception;
    public Integer calcularDemandaHistorica(LocalDate fechaDesde, LocalDate fechaHasta, Long idArticulo);

    public Integer calcularDemandaHistoricaArticulo(LocalDate fechaDesde, LocalDate fechaHasta, Long idArticulo);
}


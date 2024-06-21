package invop.services;

import invop.dto.DatosPMPDto;
import invop.entities.PrediccionDemanda;

import java.time.LocalDate;
import java.util.List;

public interface PrediccionDemandaService extends BaseService<PrediccionDemanda, Long> {
    // Métodos específicos de Prediccion Demanda

    public Integer calculoPromedioMovilPonderado(DatosPMPDto datosPMP) throws Exception;

    public Integer calculoPromedioMovilPonderadoSuavizado(Double alfa, LocalDate fechaPrediccion, Long idArticulo) throws Exception;
    public Integer calcularPromedioMovilMesAnterior(Long idArticulo, LocalDate fechaPrediccion) throws Exception;
    public Integer calcularRegresionLineal(int cantPeriodosHistoricos, LocalDate fechaPrediccion, Long idArticulo) throws Exception;

    public List<Integer> calcularEstacional(Long idArticulo, Integer cantidadDemandaAnualTotal, Integer anioAPredecir) throws Exception;
}

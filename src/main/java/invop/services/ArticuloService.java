package invop.services;

import invop.entities.Articulo;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

public interface ArticuloService extends BaseService<Articulo, Long> {
    public Articulo findArticuloById(Long id);
    public boolean controlOrdenCompraActiva(Long idArticulo) throws Exception;

    public void darDeBajaArticulo(Long idArticulo) throws Exception;
    public List<Long> getArticulosSinStock(Map<String, Integer> articulosDetalleVenta);
    public Double calculoCGI(Double costoAlmacenamiento, Double costoPedido, Double precioArticulo, Double cantidadAComprar, Double demandaAnual) throws Exception;

    public void guardarValorCGI(Double valorCGI, Articulo Articulo) throws Exception;

    //METODOS PARA EL EOQ

    public void guardarPuntoPedido(Integer valorPP, Articulo Articulo) throws Exception;
    public int calculoStockSeguridad(Long idArticulo) throws Exception;
    public void guardarStockSeguridad(Integer valorSS, Articulo Articulo) throws Exception;

    public void metodoLoteFijo(Long idArticulo) throws Exception;
    public int calculoPuntoPedido(Long idArticulo) throws Exception;
    public Integer calculoDemandaAnual(Long idArticulo) throws Exception;
    public int calculoDeLoteOptimo(Long idArticulo) throws Exception;

    //METODOS PARA EL MODELO INTERVALO FIJO

    //METODOS PARA EL MODELO INTERVALO FIJO
    Integer cantidadMaxima(Articulo articulo) throws Exception;
    Integer cantidadAPedir(Articulo articulo) throws Exception;
    public void modeloIntervaloFijo(Long idArticulo) throws Exception;

    public List<Articulo> listadoFaltantes() throws Exception;
    public List<Articulo> listadoAReponer() throws Exception;

    //para cuando modifica un articulo
    public void modificarArticulo(Long idArticulo) throws Exception;
    public void sacarIntervaloFijo(Articulo articulo) throws Exception;
    public void sacarLoteFijo(Articulo articulo) throws Exception;
    //fin

}

package invop.services;

import invop.entities.*;
import invop.enums.ModeloInventario;
import invop.repositories.ArticuloRepository;
import invop.repositories.BaseRepository;
import invop.repositories.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArticuloServiceImpl extends BaseServiceImpl<Articulo, Long> implements ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;
    @Autowired
    private OrdenCompraService ordenCompraService;
    @Autowired
    private DemandaHistoricaService demandaHistoricaService;
    @Autowired
    private ProveedorArticuloService proveedorArticuloService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private VentaRepository ventaRepository;

    public ArticuloServiceImpl(BaseRepository<Articulo, Long> baseRepository, ArticuloRepository articuloRepository, OrdenCompraService ordenCompraService, DemandaHistoricaService demandaHistoricaService, ProveedorArticuloService proveedorArticuloService, ProveedorService proveedorService) {
        super(baseRepository);
        this.articuloRepository = articuloRepository;
        this.ordenCompraService = ordenCompraService;
        this.demandaHistoricaService = demandaHistoricaService;
        this.proveedorArticuloService = proveedorArticuloService;
        this.proveedorService = proveedorService;
    }

    public Articulo findArticuloById(Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artículo no encontrado con id: " + id));
    }

    //Controla que el Articulo no tenga Ordenes de Compras Activas.
    public boolean controlOrdenCompraActiva(Long idArticulo) throws Exception{
            boolean ordenActiva = ordenCompraService.articuloConOrdenActiva(idArticulo);
            return ordenActiva;
    }

    //Borrar articulo si no hay orden de compra activa
    public void darDeBajaArticulo(Long idArticulo) throws Exception{
        boolean ordenActiva = controlOrdenCompraActiva(idArticulo);
        try{
            if(!ordenActiva){
                Articulo articuloABorrar = articuloRepository.findById(idArticulo).orElseThrow(() -> new EntityNotFoundException("Articulo no encontrado"));
                articuloRepository.delete(articuloABorrar);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public List<Long> getArticulosSinStock(Map<Long, Integer> articulosDetalleVenta){
        List<Long> articulosSinStock = new ArrayList<>();

        for (Map.Entry<Long,Integer> item : articulosDetalleVenta.entrySet()) {
            Long idArticulo = item.getKey();
            Integer cantidad = item.getValue();

            if (articuloRepository.getById(idArticulo).getCantidadArticulo() < cantidad) {
                articulosSinStock.add(idArticulo);
            }

        }
        return articulosSinStock;
    }
    public void disminuirStock(Articulo articulo, Integer cantVendida){
        Integer nuevoStock = articulo.getCantidadArticulo() - cantVendida;
        articulo.setCantidadArticulo(nuevoStock);
        articuloRepository.save(articulo);
    }

    public Double calculoCGI(Double costoAlmacenamiento, Double costoPedido, Double precioArticulo, Double cantidadAComprar, Double demandaAnual) throws Exception {
        try {
            Double costoCompra = precioArticulo * cantidadAComprar;
            Double cgi = costoCompra + costoAlmacenamiento * (cantidadAComprar/2) + costoPedido * (demandaAnual/cantidadAComprar);
            return cgi;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void guardarValorCGI(Double valorCGI, Articulo Articulo) throws Exception{
        Articulo.setCgiArticulo(valorCGI);
        articuloRepository.save(Articulo);

    }

    // METODO LOTE FIJO: Calculo Lote Optimo -> Dividirlo
    //Ver si mover el metodo a DemandaHistorica
    @Override
    @Transactional
    public int calculoDemandaAnual(Long idArticulo) throws Exception {
        try {
            //Obtener fecha actual y fecha de hace un año
            LocalDate fechaActual = LocalDate.now();
            LocalDate fechaHaceUnAno = fechaActual.minusYears(1);
            int demandaAnual = demandaHistoricaService.calcularDemandaHistorica(fechaHaceUnAno,fechaActual ,idArticulo);
            return demandaAnual;
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }

    @Override
    @Transactional
    public int calculoDeLoteOptimo(Long idArticulo) throws Exception {
        try{
            //Buscar el Articulo
            Articulo articulo = findArticuloById(idArticulo);

            //Buscar ProveedorPredeterminado para obtener el CP
            Long proveedorPredeterminado = articulo.getProveedorPredeterminado().getId();

            int demandaAnual = calculoDemandaAnual(idArticulo);
            double costoAlmacenamiento = articulo.getCostoAlmacenamientoArticulo();
            double costoPedido = proveedorArticuloService.findCostoPedido(idArticulo, proveedorPredeterminado);

            int loteOptimo = (int)Math.sqrt((2 * demandaAnual * costoPedido) / costoAlmacenamiento);
            return loteOptimo;
        }
        catch(Exception e ){
            throw new Exception(e.getMessage());
        }
    }


    @Transactional
    public int calculoPuntoPedido(Long idArticulo) throws Exception{
        try {
            int demandaAnual = calculoDemandaAnual(idArticulo);
            Articulo articulo = articuloRepository.findById(idArticulo).orElseThrow(() -> new EntityNotFoundException("Articulo no encontrado"));
            Long idProveedorPredeterminado = articulo.getProveedorPredeterminado().getId();
            List<ProveedorArticulo> todosProveedoresDelArticulo = proveedorArticuloService.findProveedoresByArticulo(idArticulo);
            Double tiempoDemoraProv = 0.0;

            for(ProveedorArticulo proveedorArticulo : todosProveedoresDelArticulo){
                if(proveedorArticulo.getId().equals(idProveedorPredeterminado)){
                    tiempoDemoraProv = proveedorArticulo.getTiempoDemoraArticulo();
                    break;
                }
                if (tiempoDemoraProv == 0.0) {
                    tiempoDemoraProv = proveedorArticuloService.obtenerTiempoDemoraPromedioProveedores(idArticulo);
                }
            }

            int puntoPedido = demandaAnual * tiempoDemoraProv.intValue();

            return puntoPedido;
        } catch(Exception e ){
            throw new Exception(e.getMessage());
        }

    }
    public void guardarPuntoPedido(Integer valorPP, Articulo Articulo) throws Exception{
        Articulo.setPuntoPedidoArticulo(valorPP);
        articuloRepository.save(Articulo);

    }

    @Override
    @Transactional
    public int calculoStockSeguridad(Long idArticulo) throws Exception{
        try {
            Articulo articulo = findArticuloById(idArticulo);

            //Valor de Z -> Despues modificar
            Double valorNormalZ = 1.67;

            Integer puntoPedido;
            if (articulo.getPuntoPedidoArticulo() == null){
                puntoPedido = calculoPuntoPedido(idArticulo);
            } else {
                puntoPedido = articulo.getPuntoPedidoArticulo();
            }

            int stockSeguridad = (int) (valorNormalZ * Math.sqrt(puntoPedido));
            return stockSeguridad;
        }catch(Exception e ){
            throw new Exception(e.getMessage());
        }
    }
    public void guardarStockSeguridad(Integer valorSS, Articulo Articulo) throws Exception{
        Articulo.setStockSeguridadArticulo(valorSS);
        articuloRepository.save(Articulo);

    }

    @Override
    @Transactional
    public void metodoLoteFijo(Long idArticulo) throws Exception{
        try {
            Articulo articulo = articuloRepository.findById(idArticulo).orElseThrow(() -> new Exception("Articulo no encontrado"));

            int loteOptimoCalculado = calculoDeLoteOptimo(idArticulo);
            int puntoPedidoCalculado = calculoPuntoPedido(idArticulo);

            articulo.setLoteOptimoArticulo(loteOptimoCalculado);
            articulo.setPuntoPedidoArticulo(puntoPedidoCalculado);

        } catch(Exception e ){
            throw new Exception(e.getMessage());
        }
    }

    //METODOS PARA EL MODELO INTERVALO FIJO

    @Transactional
    public int cantidadAPedir(Articulo articulo) throws Exception{
        try {
            // q = demandaPromedioDiaria * (TiempoEntrePedidos + TiempoDemoraProveedor) + Z * DesvEstandar(T+L) - InventarioActual
            Long idArticulo = articulo.getId();
            int demandaPromedioDiaria = ventaRepository.calcularDemandaPromedioDiariaDeArticulo(idArticulo);
            int tiempoEntrePedidos = 0; // !!! No es un atributo de cada Articulo?
            double promedioDemoraProv = proveedorArticuloService.obtenerTiempoDemoraPromedioProveedores(idArticulo); // SE USA EL PROMEDIO?!
            Double valorNormalZ = 1.67; //Valor de Z -> Despues intentar modificar/mejorar
            Double desvEstandarTiempoPedidoYDemora = 0.0; // !!!
            // desvEstandarTiempoPedidoYDemora = raiz(TiempoEntrePedidos+tiempoDemora)*desvEstandarDemandaDiaria
            Integer inventarioActual = articulo.getCantidadArticulo();

            Integer cantidadAPedir = (int) (demandaPromedioDiaria * (tiempoEntrePedidos + promedioDemoraProv) + valorNormalZ * desvEstandarTiempoPedidoYDemora - inventarioActual);
            return cantidadAPedir;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public int metodoIntervaloFijo(Long idArticulo) throws Exception{
        try {
            Articulo articulo = articuloRepository.findById(idArticulo).orElseThrow(() -> new Exception("Articulo no encontrado"));
            int cantidadAPedir = cantidadAPedir(articulo);
        } catch (Exception e ){
            throw new Exception(e.getMessage());
        }
        return 0;
    }


    public List<Articulo> listadoFaltantes() throws Exception{
        try{
            List<Articulo> todosArticulos = articuloRepository.findAll();
            List<Articulo> articulosFaltantes = new ArrayList<Articulo>();

            for(Articulo articulo : todosArticulos){
                if(articulo.getCantidadArticulo() < articulo.getStockSeguridadArticulo()){
                    articulosFaltantes.add(articulo);
                }
            }
            return articulosFaltantes;
        } catch (Exception e ){
            throw new Exception(e.getMessage());
        }
    }

    public List<Articulo> listadoAReponer() throws Exception{
        try{
            List<Articulo> todosArticulos = articuloRepository.findAll();
            List<Articulo> articulosAReponer = new ArrayList<Articulo>();


            for(Articulo articulo : todosArticulos){
                boolean ordenActiva = ordenCompraService.articuloConOrdenActiva(articulo.getId());
                if(articulo.getCantidadArticulo() < articulo.getPuntoPedidoArticulo() && !ordenActiva ){
                    articulosAReponer.add(articulo);
                }
            }
            return articulosAReponer;
        } catch (Exception e ){
            throw new Exception(e.getMessage());
        }
    }





}

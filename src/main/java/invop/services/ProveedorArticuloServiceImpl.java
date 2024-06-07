package invop.services;

import invop.entities.OrdenCompra;
import invop.entities.ProveedorArticulo;
import invop.repositories.ProveedorArticuloRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorArticuloServiceImpl extends BaseServiceImpl<ProveedorArticulo, Long> implements ProveedorArticuloService {

    @Autowired
    private ProveedorArticuloRepository proveedorArticuloRepository;

    public ProveedorArticuloServiceImpl(ProveedorArticuloRepository proveedorArticuloRepository){
        super(proveedorArticuloRepository);
        this.proveedorArticuloRepository = proveedorArticuloRepository;
    }

    @Override
    @Transactional
    public List<Object> findProveedoresByArticulo(String filtroArticulo) throws Exception{
        try {
            List<Object> buscarProveedores = proveedorArticuloRepository.findProveedoresByArticulo(filtroArticulo);
            return buscarProveedores;
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<Object> findArticulosByProveedor(String filtroProveedor) throws Exception {
        try {
            List<Object> buscarArticulos = proveedorArticuloRepository.findArticulosByProveedor(filtroProveedor);
            return buscarArticulos;
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
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

    public void guardarValorCGI(Double valorCGI, ProveedorArticulo proveedorArticulo) throws Exception{
        proveedorArticulo.setCgiArticulo(valorCGI);
        proveedorArticuloRepository.save(proveedorArticulo);

    }


}

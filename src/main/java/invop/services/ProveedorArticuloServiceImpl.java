package invop.services;

import invop.entities.ProveedorArticulo;
import invop.repositories.ProveedorArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProveedorArticuloServiceImpl extends BaseServiceImpl<ProveedorArticulo, Long> implements ProveedorArticuloService {

    @Autowired
    private ProveedorArticuloRepository proveedorArticuloRepository;
    public ProveedorArticuloServiceImpl(ProveedorArticuloRepository proveedorArticuloRepository){
        super(proveedorArticuloRepository);
        this.proveedorArticuloRepository = proveedorArticuloRepository;
    }
}
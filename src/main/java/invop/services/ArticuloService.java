package invop.services;

import invop.entities.Articulo;

import java.util.List;

public interface ArticuloService extends BaseService<Articulo, Long> {


    public boolean controlOrdenCompraActiva(Long idArticulo) throws Exception;

    }

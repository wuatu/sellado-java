/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sellado.models;

/**
 *
 * @author crist
 */
public class CajaSellado {
    
    int id;
    String codigo_envase;
    String envase;
    String descripcion;
    int ponderacion;

    public CajaSellado() {
    }

    public CajaSellado(int id, String codigo_envase, String envase, String descripcion, int ponderacion) {
        this.id = id;
        this.codigo_envase = codigo_envase;
        this.envase = envase;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo_envase() {
        return codigo_envase;
    }

    public void setCodigo_envase(String codigo_envase) {
        this.codigo_envase = codigo_envase;
    }

    public String getEnvase() {
        return envase;
    }

    public void setEnvase(String envase) {
        this.envase = envase;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(int ponderacion) {
        this.ponderacion = ponderacion;
    }
    
    
    
}

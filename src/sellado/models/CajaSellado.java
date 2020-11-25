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

    int id = 0;
    String envase = "";
    String variedad = "";
    String categoria = "";
    String calibre = "";
    String correlativo = "";
    int ponderacion = 0;

    public CajaSellado(int id, String envase, String variedad, String categoria, String calibre, String correlativo, int ponderacion) {
        this.id = id;
        this.envase = envase;
        this.variedad = variedad;
        this.categoria = categoria;
        this.calibre = calibre;
        this.correlativo = correlativo;
        this.ponderacion = ponderacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnvase() {
        return envase;
    }

    public void setEnvase(String envase) {
        this.envase = envase;
    }

    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCalibre() {
        return calibre;
    }

    public void setCalibre(String calibre) {
        this.calibre = calibre;
    }

    public String getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(String correlativo) {
        this.correlativo = correlativo;
    }

    public int getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(int ponderacion) {
        this.ponderacion = ponderacion;
    }

}
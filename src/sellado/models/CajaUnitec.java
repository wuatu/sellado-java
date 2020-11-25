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
public class CajaUnitec {
    int id;
    String Cod_Caja;
    String Codigo_Confection; 
    String Confection; 
    String Codigo_Embalaje; 
    String Embalaje; 
    String Codigo_Envase; 
    String Envase; 
    String Categoria; 
    String Categoria_Timbrada; 
    String Codigo_Calibre;
    String Calibre;

    public CajaUnitec(String Cod_Caja, String Codigo_Confection, String Confection, String Codigo_Embalaje, String Embalaje, String Codigo_Envase, String Envase, String Categoria, String Categoria_Timbrada, String Codigo_Calibre, String Calibre) {
        this.Cod_Caja = Cod_Caja;
        this.Codigo_Confection = Codigo_Confection;
        this.Confection = Confection;
        this.Codigo_Embalaje = Codigo_Embalaje;
        this.Embalaje = Embalaje;
        this.Codigo_Envase = Codigo_Envase;
        this.Envase = Envase;
        this.Categoria = Categoria;
        this.Categoria_Timbrada = Categoria_Timbrada;
        this.Codigo_Calibre = Codigo_Calibre;
        this.Calibre = Calibre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCod_Caja() {
        return Cod_Caja;
    }

    public void setCod_Caja(String Cod_Caja) {
        this.Cod_Caja = Cod_Caja;
    }

    public String getCodigo_Confection() {
        return Codigo_Confection;
    }

    public void setCodigo_Confection(String Codigo_Confection) {
        this.Codigo_Confection = Codigo_Confection;
    }

    public String getConfection() {
        return Confection;
    }

    public void setConfection(String Confection) {
        this.Confection = Confection;
    }

    public String getCodigo_Embalaje() {
        return Codigo_Embalaje;
    }

    public void setCodigo_Embalaje(String Codigo_Embalaje) {
        this.Codigo_Embalaje = Codigo_Embalaje;
    }

    public String getEmbalaje() {
        return Embalaje;
    }

    public void setEmbalaje(String Embalaje) {
        this.Embalaje = Embalaje;
    }

    public String getCodigo_Envase() {
        return Codigo_Envase;
    }

    public void setCodigo_Envase(String Codigo_Envase) {
        this.Codigo_Envase = Codigo_Envase;
    }

    public String getEnvase() {
        return Envase;
    }

    public void setEnvase(String Envase) {
        this.Envase = Envase;
    }

    public String getCategoria() {
        return Categoria;
    }

    public void setCategoria(String Categoria) {
        this.Categoria = Categoria;
    }

    public String getCategoria_Timbrada() {
        return Categoria_Timbrada;
    }

    public void setCategoria_Timbrada(String Categoria_Timbrada) {
        this.Categoria_Timbrada = Categoria_Timbrada;
    }

    public String getCodigo_Calibre() {
        return Codigo_Calibre;
    }

    public void setCodigo_Calibre(String Codigo_Calibre) {
        this.Codigo_Calibre = Codigo_Calibre;
    }

    public String getCalibre() {
        return Calibre;
    }

    public void setCalibre(String Calibre) {
        this.Calibre = Calibre;
    }
    
    

   
    
}

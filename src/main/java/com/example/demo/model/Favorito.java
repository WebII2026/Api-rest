package com.example.demo.model;

import java.time.LocalDateTime;

public class Favorito {
    private Long id;
    private Long idProductoExterno;
    private String notaPersonal;
    private LocalDateTime fechaAgregado;

    public Favorito() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdProductoExterno() { return idProductoExterno; }
    public void setIdProductoExterno(Long idProductoExterno) { this.idProductoExterno = idProductoExterno; }

    public String getNotaPersonal() { return notaPersonal; }
    public void setNotaPersonal(String notaPersonal) { this.notaPersonal = notaPersonal; }

    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}

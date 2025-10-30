package com.integradorii.gimnasiov1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "promocion_membresias")
public class PromocionMembresia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promocion_id", nullable = false)
    private Promocion promocion;

    @Column(name = "membresia", nullable = false, length = 50)
    private String membresia; // Ej: Mensual, Trimestral, Anual

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }

    public String getMembresia() { return membresia; }
    public void setMembresia(String membresia) { this.membresia = membresia; }
}

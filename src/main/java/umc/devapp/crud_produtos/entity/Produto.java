package umc.devapp.crud_produtos.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "produtos")

public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "preco")
    private double preco;
    @Column(name = "categoria")
    private String categoria;
    @Column (name = "qtd_estoque")
    private Integer qtd_estoque;
    @Column (name = "forncedeor")
    private String fornecedor;
    @Column (name = "tipo")
    private String tipo;
    @Column (name = "qtd_entrada")
    private Integer qtd_entrada;
    @Column (name = "marca")
    private String marca;
    @Column (name = "cor")
    private String cor;

    public Integer getId() {
        return id;
    }
}

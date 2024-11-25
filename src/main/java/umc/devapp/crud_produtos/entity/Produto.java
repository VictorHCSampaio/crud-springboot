package umc.devapp.crud_produtos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "produtos")

public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
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

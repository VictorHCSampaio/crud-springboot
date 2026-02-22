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

    public Produto (Builder builder){
        this.id = builder.id;
        this.descricao = builder.descricao;
        this.preco = builder.preco;
        this.categoria = builder.categoria;
        this.qtd_estoque = builder.qtd_estoque;
        this.fornecedor = builder.fornecedor;
        this.tipo = builder.tipo;
        this.qtd_entrada = builder.qtd_entrada;
        this.marca = builder.marca;
        this.cor = builder.cor;
    }

    public static class Builder {
        private int id;
        private String descricao;
        private double preco;
        private String categoria;
        private Integer qtd_estoque;
        private String fornecedor;
        private String tipo;
        private Integer qtd_entrada;
        private String marca;
        private String cor;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder descricao(String descricao){
            this.descricao = descricao;
            return this;
        }

        public Builder preco(double preco){
            this.preco = preco;
            return this;
        }

        public Builder categoria(String categoria){
            this.categoria = categoria;
            return this;
        }

        public Builder qtd_estoque(Integer qtd_estoque){
            this.qtd_estoque = qtd_estoque;
            return this;
        }

        public Builder fornecedor(String fornecedor){
            this.fornecedor = fornecedor;
            return this;
        }

        public Builder tipo(String tipo){
            this.tipo = tipo;
            return this;
        }

        public Builder qtd_entrada(Integer qtd_entrada){
            this.qtd_entrada = qtd_entrada;
            return this;
        }

        public Builder marca(String marca){
            this.marca = marca;
            return this;
        }

        public Builder cor(String cor){
            this.cor = cor;
            return this;
        }

        public Produto build(){
            return new Produto(this);
        }
    }

    public Integer getId() {
        return id;
    }
}

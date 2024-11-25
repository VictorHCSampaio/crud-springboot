package umc.devapp.crud_produtos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.devapp.crud_produtos.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}

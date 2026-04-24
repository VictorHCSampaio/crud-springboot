package umc.devapp.crud_produtos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.devapp.crud_produtos.entity.Produto;
import umc.devapp.crud_produtos.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    List<Produto> findByUsuario(Usuario usuario);
    Optional<Produto> findByIdAndUsuario(Integer id, Usuario usuario);
}

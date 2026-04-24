package umc.devapp.crud_produtos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import umc.devapp.crud_produtos.entity.Produto;
import umc.devapp.crud_produtos.entity.Usuario;
import umc.devapp.crud_produtos.repository.ProdutoRepository;
import umc.devapp.crud_produtos.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    ProdutoRepository produtoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Produto> getAllProductsService(){
        return produtoRepository.findByUsuario(getCurrentUser());
    }

    public Optional<Produto> getProductService(Integer id){
        return produtoRepository.findByIdAndUsuario(id, getCurrentUser());
    }

    public Produto insertProductService(Produto produto){
        produto.setUsuario(getCurrentUser());
        return produtoRepository.save(produto);
    }

    public void deleteProductByIdService(Integer id){
        produtoRepository.findByIdAndUsuario(id, getCurrentUser()).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produtoRepository.deleteById(id);
    }

    public Produto updateProductService(Produto produto) {
        Optional<Produto> existingOpt = produtoRepository.findByIdAndUsuario(produto.getId(), getCurrentUser());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado ou não pertence ao usuário");
        }
        Produto existing = existingOpt.get();
        existing.setDescricao(produto.getDescricao());
        existing.setPreco(produto.getPreco());
        existing.setCategoria(produto.getCategoria());
        existing.setQtd_estoque(produto.getQtd_estoque());
        existing.setFornecedor(produto.getFornecedor());
        existing.setTipo(produto.getTipo());
        existing.setQtd_entrada(produto.getQtd_entrada());
        existing.setMarca(produto.getMarca());
        existing.setCor(produto.getCor());
        return produtoRepository.save(existing);
    }

    private Usuario getCurrentUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}

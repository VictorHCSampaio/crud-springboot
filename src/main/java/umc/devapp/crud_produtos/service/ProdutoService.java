package umc.devapp.crud_produtos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import umc.devapp.crud_produtos.entity.Produto;
import umc.devapp.crud_produtos.repository.ProdutoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    ProdutoRepository produtoRepository;

    public List<Produto> getAllProductsService(){
        List<Produto> produtos = produtoRepository.findAll();
        return produtos;
    }

    public Optional<Produto> getProductService(Integer id){
        return produtoRepository.findById(id);
    }

    public Produto insertProductService(Produto produto){
        return produtoRepository.save(produto);
    }

    public void deleteProductByIdService(Integer id){
        produtoRepository.deleteById(id);
    }

    public Produto updateProductService(Produto produto) {
        Produto updatedProduct = produtoRepository.findById(produto.getId()).get();
        updatedProduct = produto;
        return produtoRepository.save(updatedProduct);
    }
}

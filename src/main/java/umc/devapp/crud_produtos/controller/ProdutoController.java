package umc.devapp.crud_produtos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import umc.devapp.crud_produtos.entity.Produto;
import umc.devapp.crud_produtos.service.ProdutoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produto")
public class ProdutoController {

    @Autowired
    ProdutoService produtoService;

    //retorna lista de produtos
    @GetMapping("/listall")
    public ResponseEntity<List<Produto>> getAllProducts(){
        List<Produto> produto = produtoService.getAllProductsService();
        if (produto.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objeto não encontrado");
        }else
        {
            return ResponseEntity.ok(produto);
        }
    }

    //retorna os dados de um produto cujo id é fornecido
    @GetMapping("/list/{id}")
    public ResponseEntity<Optional<Produto>> getProductService(@PathVariable Integer id){
        Optional<Produto> produto = produtoService.getProductService(id);
        if (produto.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objeto não encontrado ou inexistente");
        }else
        {
            return ResponseEntity.ok(produto);
        }
    }

    //insere produto na base de dados
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody Produto produto) {
        try {
            Produto newProduct = produtoService.insertProductService(produto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Produto cadastrado com sucesso! ID: " + newProduct.getId());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cadastrar o produto: " + e.getMessage());
        }
    }


    //atualiza produto na base de dados
    @PutMapping("/update")
    public ResponseEntity<Produto> updateProduct(@RequestBody Produto produto){
        Produto updatedProduct = produtoService.updateProductService(produto);
        return ResponseEntity.ok(updatedProduct);
    }


    //delete os dados de um produto cujo id é fornecido
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id){
        produtoService.deleteProductByIdService(id);
        return ResponseEntity.noContent().build();
    }

}

package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoRepository repository;
    private final ServicoService service;

    public ServicoController(ServicoRepository repository, ServicoService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public List<Servico> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Servico buscar(@PathVariable Integer id) {
        return repository.findById(id).orElseThrow();
    }

    @GetMapping("/area/{area}")
    public List<Servico> porArea(@PathVariable String area) {
        return repository.findByAreaServico(area);
    }

    @GetMapping("/similares")
    public List<AnaliseSimilaridade.SimilarDto> similares(@RequestParam String texto) {
        return service.similares(texto);
    }

    @PostMapping("/analise")
    public AnaliseSimilaridade analisar(@Valid @RequestBody NovoServicoForm form) {
        return service.analisar(form);
    }

    @PostMapping
    public ResponseEntity<Servico> criar(@Valid @RequestBody NovoServicoForm form,
                                         @RequestParam(defaultValue = "false") boolean confirmar,
                                         Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(form, confirmar, principal.getName()));
    }

    @ExceptionHandler(CadastroRejeitadoException.class)
    public ResponseEntity<AnaliseSimilaridade> rejeitado(CadastroRejeitadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getAnalise());
    }
}

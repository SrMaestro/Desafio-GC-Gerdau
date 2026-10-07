package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Item;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/itens")
public class ItemController {

    private final ItemRepository repository;

    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Item criar(@RequestBody Item item) {
        return repository.save(item);
    }

    @GetMapping
    public List<Item> listar() {
        return repository.findAll();
    }
}
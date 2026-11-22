package com.duoc.album.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.album.dto.AlbumRequest;
import com.duoc.album.dto.AlbumResponse;
import com.duoc.album.service.AlbumService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public List<AlbumResponse> findAll() {
        return albumService.findAll().stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AlbumResponse findById(@PathVariable("id") Long id) {
        return AlbumResponse.from(albumService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AlbumResponse.from(albumService.create(request)));
    }

    @PutMapping("/{id}")
    public AlbumResponse update(@PathVariable("id") Long id, @Valid @RequestBody AlbumRequest request) {
        return AlbumResponse.from(albumService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

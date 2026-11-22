package com.duoc.album.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.album.dto.AlbumRequest;
import com.duoc.album.error.ResourceNotFoundException;
import com.duoc.album.model.Album;
import com.duoc.album.repository.AlbumRepository;

@Service
public class AlbumService {

    private static final Logger log = LoggerFactory.getLogger(AlbumService.class);

    private final AlbumRepository albumRepository;

    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Transactional(readOnly = true)
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Album findById(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Album> findByArtist(String artist) {
        return albumRepository.findByArtistContainingIgnoreCase(artist);
    }

    @Transactional
    public Album create(AlbumRequest request) {
        Album album = Album.builder()
                .artist(request.artist())
                .title(request.title())
                .year(request.year())
                .genre(request.genre())
                .price(request.price())
                .stock(request.stock())
                .coverArt(request.coverArt())
                .build();
        Album created = albumRepository.save(album);
        log.info("Album created: {}", created.getIdAlbum());
        return created;
    }

    @Transactional
    public Album update(Long id, AlbumRequest request) {
        Album album = findById(id);
        album.setArtist(request.artist());
        album.setTitle(request.title());
        album.setYear(request.year());
        album.setGenre(request.genre());
        album.setPrice(request.price());
        album.setStock(request.stock());
        album.setCoverArt(request.coverArt());
        Album updated = albumRepository.save(album);
        log.info("Album updated: {}", updated.getIdAlbum());
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        Album album = findById(id);
        albumRepository.delete(album);
        log.info("Album deleted: {}", album.getIdAlbum());
    }
}

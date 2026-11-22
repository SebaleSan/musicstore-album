package com.duoc.album.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.album.model.Album;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtistContainingIgnoreCase(String artist);
}

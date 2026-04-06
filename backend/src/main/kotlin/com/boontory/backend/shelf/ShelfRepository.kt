package com.boontory.backend.shelf

import org.springframework.data.jpa.repository.JpaRepository

interface ShelfRepository : JpaRepository<ShelfEntity, Long> {
    fun findAllByOrderByNameAsc(): List<ShelfEntity>

    fun findByNameIgnoreCase(name: String): ShelfEntity?
}

package com.boontory.backend.shelf

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/api/shelves")
class ShelfController(
    private val shelfService: ShelfService,
) {
    @GetMapping
    fun list(): List<ShelfSummaryDto> = shelfService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ShelfSummaryDto = shelfService.get(id)

    @PostMapping
    fun create(@Valid @RequestBody request: UpsertShelfRequest): ShelfSummaryDto = shelfService.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpsertShelfRequest): ShelfSummaryDto =
        shelfService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = shelfService.delete(id)
}

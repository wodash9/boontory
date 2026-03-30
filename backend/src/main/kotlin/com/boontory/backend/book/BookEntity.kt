package com.boontory.backend.book

import java.time.Instant
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table

@Entity
@Table(name = "books")
class BookEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var isbn: String? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(nullable = false)
    var authors: String = "",

    @Column(length = 2_000)
    var description: String? = null,

    var coverUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReadingStatus = ReadingStatus.WANT_TO_READ,

    @Column(nullable = false)
    var rating: Int = 0,

    @Column(length = 4_000)
    var notes: String? = null,

    var publishedYear: Int? = null,
    var dateRead: LocalDate? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
